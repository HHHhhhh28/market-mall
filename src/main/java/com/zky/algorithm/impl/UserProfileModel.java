package com.zky.algorithm.impl;

import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.dao.ProductDao;
import com.zky.dao.UserBehaviorDao;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 基于用户画像的拼团商品推荐模型（适配user_tags字段+动态更新标签）
 * 核心：用户画像标签 ↔ 商品user_tags标签匹配，权重：性别5分 > 年龄段3分
 */
@Component
@Slf4j
public class UserProfileModel implements RecommendationStrategy {

    // ====================== 1. 完整注入所需DAO/Mapper ======================
    @Autowired
    private UserBehaviorDao userBehaviorMapper; // 用户行为Mapper
    @Autowired
    private ProductDao productInfoMapper;   // 商品Mapper

    // ====================== 2. 常量配置（仅保留你实际的行为类型权重） ======================
    // 行为权重：完全匹配你的数据库值 CLICK/COLLECT/BUY
    private static final int BUY_WEIGHT = 5;       // 购买权重
    private static final int COLLECT_WEIGHT = 3;   // 收藏权重
    private static final int CLICK_WEIGHT = 1;     // 点击权重
    // 标签更新阈值：占比≥60%保留，＜30%剔除，30%-60%不修改
    private static final double TAG_KEEP_RATIO = 0.6;
    private static final double TAG_REMOVE_RATIO = 0.3;
    // 最小行为数阈值：不同用户数＜最小值时不更新标签（避免样本过少）
    private static final int MIN_BEHAVIOR_COUNT = 3;

    // ====================== 3. 原有推荐核心方法 ======================
    @Override
    public RecommendationType getType() {
        return RecommendationType.GROUP_BUY;
    }

    @Override
    public List<ProductInfo> recommend(UserInfo user, List<ProductInfo> candidates) {
        // 1. 候选集空校验
        if (candidates == null || candidates.isEmpty()) {
            log.info("拼团推荐：候选商品集为空，返回空列表");
            return new ArrayList<>();
        }

        // 2. 过滤无库存商品（仅保留stock>0）
        List<ProductInfo> stockValidProducts = candidates.stream()
                .filter(product -> product.getStock() != null && product.getStock() > 0)
                .collect(Collectors.toList());
        if (stockValidProducts.isEmpty()) {
            log.info("拼团推荐：过滤后无有效库存商品，返回空列表");
            return new ArrayList<>();
        }

        // 3. 提取/兜底用户画像特征
        UserProfileFeature profileFeature = Optional.ofNullable(user)
                .map(this::extractUserProfileFeature)
                .orElseGet(this::getDefaultProfileFeature);

        // 4. 计算标签匹配度
        Map<ProductInfo, Integer> productMatchMap = calculateTagMatchScore(stockValidProducts, profileFeature);
        if (productMatchMap.isEmpty()) {
            log.info("拼团推荐：无匹配用户画像的商品，返回空列表");
            return new ArrayList<>();
        }

        // 5. 排序：匹配度降序 → 价格升序
        List<ProductInfo> sortedProducts = productMatchMap.entrySet().stream()
                .sorted((e1, e2) -> {
                    // 第一步：比较匹配度（核心排序）
                    int scoreCompare = e2.getValue().compareTo(e1.getValue());
                    // 第二步：匹配度不同则按匹配度排；匹配度相同则按价格排
                    return scoreCompare != 0 ? scoreCompare : e1.getKey().getPrice().compareTo(e2.getKey().getPrice());
                })
                .limit(18) // 核心新增：排序后直接取前18个，不足18个则取实际数量
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        log.info("拼团推荐完成：原始{}个 → 过滤库存{}个 → 标签匹配{}个",
                candidates.size(), stockValidProducts.size(), sortedProducts.size());
        return sortedProducts;
    }

    // ====================== 4. 推荐核心辅助方法 ======================
    /**
     * 提取用户标准化画像特征（0=女/1=男，年龄转区间）
     */
    private UserProfileFeature extractUserProfileFeature(UserInfo user) {
        UserProfileFeature feature = new UserProfileFeature();
        // 性别转换
        Integer genderCode = user.getGender();
        if (0 == genderCode) {
            feature.setGenderTag("女");
        } else if (1 == genderCode) {
            feature.setGenderTag("男");
        } else {
            feature.setGenderTag("通用");
            log.warn("用户{}性别码异常：{}，设为通用", user.getUserId(), genderCode);
        }
        // 年龄转区间
        feature.setAgeRangeTag(getAgeRange(user.getAge()));
        return feature;
    }

    /**
     * 计算user_tags标签匹配度（性别5分，年龄段3分）
     */
    private Map<ProductInfo, Integer> calculateTagMatchScore(List<ProductInfo> products, UserProfileFeature profile) {
        Map<ProductInfo, Integer> matchScoreMap = new HashMap<>();
        String userGender = profile.getGenderTag();
        String userAgeRange = profile.getAgeRangeTag();

        for (ProductInfo product : products) {
            int matchScore = 0;
            Set<String> productTags = extractProductUserTags(product);
            if (productTags.isEmpty()) {
                continue;
            }

            // 性别匹配加分
            if (!"通用".equals(userGender) && productTags.contains(userGender)) {
                matchScore += 5;
            }
            // 年龄段匹配加分
            if (isNotBlank(userAgeRange) && productTags.contains(userAgeRange)) {
                matchScore += 3;
            }

            if (matchScore > 0) {
                matchScoreMap.put(product, matchScore);
            }
        }
        return matchScoreMap;
    }

    /**
     * 从user_tags提取标签（去重、去空）
     */
    private Set<String> extractProductUserTags(ProductInfo product) {
        Set<String> tags = new HashSet<>();
        if (product.getUserTags() == null) {
            return tags;
        }
        Arrays.stream(product.getUserTags().split(","))
                .map(String::trim)
                .filter(this::isNotBlank)
                .forEach(tags::add);
        return tags;
    }

    /**
     * 年龄转区间（与user_tags格式一致）
     */
    private String getAgeRange(Integer age) {
        if (age == null || age < 18) {
            return "under18";
        } else if (age >= 18 && age <= 25) {
            return "18-25";
        } else if (age >= 26 && age <= 35) {
            return "26-35";
        } else if (age >= 36 && age <= 45) {
            return "36-45";
        } else if (age >= 46 && age <= 55) {
            return "46-55";
        } else {
            return "over55";
        }
    }

    /**
     * 默认用户画像（兜底）
     */
    private UserProfileFeature getDefaultProfileFeature() {
        UserProfileFeature feature = new UserProfileFeature();
        feature.setGenderTag("通用");
        feature.setAgeRangeTag("26-35");
        log.info("拼团推荐：无用户信息，使用默认通用画像");
        return feature;
    }

    /**
     * JDK8兼容：判断字符串非空
     */
    private boolean isNotBlank(String str) {
        if (str == null || str.length() == 0) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // ====================== 5. 标签动态更新核心方法（仅保留CLICK/COLLECT/BUY） ======================

    public void dynamicUpdateProductUserTags() {
        log.info("开始执行商品user_tags标签动态更新任务");
        try {
            // 1. 查询近30天有行为的商品ID（String类型）
            List<String> productIds = userBehaviorMapper.selectProductIdsWithBehavior(30);
            if (productIds.isEmpty()) {
                log.info("商品标签更新：近30天无用户行为商品，任务结束");
                return;
            }

            // 2. 遍历商品更新标签
            for (String productId : productIds) {

                // 新增：查询该商品近30天不同交互用户数
                Integer distinctUserCount = userBehaviorMapper.selectDistinctUserCountByProductId(productId, 30);
                // 处理空值（无行为时count为null，按0处理）
                distinctUserCount = distinctUserCount == null ? 0 : distinctUserCount;

                // 核心判断：不同用户数＜最小值，不更新标签（完全贴合你的诉求）
                if (distinctUserCount < MIN_BEHAVIOR_COUNT) {
                    log.info("商品{}不同交互用户数{}＜{}，不更新标签", productId, distinctUserCount, MIN_BEHAVIOR_COUNT);
                    continue;
                }
                // 查询单商品近30天加权用户画像
                List<BehaviorUserProfile> behaviorProfiles = userBehaviorMapper.selectWeightedUserProfileByProductId(productId, 30);


                // 3. 统计性别/年龄段加权分数
                Map<String, Integer> genderScoreMap = new HashMap<>();
                Map<String, Integer> ageRangeScoreMap = new HashMap<>();
                for (BehaviorUserProfile profile : behaviorProfiles) {
                    genderScoreMap.put(profile.getGender(), genderScoreMap.getOrDefault(profile.getGender(), 0) + profile.getWeight());
                    ageRangeScoreMap.put(profile.getAgeRange(), ageRangeScoreMap.getOrDefault(profile.getAgeRange(), 0) + profile.getWeight());
                }

                // 4. 计算总权重，筛选有效标签
                int totalWeight = behaviorProfiles.stream().mapToInt(BehaviorUserProfile::getWeight).sum();
                Set<String> newGenderTags = filterValidTags(genderScoreMap, totalWeight);
                Set<String> newAgeRangeTags = filterValidTags(ageRangeScoreMap, totalWeight);

                // 5. 拼接新标签并更新（JDK8兼容写法）
                if (!newGenderTags.isEmpty() || !newAgeRangeTags.isEmpty()) {
                    String newUserTags = String.join(",",
                            Stream.concat(newGenderTags.stream(), newAgeRangeTags.stream())
                                    .collect(Collectors.toList())
                    );
                    productInfoMapper.updateUserTagsByProductId(productId, newUserTags);
                    log.info("商品{}标签更新完成：新标签={}", productId, newUserTags);
                } else {
                    log.info("商品{}无符合阈值的标签，保留原标签", productId);
                }
            }

            log.info("商品user_tags标签动态更新任务执行完成，共处理{}个商品", productIds.size());
        } catch (Exception e) {
            log.error("商品user_tags标签动态更新任务执行失败", e);
        }
    }

    /**
     * 过滤有效标签：占比≥60%保留
     */
    private Set<String> filterValidTags(Map<String, Integer> tagScoreMap, int totalWeight) {
        Set<String> validTags = new HashSet<>();
        for (Map.Entry<String, Integer> entry : tagScoreMap.entrySet()) {
            double ratio = (double) entry.getValue() / totalWeight;
            if (ratio >= TAG_KEEP_RATIO) {
                validTags.add(entry.getKey());
            }
        }
        return validTags;
    }

    // ====================== 6. 内部静态类（特征封装） ======================
    /**
     * 用户画像特征（推荐用）
     */
    private static class UserProfileFeature {
        private String genderTag;    // 女/男/通用
        private String ageRangeTag;  // 18-25/26-35等

        public String getGenderTag() { return genderTag; }
        public void setGenderTag(String genderTag) { this.genderTag = genderTag; }
        public String getAgeRangeTag() { return ageRangeTag; }
        public void setAgeRangeTag(String ageRangeTag) { this.ageRangeTag = ageRangeTag; }
    }

    /**
     * 行为-用户画像关联（统计用）
     */
    public static class BehaviorUserProfile {
        private String gender;    // 女/男
        private String ageRange;  // 18-25/26-35等
        private Integer weight;   // 行为加权分数（CLICK=1/COLLECT=3/BUY=5）

        public String getGender() { return gender; }
        public void setGender(String gender) { this.gender = gender; }
        public String getAgeRange() { return ageRange; }
        public void setAgeRange(String ageRange) { this.ageRange = ageRange; }
        public Integer getWeight() { return weight; }
        public void setWeight(Integer weight) { this.weight = weight; }
    }


}
