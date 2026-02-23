package com.zky.service;

import com.github.pagehelper.PageInfo;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.zky.domain.vo.GroupBuyProductVO;
import com.zky.domain.vo.HomeProductVO;
import com.zky.domain.vo.LotteryProductVO;
import com.zky.domain.vo.ProductVO;

public interface IRecommendationService {

    // 拼团商品推荐
    PageInfo<GroupBuyProductVO> getGroupBuyRecommend(RecommendationRequestDTO request);
    // 首页商品推荐
    PageInfo<HomeProductVO> getMallHomeRecommend(RecommendationRequestDTO request);

}
