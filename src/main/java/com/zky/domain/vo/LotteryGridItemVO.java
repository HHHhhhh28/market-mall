package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class LotteryGridItemVO {
    private String type;
    private String couponId;
    private String name;
    private String category;
    private String couponType;
    private BigDecimal value;
}

