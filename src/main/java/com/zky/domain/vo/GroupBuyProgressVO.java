package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户拼团进度VO（用于我的拼团进度页）
 */
@Data
public class GroupBuyProgressVO {

    /** 团队ID */
    private String teamId;

    /** 活动ID */
    private String activityId;

    /** 商品ID */
    private String productId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String imageUrl;

    /** 拼团价格 */
    private BigDecimal groupBuyPrice;

    /** 拼团所需人数 */
    private Integer requiredPeople;

    /** 当前已参团人数 */
    private Integer currentPeople;

    /** 还差几人成团 */
    private Integer remainingPeople;

    /**
     * 团队状态：0-拼团中，1-拼团成功，2-拼团失败
     */
    private Integer status;

    /** 拼团截止时间 */
    private Date endTime;

    /** 倒计时描述 */
    private String countdownDesc;

    /** 是否为团长 */
    private Integer isLeader;

    /** 关联订单ID */
    private String orderId;
}
