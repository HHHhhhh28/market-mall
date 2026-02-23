package com.zky.domain.dto;

import com.zky.domain.vo.LotteryGridItemVO;
import lombok.Data;

import java.util.List;

@Data
public class LotteryDrawRequestDTO {
    private String userId;
    private List<LotteryGridItemVO> gridItems;
}
