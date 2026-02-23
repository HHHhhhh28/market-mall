package com.zky.domain.po;

import lombok.Data;

import java.util.Date;

/**
 * 用户优惠券持有记录表，对应表：user_coupon
 */
@Data
public class UserCoupon {

    /** 主键ID */
    private Long id;

    /** 用户ID，对应 user_id */
    private String userId;

    /** 优惠券ID，对应 coupon_id */
    private String couponId;

    /** 优惠券品类，对应 category */
    private String category;

    /** 使用状态：0未使用 1已使用，对应 status */
    private Integer status;

    /** 领取时间，对应 create_time */
    private Date createTime;
}

