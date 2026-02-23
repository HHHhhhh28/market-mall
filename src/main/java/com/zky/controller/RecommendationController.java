package com.zky.controller;

import com.zky.algorithm.impl.UserCFModel;
import com.zky.common.response.Response;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.github.pagehelper.PageInfo;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.domain.vo.GroupBuyProductVO;
import com.zky.domain.vo.HomeProductVO;
import com.zky.domain.vo.ProductVO;
import com.zky.service.IRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/recommend")
public class RecommendationController {

    @Resource
    private IRecommendationService recommendationService;
    @Resource
    private UserCFModel userCFModel;
    @Resource
    private UserDao userDao;

    @Resource
    private ProductDao productDao;


    @PostMapping("/group-buy")
    public Response<PageInfo<GroupBuyProductVO>> groupBuyRecommend(@RequestBody RecommendationRequestDTO request) {
        log.info("拼团商品推荐接口调用，请求参数：{}", request);
        PageInfo<GroupBuyProductVO> result = recommendationService.getGroupBuyRecommend(request);
        return Response.<PageInfo<GroupBuyProductVO>>builder()
                .code("0000")
                .info("拼团商品推荐成功")
                .data(result)
                .build();
    }


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

    @PostMapping("/lottery")
    public Response<List<ProductInfo>> lotteryRecommend(@RequestBody RecommendationRequestDTO request) {
        UserInfo user = userDao.selectByUserId(request.getUserId());

        // 4. 查询候选商品 + 策略推荐
        List<ProductInfo> candidates = productDao.selectAll();
        List<ProductInfo> recommend = userCFModel.recommend(user, candidates);
        return Response.<List<ProductInfo>>builder()
                .code("0000")
                .info("抽奖商品推荐成功")
                .data(recommend)
                .build();
    }

}
