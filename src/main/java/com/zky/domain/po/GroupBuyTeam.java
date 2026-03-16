package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼团团队实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyTeam {

    /** 主键ID */
    private Long id;

    /** 团队ID（业务唯一标识） */
    private String teamId;

    /** 关联活动ID */
    private String activityId;

    /** 关联商品ID */
    private String productId;

    /** 团长用户ID */
    private String leaderUserId;

    /** 拼团所需人数（从活动配置冗余） */
    private Integer requiredPeople;

    /** 当前已参团人数 */
    private Integer currentPeople;

    /** 团队状态：0-拼团中，1-拼团成功，2-拼团失败（超时未满） */
    private Integer status;

    /** 本次拼团价格（快照） */
    private BigDecimal groupBuyPrice;

    /** 开团时间（第一人参团时间） */
    private Date startTime;

    /** 拼团截止时间（start_time + valid_duration小时） */
    private Date endTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
