package com.zky.controller;

import com.github.pagehelper.PageInfo;
import com.zky.common.response.Response;
import com.zky.domain.dto.ProductInfoRequestDTO;
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

    @PostMapping("/list")
    public Response<PageInfo<ProductVO>> getProductList(@RequestBody ProductInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/product/list");
        PageInfo<ProductVO> pageInfo = productService.getProductList(request);
        return Response.<PageInfo<ProductVO>>builder().code("0000").info("Success").data(pageInfo).build();
    }
}
