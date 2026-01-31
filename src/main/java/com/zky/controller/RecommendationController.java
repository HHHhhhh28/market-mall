package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.github.pagehelper.PageInfo;
import com.zky.domain.vo.ProductVO;
import com.zky.service.IRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/recommend")
public class RecommendationController {

    @Resource
    private IRecommendationService recommendationService;

    @PostMapping("/list")
    public Response<PageInfo<ProductVO>> getRecommendations(@RequestBody RecommendationRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/recommend/list");
        PageInfo<ProductVO> response = recommendationService.recommend(request);
        return Response.<PageInfo<ProductVO>>builder()
                .code("0000")
                .info("Success")
                .data(response)
                .build();
    }
}
