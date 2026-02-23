package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class UserCouponVO {
    private String couponId;
    private String name;
    private String category;
    private String couponType;
    private BigDecimal value;
    private Integer status;
}

