package com.zky.domain.vo;

import com.zky.common.enums.RecommendationType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author ：zky
 * @description：
 * @date ：2026/2/14 09:58
 */
@Data
public class GroupBuyProductVO {
    private Long id;
    private String productId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String brand;
    private String keywords;
    private Date createTime;
    private Date updateTime;
    private String userTags;

    private BigDecimal payPrice;


    // 用户ID
    private String userId;
    // 拼单组队ID
    private String teamId;
    // 活动ID（本地拼团模块使用字符串UUID）
    private String activityId;
    // 目标数量
    private Integer targetCount;
    // 完成数量
    private Integer completeCount;
    // 锁单数量
    private Integer lockCount;
    // 拼团开始时间 - 参与拼团时间
    private Date validStartTime;
    // 拼团结束时间 - 拼团有效时长
    private Date validEndTime;
    // 倒计时(字符串) validEndTime - validStartTime
    private String validTimeCountdown;
    /** 活动开始时间 */
    private Date activityStartTime;
    /** 活动结束时间 */
    private Date activityEndTime;
    /** 外部交易单号-确保外部调用唯一幂等 */
    private String outTradeNo;

}
