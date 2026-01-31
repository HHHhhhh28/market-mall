package com.zky.service;

import com.github.pagehelper.PageInfo;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.zky.domain.vo.ProductVO;

public interface IRecommendationService {
    PageInfo<ProductVO> recommend(RecommendationRequestDTO request);
}
