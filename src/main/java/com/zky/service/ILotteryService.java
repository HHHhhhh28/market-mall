package com.zky.service;

import com.zky.domain.vo.LotteryDrawResultVO;
import com.zky.domain.vo.LotteryGridItemVO;
import com.zky.domain.vo.LotteryInfoVO;

import java.util.List;

public interface ILotteryService {
    LotteryInfoVO getLotteryInfo(String userId);

    Integer signIn(String userId);

    LotteryDrawResultVO draw(String userId, List<LotteryGridItemVO> gridItems);

    List<LotteryGridItemVO> previewGrid(String userId);
}
