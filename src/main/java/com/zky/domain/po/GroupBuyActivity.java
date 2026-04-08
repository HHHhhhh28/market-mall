package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 全局拼团活动配置实体（公共规则，不绑定具体商品）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyActivity {

    /** 主键ID */
    private Long id;

    /** 活动ID（业务唯一标识，UUID） */
    private String activityId;

    /** 拼团所需人数（最少2人） */
    private Integer requiredPeople;

    /** 拼团有效时长（小时），从第一人开团开始计时 */
    private Integer validDuration;

    /** 活动状态：1-进行中，0-已结束 */
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
