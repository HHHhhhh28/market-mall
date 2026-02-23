package com.zky.algorithm.impl;

/**
 * @author ：zky
 * @description：
 * @date ：2026/2/22 23:16
 */

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
 * 基于用户的协同过滤推荐算法（UserCF）
 * 核心规则：
 * 1. 计算当前用户与其他用户的行为相似度（余弦相似度）；
 * 2. 筛选相似度TopN的相似用户，收集其交互过但当前用户未交互的商品；
 * 3. 按商品被相似用户交互的频次+行为权重排序推荐；
 * 适用场景：非首页/非拼团的"猜你喜欢"板块
 */
@Slf4j
@Component
public class UserCFModel implements RecommendationStrategy {

    // ====================== 常量配置 ======================
    /** 行为权重：与现有算法保持一致 */
    private static final Map<String, Integer> BEHAVIOR_WEIGHT = new HashMap<String, Integer>() {{
        put("BUY", 5);
        put("COLLECT", 3);
        put("CLICK", 1);
    }};
    /** 相似用户取TopN：平衡推荐效果与性能 */
    private static final int TOP_SIMILAR_USER_NUM = 20;
    /** 推荐商品最大数量：避免返回过多结果 */
    private static final int MAX_RECOMMEND_NUM = 20;
    /** 本地缓存：用户-商品行为评分矩阵（userId -> (productId -> 加权评分)） */
    private static final Map<String, Map<String, Integer>> USER_PRODUCT_SCORE_CACHE = new ConcurrentHashMap<>();
    /** 本地缓存：用户相似度矩阵（userId -> (similarUserId -> 相似度值)） */
    private static final Map<String, Map<String, BigDecimal>> USER_SIMILARITY_CACHE = new ConcurrentHashMap<>();

    // ====================== 依赖注入 ======================
    @Resource
    private UserBehaviorDao userBehaviorMapper;
    @Resource
    private ProductDao productInfoMapper;

    // ====================== 核心推荐方法 ======================
    @Override
    public List<ProductInfo> recommend(UserInfo user, List<ProductInfo> candidates) {
        // 1. 入参校验
        if (user == null || CollectionUtils.isEmpty(candidates) || user.getUserId() == null) {
            log.warn("UserCF推荐：入参无效，返回空列表");
            return new ArrayList<>();
        }
        String currentUserId = user.getUserId();

        // 2. 加载缓存（无缓存则触发计算）
        if (CollectionUtils.isEmpty(USER_PRODUCT_SCORE_CACHE) || CollectionUtils.isEmpty(USER_SIMILARITY_CACHE)) {
            train();
        }

        // 3. 获取当前用户的行为数据和相似用户
        Map<String, Integer> currentUserProductScores = USER_PRODUCT_SCORE_CACHE.getOrDefault(currentUserId, new HashMap<>());
        Map<String, BigDecimal> currentUserSimilarityMap = USER_SIMILARITY_CACHE.getOrDefault(currentUserId, new HashMap<>());
        if (CollectionUtils.isEmpty(currentUserSimilarityMap)) {
            log.info("UserCF推荐：用户{}无相似用户，返回热门商品", currentUserId);
            return getHotProducts(candidates);
        }

        // 4. 筛选TopN相似用户
        List<Map.Entry<String, BigDecimal>> topSimilarUsers = currentUserSimilarityMap.entrySet().stream()
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .limit(TOP_SIMILAR_USER_NUM)
                .collect(Collectors.toList());

        // 5. 收集相似用户交互过但当前用户未交互的商品
        Map<String, Integer> recommendProductScore = new HashMap<>();
        for (Map.Entry<String, BigDecimal> similarUserEntry : topSimilarUsers) {
            String similarUserId = similarUserEntry.getKey();
            BigDecimal similarity = similarUserEntry.getValue();
            // 相似用户的商品行为评分
            Map<String, Integer> similarUserProductScores = USER_PRODUCT_SCORE_CACHE.getOrDefault(similarUserId, new HashMap<>());

            for (Map.Entry<String, Integer> productEntry : similarUserProductScores.entrySet()) {
                String productId = productEntry.getKey();
                // 跳过当前用户已交互的商品
                if (currentUserProductScores.containsKey(productId)) {
                    continue;
                }
                // 商品评分 = 行为权重 * 用户相似度（放大100倍避免小数）
                int productScore = productEntry.getValue() * similarity.multiply(BigDecimal.valueOf(100)).intValue();
                recommendProductScore.put(productId, recommendProductScore.getOrDefault(productId, 0) + productScore);
            }
        }

        // 6. 过滤候选集并排序
        List<ProductInfo> finalRecommendList = candidates.stream()
                // 仅保留相似用户推荐的商品
                .filter(product -> recommendProductScore.containsKey(product.getProductId()))
                // 按推荐评分降序，评分相同按价格升序
                .sorted((p1, p2) -> {
                    int score1 = recommendProductScore.getOrDefault(p1.getProductId(), 0);
                    int score2 = recommendProductScore.getOrDefault(p2.getProductId(), 0);
                    if (score1 != score2) {
                        return Integer.compare(score2, score1);
                    } else {
                        return p1.getPrice().compareTo(p2.getPrice());
                    }
                })
                .limit(MAX_RECOMMEND_NUM)
                .collect(Collectors.toList());

        // 7. 兜底：无推荐结果时返回热门商品
        if (CollectionUtils.isEmpty(finalRecommendList)) {
            finalRecommendList = getHotProducts(candidates);
        }

        log.info("UserCF推荐：用户{}最终推荐商品数{}", currentUserId, finalRecommendList.size());
        return finalRecommendList;
    }

    // ====================== 模型训练/缓存更新方法 ======================
    public void train() {
        long startTime = System.currentTimeMillis();
        log.info("UserCF模型train方法开始执行，开始时间：{}ms", startTime);

        try {
            // 1. 全量查询用户行为和商品数据
            List<UserBehavior> allUserBehaviors = userBehaviorMapper.selectAll();
            List<ProductInfo> allProducts = productInfoMapper.selectAll();
            if (CollectionUtils.isEmpty(allUserBehaviors) || CollectionUtils.isEmpty(allProducts)) {
                log.warn("UserCF train：用户行为或商品数据为空，终止执行");
                return;
            }

            // 2. 构建用户-商品行为评分矩阵
            buildUserProductScoreMatrix(allUserBehaviors);
            log.info("UserCF train：用户-商品评分矩阵构建完成，覆盖用户数{}", USER_PRODUCT_SCORE_CACHE.size());

            // 3. 计算用户间余弦相似度矩阵
            buildUserSimilarityMatrix();
            log.info("UserCF train：用户相似度矩阵构建完成");

            long endTime = System.currentTimeMillis();
            log.info("UserCF模型train执行完成，耗时{}ms，处理行为数{}，商品数{}",
                    endTime - startTime, allUserBehaviors.size(), allProducts.size());

        } catch (Exception e) {
            long endTime = System.currentTimeMillis();
            log.error("UserCF模型train执行失败，耗时{}ms，异常信息：{}",
                    endTime - startTime, e.getMessage(), e);
        }
    }

    // ====================== 核心辅助方法 ======================
    /**
     * 构建用户-商品行为评分矩阵
     * key: userId, value: (productId -> 加权评分)
     */
    private void buildUserProductScoreMatrix(List<UserBehavior> userBehaviors) {
        // 清空旧缓存
        USER_PRODUCT_SCORE_CACHE.clear();

        // 按用户分组行为
        Map<String, List<UserBehavior>> userBehaviorGroup = userBehaviors.stream()
                .filter(behavior -> behavior.getUserId() != null
                        && behavior.getProductId() != null
                        && BEHAVIOR_WEIGHT.containsKey(behavior.getBehaviorType()))
                .collect(Collectors.groupingBy(UserBehavior::getUserId));

        // 计算每个用户的商品加权评分（同一商品多次行为累加）
        for (Map.Entry<String, List<UserBehavior>> entry : userBehaviorGroup.entrySet()) {
            String userId = entry.getKey();
            List<UserBehavior> behaviors = entry.getValue();

            Map<String, Integer> productScoreMap = new HashMap<>();
            for (UserBehavior behavior : behaviors) {
                String productId = behavior.getProductId();
                int weight = BEHAVIOR_WEIGHT.get(behavior.getBehaviorType());
                // 累加评分（同一商品多次交互权重叠加）
                productScoreMap.put(productId, productScoreMap.getOrDefault(productId, 0) + weight);
            }

            USER_PRODUCT_SCORE_CACHE.put(userId, productScoreMap);
        }
    }

    /**
     * 计算用户间余弦相似度矩阵
     * 余弦相似度公式：sim(A,B) = (A·B) / (||A|| * ||B||)
     */
    private void buildUserSimilarityMatrix() {
        // 清空旧缓存
        USER_SIMILARITY_CACHE.clear();

        // 获取所有用户ID
        List<String> allUserIds = new ArrayList<>(USER_PRODUCT_SCORE_CACHE.keySet());
        if (CollectionUtils.isEmpty(allUserIds)) {
            return;
        }

        // 遍历所有用户对，计算相似度
        for (int i = 0; i < allUserIds.size(); i++) {
            String userIdA = allUserIds.get(i);
            Map<String, Integer> scoreMapA = USER_PRODUCT_SCORE_CACHE.get(userIdA);
            if (CollectionUtils.isEmpty(scoreMapA)) {
                continue;
            }

            Map<String, BigDecimal> similarityMap = new HashMap<>();
            // 计算用户A与其他用户的相似度
            for (int j = 0; j < allUserIds.size(); j++) {
                String userIdB = allUserIds.get(j);
                if (userIdA.equals(userIdB)) {
                    continue; // 跳过自身
                }

                Map<String, Integer> scoreMapB = USER_PRODUCT_SCORE_CACHE.get(userIdB);
                if (CollectionUtils.isEmpty(scoreMapB)) {
                    continue;
                }

                // 计算分子：A·B（共现商品的评分乘积和）
                int dotProduct = 0;
                for (String productId : scoreMapA.keySet()) {
                    if (scoreMapB.containsKey(productId)) {
                        dotProduct += scoreMapA.get(productId) * scoreMapB.get(productId);
                    }
                }

                // 计算分母：||A|| * ||B||（模长乘积）
                double normA = Math.sqrt(scoreMapA.values().stream().mapToInt(x -> x * x).sum());
                double normB = Math.sqrt(scoreMapB.values().stream().mapToInt(x -> x * x).sum());
                if (normA == 0 || normB == 0) {
                    continue; // 避免除零
                }

                // 计算余弦相似度（保留4位小数）
                BigDecimal similarity = BigDecimal.valueOf(dotProduct / (normA * normB))
                        .setScale(4, RoundingMode.HALF_UP);
                if (similarity.compareTo(BigDecimal.ZERO) > 0) {
                    similarityMap.put(userIdB, similarity);
                }
            }

            USER_SIMILARITY_CACHE.put(userIdA, similarityMap);
        }
    }

    /**
     * 热门商品兜底（无相似用户时使用）
     * 按商品被交互的总权重排序
     */
    private List<ProductInfo> getHotProducts(List<ProductInfo> candidates) {
        // 统计所有商品的交互总权重
        Map<String, Integer> productHotScore = new HashMap<>();
        for (Map<String, Integer> userProductScore : USER_PRODUCT_SCORE_CACHE.values()) {
            for (Map.Entry<String, Integer> entry : userProductScore.entrySet()) {
                productHotScore.put(entry.getKey(), productHotScore.getOrDefault(entry.getKey(), 0) + entry.getValue());
            }
        }

        // 候选商品按热门度排序
        return candidates.stream()
                .filter(product -> product.getProductId() != null && productHotScore.containsKey(product.getProductId()))
                .sorted((p1, p2) -> {
                    int score1 = productHotScore.getOrDefault(p1.getProductId(), 0);
                    int score2 = productHotScore.getOrDefault(p2.getProductId(), 0);
                    return Integer.compare(score2, score1);
                })
                .limit(MAX_RECOMMEND_NUM)
                .collect(Collectors.toList());
    }

    @Override
    public RecommendationType getType() {
        return RecommendationType.LOTTERY;
    }
}
