package com.zky.algorithm.impl;

import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.dao.ProductDao;
import com.zky.dao.UserBehaviorDao;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserBehavior;
import com.zky.domain.po.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 基于内容的推荐算法 - 商城首页专属（升级多品类分级版）
 * 核心规则：
 * 1. 所有交互过的品类按权重总和降序分级靠前（如汽车用品→户外装备）；
 * 2. 同品类内按与该品类偏好价格的差值升序排序；
 * 3. 未匹配品类商品按最高权重品类的偏好价格差值升序排序；
 * 4. 无参train、全量查询、动态更新均保留
 */
@Slf4j
@Component
public class ContentBasedModel implements RecommendationStrategy {

    /**
     * 行为权重配置：BUY(购买) > COLLECT(收藏) > CLICK(点击)
     */
    private static final Map<String, Integer> BEHAVIOR_WEIGHT = new HashMap<String, Integer>() {{
        put("BUY", 5);
        put("COLLECT", 3);
        put("CLICK", 1);
    }};

    /**
     * 本地缓存：存储用户多品类偏好特征
     * key=userId，value=用户全品类偏好特征（含所有交互品类+最高权重品类）
     * 生产环境建议替换为Redis，支持分布式部署
     */
    private static final Map<String, UserMultiCategoryPrefer> USER_MULTI_CATEGORY_PREFER_CACHE = new ConcurrentHashMap<>();

    // 注入DAO层，内部全量查询数据
    @Resource
    private UserBehaviorDao userBehaviorMapper;
    @Resource
    private ProductDao productInfoMapper;

    /**
     * 内部类：单品类偏好特征（存储单个品类的权重和偏好价格）
     */
    private static class SingleCategoryPrefer {
        private Integer totalWeight; // 该品类的行为权重总和
        private BigDecimal preferPrice; // 该品类的加权平均价格

        public SingleCategoryPrefer() {}
        public SingleCategoryPrefer(Integer totalWeight, BigDecimal preferPrice) {
            this.totalWeight = totalWeight;
            this.preferPrice = preferPrice;
        }

        // getter & setter
        public Integer getTotalWeight() { return totalWeight; }
        public void setTotalWeight(Integer totalWeight) { this.totalWeight = totalWeight; }
        public BigDecimal getPreferPrice() { return preferPrice; }
        public void setPreferPrice(BigDecimal preferPrice) { this.preferPrice = preferPrice; }
    }

    /**
     * 内部类：用户多品类偏好特征（核心缓存对象）
     */
    private static class UserMultiCategoryPrefer {
        private String topCategory; // 权重最高的品类（用于未匹配品类的价格排序）
        private BigDecimal topCategoryPreferPrice; // 最高权重品类的偏好价格
        // 所有交互过的品类偏好：key=品类名，value=单品类特征，按权重降序排序
        private Map<String, SingleCategoryPrefer> interactedCategoryMap;

        // getter & setter
        public String getTopCategory() { return topCategory; }
        public void setTopCategory(String topCategory) { this.topCategory = topCategory; }
        public BigDecimal getTopCategoryPreferPrice() { return topCategoryPreferPrice; }
        public void setTopCategoryPreferPrice(BigDecimal topCategoryPreferPrice) { this.topCategoryPreferPrice = topCategoryPreferPrice; }
        public Map<String, SingleCategoryPrefer> getInteractedCategoryMap() { return interactedCategoryMap; }
        public void setInteractedCategoryMap(Map<String, SingleCategoryPrefer> interactedCategoryMap) { this.interactedCategoryMap = interactedCategoryMap; }
    }

    /**
     * 推荐核心方法：多品类分级排序+未匹配品类价格关联
     */
    @Override
    public List<ProductInfo> recommend(UserInfo user, List<ProductInfo> candidates) {
        log.info("========== 商品推荐算法开始执行 ==========");
        log.info("输入参数：userId={}, 候选商品数={}", user != null ? user.getUserId() : null, candidates != null ? candidates.size() : 0);

        // 入参校验：用户/候选商品为空，直接返回空/原列表
        if (user == null || CollectionUtils.isEmpty(candidates) || user.getUserId() == null) {
            log.warn("【recommend】入参校验失败，返回原候选集：user={}, candidates={}", user, candidates);
            return CollectionUtils.isEmpty(candidates) ? new ArrayList<>() : candidates;
        }

        String userId = user.getUserId();
        // 获取用户多品类偏好特征，无特征（无交互）直接返回原候选集
        UserMultiCategoryPrefer userPrefer = USER_MULTI_CATEGORY_PREFER_CACHE.get(userId);
        if (userPrefer == null || CollectionUtils.isEmpty(userPrefer.getInteractedCategoryMap())) {
            log.info("【recommend】用户无偏好特征，返回原候选集：userId={}", userId);
            return candidates;
        }
        log.info("【recommend】获取用户多品类偏好特征完成：userId={}, 交互品类数={}, 最高权重品类={}, 最高权重品类偏好价格={}",
                userId, userPrefer.getInteractedCategoryMap().size(), userPrefer.getTopCategory(), userPrefer.getTopCategoryPreferPrice());

        // 1. 提取核心偏好信息
        Map<String, SingleCategoryPrefer> interactedCategoryMap = userPrefer.getInteractedCategoryMap();
        String topCategory = userPrefer.getTopCategory();
        BigDecimal topCategoryPreferPrice = userPrefer.getTopCategoryPreferPrice();
        // 交互品类按权重降序排序，得到分级顺序（如汽车用品→户外装备→...）
        List<String> sortedInteractedCategories = interactedCategoryMap.entrySet().stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue().getTotalWeight(), e1.getValue().getTotalWeight()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        log.info("【recommend】交互品类按权重降序排序完成：sortedInteractedCategories={}", sortedInteractedCategories);

        // 最终推荐结果集
        List<ProductInfo> finalRecommendList = new ArrayList<>();

        // 2. 第一步：处理所有交互过的品类，按权重降序分级添加
        for (String category : sortedInteractedCategories) {
            SingleCategoryPrefer categoryPrefer = interactedCategoryMap.get(category);
            if (categoryPrefer == null || categoryPrefer.getPreferPrice() == null) {
                continue;
            }
            log.info("【recommend】处理交互品类：category={}, 品类权重={}, 品类偏好价格={}",
                    category, categoryPrefer.getTotalWeight(), categoryPrefer.getPreferPrice());
            // 筛选该品类的所有商品
            List<ProductInfo> categoryProducts = candidates.stream()
                    .filter(product -> category.equals(product.getCategory()))
                    .collect(Collectors.toList());
            log.info("【recommend】筛选到该品类商品数：category={}, productCount={}", category, categoryProducts.size());
            // 同品类内：按与该品类偏好价格的差值升序排序
            List<ProductInfo> sortedCategoryProducts = categoryProducts.stream()
                    .sorted((p1, p2) -> {
                        BigDecimal diff1 = p1.getPrice().subtract(categoryPrefer.getPreferPrice()).abs();
                        BigDecimal diff2 = p2.getPrice().subtract(categoryPrefer.getPreferPrice()).abs();
                        return diff1.compareTo(diff2);
                    })
                    .collect(Collectors.toList());
            log.info("【recommend】同品类商品按价格贴近度排序完成：category={}, sortedProductCount={}", category, sortedCategoryProducts.size());
            // 添加到最终结果（权重越高的品类，越先添加，排名越靠前）
            finalRecommendList.addAll(sortedCategoryProducts);
        }
        log.info("【recommend】交互品类商品添加完成，当前结果集大小：{}", finalRecommendList.size());

        // 3. 第二步：处理未匹配品类的商品（用户无交互的品类）
        List<ProductInfo> unMatchedProducts = candidates.stream()
                .filter(product -> !interactedCategoryMap.containsKey(product.getCategory()))
                .collect(Collectors.toList());
        log.info("【recommend】筛选到未匹配品类商品数：unMatchedProductCount={}", unMatchedProducts.size());
        // 未匹配品类：按最高权重品类的偏好价格差值升序排序
        List<ProductInfo> sortedUnMatchedProducts = unMatchedProducts.stream()
                .sorted((p1, p2) -> {
                    BigDecimal diff1 = p1.getPrice().subtract(topCategoryPreferPrice).abs();
                    BigDecimal diff2 = p2.getPrice().subtract(topCategoryPreferPrice).abs();
                    return diff1.compareTo(diff2);
                })
                .collect(Collectors.toList());
        log.info("【recommend】未匹配品类商品按最高权重品类价格排序完成：sortedUnMatchedProductCount={}", sortedUnMatchedProducts.size());

        // 4. 拼接最终结果：交互品类（分级排序） + 未匹配品类（按最高权重品类价格排序）
        finalRecommendList.addAll(sortedUnMatchedProducts);
        log.info("【recommend】最终推荐结果拼接完成，总推荐商品数：{}", finalRecommendList.size());
        log.info("========== 商品推荐算法执行完成 ==========");

        return finalRecommendList;
    }

    /**
     * 无参训练/更新方法：内部全量查询，计算多品类偏好特征，XXL-Job直接调用
     */
    public void train() {
        // 记录方法开始执行时间（毫秒级）
        long startTime = System.currentTimeMillis();
        log.info("基于内容推荐算法train方法开始执行，开始时间：{}ms", startTime);

        try {
            // 1. 全量查询用户行为和商品数据
            List<UserBehavior> allUserBehaviors = userBehaviorMapper.selectAll();
            List<ProductInfo> allProducts = productInfoMapper.selectAll();

            // 记录查询到的数据量
            int behaviorCount = CollectionUtils.isEmpty(allUserBehaviors) ? 0 : allUserBehaviors.size();
            int productCount = CollectionUtils.isEmpty(allProducts) ? 0 : allProducts.size();
            log.info("train方法查询数据完成，用户行为数：{}，商品数：{}", behaviorCount, productCount);

            if (CollectionUtils.isEmpty(allUserBehaviors) || CollectionUtils.isEmpty(allProducts)) {
                log.warn("train方法执行终止：用户行为或商品数据为空，行为数：{}，商品数：{}", behaviorCount, productCount);
                return;
            }

            // 2. 构建商品映射：key=productId，value=ProductInfo，避免多次查询
            Map<String, ProductInfo> productMap = allProducts.stream()
                    .filter(product -> product.getProductId() != null)
                    .collect(Collectors.toMap(
                            ProductInfo::getProductId,
                            product -> product,
                            (p1, p2) -> p1
                    ));
            log.info("【train】商品映射构建完成，有效商品数：{}", productMap.size());

            // 3. 按用户分组，过滤有效行为（userId/ProductId非空 + 行为类型合法）
            Map<String, List<UserBehavior>> userBehaviorGroup = allUserBehaviors.stream()
                    .filter(behavior -> behavior.getUserId() != null
                            && behavior.getProductId() != null
                            && BEHAVIOR_WEIGHT.containsKey(behavior.getBehaviorType()))
                    .collect(Collectors.groupingBy(UserBehavior::getUserId));

            // 记录有效用户数（分组后的用户数）
            int validUserCount = userBehaviorGroup.size();
            log.info("train方法有效行为过滤完成，有效用户数：{}", validUserCount);

            // 4. 遍历每个用户，计算多品类偏好特征
            int processedUserCount = 0;
            for (Map.Entry<String, List<UserBehavior>> entry : userBehaviorGroup.entrySet()) {
                String userId = entry.getKey();
                List<UserBehavior> validBehaviors = entry.getValue();
                log.info("【train】开始处理用户：userId={}, 有效行为数={}", userId, validBehaviors.size());

                // 统计每个品类的：总权重、价格加权和（BigDecimal避免精度丢失）
                Map<String, BigDecimal[]> categoryStatMap = new HashMap<>();
                for (UserBehavior behavior : validBehaviors) {
                    ProductInfo product = productMap.get(behavior.getProductId());
                    // 过滤无效商品：品类/价格为空 + 价格<=0
                    if (product == null || product.getCategory() == null
                            || product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                        continue;
                    }

                    String category = product.getCategory();
                    BigDecimal productPrice = product.getPrice();
                    int behaviorWeight = BEHAVIOR_WEIGHT.get(behavior.getBehaviorType());
                    log.debug("【train】处理单条行为：userId={}, productId={}, category={}, productPrice={}, behaviorType={}, behaviorWeight={}",
                            userId, behavior.getProductId(), category, productPrice, behavior.getBehaviorType(), behaviorWeight);

                    // 初始化品类统计：[总权重(BigDecimal), 价格加权和(BigDecimal)]
                    categoryStatMap.putIfAbsent(category, new BigDecimal[]{BigDecimal.ZERO, BigDecimal.ZERO});
                    BigDecimal[] stat = categoryStatMap.get(category);
                    stat[0] = stat[0].add(BigDecimal.valueOf(behaviorWeight)); // 更新总权重
                    stat[1] = stat[1].add(productPrice.multiply(BigDecimal.valueOf(behaviorWeight))); // 更新价格加权和
                }
                log.info("【train】用户品类统计完成：userId={}, 交互品类数={}", userId, categoryStatMap.size());

                // 无有效品类统计，移除缓存并跳过
                if (CollectionUtils.isEmpty(categoryStatMap)) {
                    USER_MULTI_CATEGORY_PREFER_CACHE.remove(userId);
                    log.warn("【train】用户无有效品类统计，移除缓存：userId={}", userId);
                    continue;
                }

                // 5. 构建用户所有交互品类的偏好特征
                Map<String, SingleCategoryPrefer> interactedCategoryMap = new HashMap<>();
                String topCategory = null;
                BigDecimal topCategoryPreferPrice = BigDecimal.ZERO;
                int maxTotalWeight = 0;

                for (Map.Entry<String, BigDecimal[]> categoryEntry : categoryStatMap.entrySet()) {
                    String category = categoryEntry.getKey();
                    BigDecimal[] stat = categoryEntry.getValue();
                    BigDecimal totalWeightBD = stat[0];
                    BigDecimal priceWeightSum = stat[1];
                    int totalWeight = totalWeightBD.intValue();

                    // 计算该品类的加权平均价格（保留2位小数，四舍五入）
                    BigDecimal preferPrice = priceWeightSum.divide(totalWeightBD, 2, RoundingMode.HALF_UP);
                    log.info("【train】计算单品类偏好：userId={}, category={}, totalWeight={}, priceWeightSum={}, preferPrice={}",
                            userId, category, totalWeight, priceWeightSum, preferPrice);
                    // 存入单品类偏好特征
                    interactedCategoryMap.put(category, new SingleCategoryPrefer(totalWeight, preferPrice));

                    // 确定权重最高的品类（更新topCategory）
                    if (totalWeight > maxTotalWeight) {
                        maxTotalWeight = totalWeight;
                        topCategory = category;
                        topCategoryPreferPrice = preferPrice;
                        log.info("【train】更新用户最高权重品类：userId={}, topCategory={}, topCategoryPreferPrice={}",
                                userId, topCategory, topCategoryPreferPrice);
                    }
                }

                // 6. 构建用户多品类偏好特征，存入缓存
                UserMultiCategoryPrefer userMultiCategoryPrefer = new UserMultiCategoryPrefer();
                userMultiCategoryPrefer.setTopCategory(topCategory);
                userMultiCategoryPrefer.setTopCategoryPreferPrice(topCategoryPreferPrice);
                userMultiCategoryPrefer.setInteractedCategoryMap(interactedCategoryMap);
                USER_MULTI_CATEGORY_PREFER_CACHE.put(userId, userMultiCategoryPrefer);
                log.info("【train】用户多品类偏好特征存入缓存完成：userId={}, topCategory={}, interactedCategoryCount={}",
                        userId, topCategory, interactedCategoryMap.size());
                processedUserCount++;
            }

            // 计算方法总执行时间（毫秒），保留3位小数转秒，更易读
            long endTime = System.currentTimeMillis();
            long costMs = endTime - startTime;
            double costSec = costMs / 1000.0;
            log.info("基于内容推荐算法train方法执行成功！总执行时间：{}ms，处理用户数：{}，处理行为数：{}，处理商品数：{}",
                    costMs, validUserCount, behaviorCount, productCount);

        } catch (Exception e) {
            // 异常时也记录执行时间，方便排查异常耗时
            long endTime = System.currentTimeMillis();
            long costMs = endTime - startTime;
            double costSec = costMs / 1000.0;
            log.error("基于内容推荐算法train方法执行失败！执行时间：{}ms，异常信息：{}",
                    costMs, e.getMessage(), e);
        }
    }

    @Override
    public RecommendationType getType() {
        return RecommendationType.MALL_HOME;
    }
}