package com.zky.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * 抽奖优惠券策略分析结果VO
 */
@Data
public class LotteryStrategyVO {
    // 优惠券基本信息
    private String couponId;
    private String couponName;
    private String category;
    private String couponType;   // DIRECT/FULL/DISCOUNT
    private BigDecimal couponValue;
    private BigDecimal minOrderAmount;
    private Integer status;      // 0-未上架,1-已上架,2-已下架
    private Integer isFallback;  // 1=保底兜底券，0=普通策略券
    private Integer couponStatus; // coupon表status：1可用 0禁用

    // 品类市场数据
    private BigDecimal avgOrderPrice;
    private BigDecimal categoryMargin;
    private Integer orderCount30d;
    private Integer userCount30d;
    private BigDecimal repeatBuyRate;

    // 算法评分输出
    private BigDecimal elasticityScore;
    private BigDecimal conversionLift;
    private BigDecimal volumeLift;
    private BigDecimal actualDiscount;
    private BigDecimal netProfitRate;
    private BigDecimal roiScore;
    private String roiLevel;

    // 商家可见关键指标
    private BigDecimal breakEvenDiscount;
    private BigDecimal expectedRevenueLift;
    private String recommendReason;

    // 用户行为数据
    private List<CategoryOrderUser> topBuyers;

    @Data
    public static class CategoryOrderUser {
        private String userId;
        private String username;
        private Integer orderCount;
        private BigDecimal totalSpent;
    }
}
