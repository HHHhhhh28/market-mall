package com.zky.domain.vo;

import lombok.Data;

@Data
public class LotteryInfoVO {
    private String userId;
    private Integer lotteryCount;
    private Boolean signedToday;
}

