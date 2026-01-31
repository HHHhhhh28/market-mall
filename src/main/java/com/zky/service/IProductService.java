package com.zky.service;

import com.github.pagehelper.PageInfo;
import com.zky.domain.dto.ProductInfoRequestDTO;
import com.zky.domain.vo.ProductDetailVO;
import com.zky.domain.vo.ProductVO;

import java.util.List;

public interface IProductService {
    /**
     * 获取商品详情
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductDetailVO getProductDetail(String productId);
    PageInfo<ProductVO> getProductList(ProductInfoRequestDTO request);
}
