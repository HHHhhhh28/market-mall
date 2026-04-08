package com.zky.domain.po;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 抽奖优惠券策略表 PO
 */
@Data
public class LotteryCouponStrategy {
    private Long id;
    private String strategyId;
    private String couponId;
    private String category;
    private String couponType;
    private BigDecimal couponValue;
    private BigDecimal minOrderAmount;
    // 算法输入
    private BigDecimal avgOrderPrice;
    private BigDecimal categoryMargin;
    private Integer orderCount30d;
    private Integer userCount30d;
    private BigDecimal repeatBuyRate;
    // 算法输出
    private BigDecimal elasticityScore;
    private BigDecimal conversionLift;
    private BigDecimal volumeLift;
    private BigDecimal actualDiscount;
    private BigDecimal netProfitRate;
    private BigDecimal roiScore;
    private BigDecimal breakEvenDiscount; // 保本让利上限
    private String recommendReason;
    // 状态
    private Integer status; // 0-未上架,1-已上架,2-已下架
    private Integer isFallback; // 1=保底券
    private Date createTime;
    private Date updateTime;
}
