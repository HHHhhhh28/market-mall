package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.github.pagehelper.PageInfo;
import com.zky.domain.vo.HomeProductVO;
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
    @Resource
    private UserDao userDao;

    @Resource
    private ProductDao productDao;


    @PostMapping("/mall-home")
    public Response<PageInfo<HomeProductVO>> mallHomeRecommend(@RequestBody RecommendationRequestDTO request) {
        log.info("首页商品推荐接口调用，请求参数：{}", request);
        PageInfo<HomeProductVO> result = recommendationService.getMallHomeRecommend(request);
        return Response.<PageInfo<HomeProductVO>>builder()
                .code("0000")
                .info("首页商品推荐成功")
                .data(result)
                .build();
    }


}
