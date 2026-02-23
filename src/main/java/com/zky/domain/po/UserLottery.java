package com.zky.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 用户抽奖次数与签到信息表，对应表：user_lottery
 */
@Data
public class UserLottery {

    /** 主键ID */
    private Long id;

    /** 业务用户ID，对应 user_id */
    private String userId;

    /** 可用抽奖次数，对应 lottery_count */
    private Integer lotteryCount;

    /** 最后签到日期，对应 last_sign_date */
    private Date lastSignDate;

    /** 创建时间，对应 create_time */
    private Date createTime;

    /** 更新时间，对应 update_time */
    private Date updateTime;
}
