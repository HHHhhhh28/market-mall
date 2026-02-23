package com.zky.domain.po;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 平台优惠券配置表，对应表：coupon
 */
@Data
public class Coupon {

    /** 主键ID */
    private Long id;

    /** 业务优惠券ID，对应 coupon_id */
    private String couponId;

    /** 商品品类，对应 category */
    private String category;

    /** 优惠券类型：DIRECT/FULL/DISCOUNT，对应 coupon_type */
    private String couponType;

    /** 优惠值，对应 value */
    private BigDecimal value;

    /** 优惠券名称，对应 name */
    private String name;

    /** 状态：1可用 0禁用，对应 status */
    private Integer status;
}

