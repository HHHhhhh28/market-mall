package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 拼团商品策略分析结果 VO。
 *
 * <p>该对象主要用于后台“拼团价值评估”页面弹窗展示，包含三大部分信息：</p>
 * <ul>
 *     <li>商品基础信息：商品名称、图片、原价等。</li>
 *     <li>拼团策略分析结果：热度数据、推荐折扣、建议拼团价、算法说明等。</li>
 *     <li>高意向用户预测明细：基于行为强度、时间衰减、品类偏好、价格适配度等综合评估出的高概率下单用户。</li>
 * </ul>
 */
@Data
public class GroupBuyStrategyVO {

    /** 商品ID */
    private String productId;
    /** 商品名称 */
    private String productName;
    /** 商品图片 */
    private String imageUrl;
    /** 商品原价 */
    private BigDecimal originalPrice;

    // ===== 算法分析结果 =====
    /** 高意向用户总数（达到阈值后进入列表的人数） */
    private Integer interestedUsers;
    /** 近30天浏览次数 */
    private Integer viewCount;
    /** 近30天收藏次数 */
    private Integer favoriteCount;
    /** 近30天购买次数 */
    private Integer purchaseCount;
    /** 行为热度综合分 */
    private BigDecimal behaviorScore;
    /** 算法推荐折扣率 */
    private BigDecimal discountRate;
    /** 算法建议拼团价 */
    private BigDecimal suggestedGroupBuyPrice;
    /** 推送目标用户标签 */
    private String targetUserTags;
    /** 算法说明 */
    private String algorithmExplain;

    // ===== 高意向用户明细 =====
    /**
     * 高意向用户预测列表。
     *
     * <p>注意：这里不再是“只要点过/收藏过/买过就展示”，而是对候选用户进行综合评分后，
     * 仅展示达到阈值、且更可能在商品上架拼团后下单的用户。</p>
     */
    private List<InterestedUserVO> interestedUserList;

    // ===== 当前上架状态 =====
    /** 上架状态：0-未上架，1-已上架，2-已下架 */
    private Integer onlineStatus;
    /** 关联活动ID */
    private String activityId;

    /**
     * 高意向用户明细内部类。
     *
     * <p>该对象同时承载用户基础画像与评分模型输出，方便前端对“为什么推荐这个用户”进行可解释展示。</p>
     */
    @Data
    public static class InterestedUserVO {
        /** 用户ID */
        private String userId;
        /** 用户名 */
        private String username;
        /** 性别：0-女，1-男 */
        private Integer gender;
        /** 年龄 */
        private Integer age;
        /** 城市 */
        private String city;

        /** 综合意向分，分值越高，表示用户越可能在拼团上架后下单 */
        private BigDecimal intentScore;
        /** 预测下单概率（0~1） */
        private BigDecimal conversionProbability;
        /** 意向等级：极高意向 / 高意向 / 较高意向 */
        private String intentLevel;

        /** 意向得分详情 (用于前端展示具体分值来源) */
        private java.util.Map<String, BigDecimal> scoreDetails;

        /** 对当前商品的直接浏览次数 */
        private Integer directViewCount;
        /** 对当前商品的直接收藏次数 */
        private Integer directCollectCount;
        /** 对当前商品的直接购买次数 */
        private Integer directBuyCount;
        /** 对同品类商品的历史行为次数 */
        private Integer sameCategoryBehaviorCount;
        /** 对同品牌商品的历史行为次数 */
        private Integer sameBrandBehaviorCount;
        /** 历史订单均价，用于判断与本次拼团价是否匹配 */
        private BigDecimal avgOrderPrice;
        /** 历史订单数量 */
        private Integer orderCount;
        /** 历史拼团订单数量，用于衡量拼团敏感度 */
        private Integer groupBuyOrderCount;
        /** 最近一次活跃距今天数 */
        private Integer lastActiveDays;

        /** 品类偏好得分 */
        private BigDecimal categoryAffinityScore;
        /** 品牌偏好得分 */
        private BigDecimal brandAffinityScore;
        /** 价格适配得分 */
        private BigDecimal priceMatchScore;
        /** 拼团偏好得分 */
        private BigDecimal groupBuyAffinityScore;

        /** 评分拆解说明，便于前端展示“分数来源” */
        private String scoreBreakdown;
        /** 推荐摘要，用一句话描述该用户为何入选 */
        private String recommendSummary;
        /** 推荐原因标签列表，如：最近7天活跃、同品类偏好强、价格带匹配等 */
        private List<String> reasonTags;
    }

    /** 用户历史购买记录内部类 */
    @Data
    public static class UserPurchaseHistoryVO {
        /** 商品名称 */
        private String productName;
        /** 购买价格 */
        private BigDecimal price;
    }
}
