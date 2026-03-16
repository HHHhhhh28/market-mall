package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼团活动配置实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyActivity {

    /** 主键ID */
    private Long id;

    /** 活动ID（业务唯一标识） */
    private String activityId;

    /** 关联商品ID */
    private String productId;

    /** 活动名称 */
    private String activityName;

    /** 拼团价格 */
    private BigDecimal groupBuyPrice;

    /** 原价（快照） */
    private BigDecimal originalPrice;

    /** 拼团所需人数 */
    private Integer requiredPeople;

    /** 拼团有效时长（小时），从第一人参团开始计时 */
    private Integer validDuration;

    /** 活动状态：1-进行中，0-已结束，2-未开始 */
    private Integer status;

    /** 活动开始时间 */
    private Date startTime;

    /** 活动结束时间 */
    private Date endTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
