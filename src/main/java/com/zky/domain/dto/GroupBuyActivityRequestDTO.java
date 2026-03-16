package com.zky.domain.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼团活动创建/查询请求DTO（管理端使用）
 */
@Data
public class GroupBuyActivityRequestDTO {

    /** 活动ID（更新时使用） */
    private String activityId;

    /** 关联商品ID */
    private String productId;

    /** 活动名称 */
    private String activityName;

    /** 拼团价格 */
    private BigDecimal groupBuyPrice;

    /** 拼团所需人数 */
    private Integer requiredPeople;

    /** 拼团有效时长（小时） */
    private Integer validDuration;

    /** 活动开始时间 */
    private Date startTime;

    /** 活动结束时间 */
    private Date endTime;
}
