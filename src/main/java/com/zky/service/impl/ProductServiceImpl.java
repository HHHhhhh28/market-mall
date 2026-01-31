package com.zky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import java.util.Collections;
import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.common.constans.Constants;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.ProductInfoRequestDTO;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.service.IProductService;
import com.zky.service.IRedisService;
import com.zky.domain.vo.ProductDetailVO;
import com.zky.domain.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductDao productDao;

    @Resource
    private UserDao userDao;

    @Resource
    private List<RecommendationStrategy> strategies;

    @Resource
    private IRedisService redisService;

    @Override
    public ProductDetailVO getProductDetail(String productId) {
        String cacheKey = Constants.RedisKey.MALL_PRODUCT_KEY + productId;
        ProductInfo product = redisService.getValue(cacheKey);

        if (product == null) {
            product = productDao.selectByProductId(productId);
            if (product == null) {
                throw new RuntimeException("商品不存在");
            }
            // 缓存2小时
            redisService.setValue(cacheKey, product, TimeUnit.HOURS.toMillis(2));
        }
        return convertToDetailVO(product);
    }

    @Override
    public PageInfo<ProductVO> getProductList(ProductInfoRequestDTO request) {
        int pageNum = request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : 16;

        PageHelper.startPage(pageNum, pageSize);
        List<ProductInfo> products = productDao.selectList(request.getKeyword(), request.getCategory());
        PageInfo<ProductInfo> pageInfo = new PageInfo<>(products);
        List<ProductVO> voList = products.stream().map(this::convertToVO).collect(Collectors.toList());
        PageInfo<ProductVO> pageInfoVO = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, pageInfoVO);
        pageInfoVO.setList(voList);
        
        return pageInfoVO;
    }


    private ProductDetailVO convertToDetailVO(ProductInfo entity) {
        ProductDetailVO vo = new ProductDetailVO();
        vo.setProductId(entity.getProductId());
        vo.setName(entity.getName());
        vo.setDescription(entity.getDescription());
        vo.setPrice(entity.getPrice());
        vo.setStock(entity.getStock());
        vo.setImageUrl(entity.getImageUrl());
        vo.setCategory(entity.getCategory());
        vo.setBrand(entity.getBrand());
        return vo;
    }

    private ProductVO convertToVO(ProductInfo entity) {
        ProductVO vo = new ProductVO();
        vo.setProductId(entity.getProductId());
        vo.setName(entity.getName());
        vo.setPrice(entity.getPrice());
        vo.setImageUrl(entity.getImageUrl());
        vo.setDescription(entity.getDescription());
        vo.setCategory(entity.getCategory());
        vo.setType("NORMAL");
        return vo;
    }
}
