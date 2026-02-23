package com.zky.domain.vo;

import lombok.Data;

import java.util.List;

@Data
public class LotteryDrawResultVO {
    private List<LotteryGridItemVO> gridItems;
    private Integer hitIndex;
    private LotteryGridItemVO hitItem;
    private Integer remainingLotteryCount;
}

