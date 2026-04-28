package com.zky.controller;

import com.github.pagehelper.PageInfo;
import com.zky.common.response.Response;
import com.zky.domain.dto.GroupBuyProductInfoRequestDTO;
import com.zky.domain.dto.ProductInfoRequestDTO;
import com.zky.domain.vo.GroupBuyProductVO;
import com.zky.service.IProductService;
import com.zky.domain.vo.ProductDetailVO;
import com.zky.domain.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/product")
public class ProductController {

    @Resource
    private IProductService productService;

    @GetMapping("/detail/{productId}")
    public Response<ProductDetailVO> getProductDetail(@PathVariable String productId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/product/detail/" + productId);
        ProductDetailVO vo = productService.getProductDetail(productId);
        return Response.<ProductDetailVO>builder().code("0000").info("Success").data(vo).build();
    }

    @PostMapping("/group_buy_detail")
    public Response<GroupBuyProductVO> getGroupBuyProductDetail(@RequestBody GroupBuyProductInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/product/group_buy_detail/" + request.getProductId());
        GroupBuyProductVO vo = productService.getGroupBuyProductDetail(request);
        return Response.<GroupBuyProductVO>builder().code("0000").info("Success").data(vo).build();
    }

    @PostMapping("/group_buy_progress")
    public Response<List<GroupBuyProductVO>> getGroupBuyProgress(@RequestParam String userId) {
        log.info("接口 {} 被调用了，参数：userId={}", "/api/v1/mall/product/group_buy_progress", userId);
        List<GroupBuyProductVO> progressList = productService.getGroupBuyProgress(userId);

        return Response.<List<GroupBuyProductVO>>builder()
                .code("0000")
                .info("Success")
                .data(progressList)
                .build();
    }

    @PostMapping("/list")
    public Response<PageInfo<ProductVO>> getProductList(@RequestBody ProductInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/product/list");
        PageInfo<ProductVO> pageInfo = productService.getProductList(request);
        return Response.<PageInfo<ProductVO>>builder().code("0000").info("Success").data(pageInfo).build();
    }
}
