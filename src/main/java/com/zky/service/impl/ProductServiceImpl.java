package com.zky.service.impl;

import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.ArrayList;
import java.util.Collections;

import com.zky.algorithm.RecommendationStrategy;
import com.zky.dao.GroupBuyActivityDao;
import com.zky.domain.po.GroupBuyActivity;
import com.zky.common.enums.RecommendationType;
import com.zky.common.constans.Constants;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.GroupBuyProductInfoRequestDTO;
import com.zky.domain.dto.ProductInfoRequestDTO;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.domain.vo.GroupBuyProductVO;
import com.zky.service.IProductService;
import com.zky.service.IRedisService;
import com.zky.domain.vo.ProductDetailVO;
import com.zky.domain.vo.ProductVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements IProductService {

    @Resource
    private ProductDao productDao;

    @Resource
    private OrderDao orderDao;

    @Resource
    private UserDao userDao;

    @Resource
    private List<RecommendationStrategy> strategies;

    @Resource
    private IRedisService redisService;

    @Resource
    private GroupBuyActivityDao groupBuyActivityDao;

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

    @Override
    public GroupBuyProductVO getGroupBuyProductDetail(GroupBuyProductInfoRequestDTO request) {
        ProductInfo productInfo = productDao.selectByProductId(request.getProductId());
        GroupBuyProductVO product = handleGroupBuyDubboV2(productInfo, request.getUserId());
        return product;
    }

    @Override
    public List<GroupBuyProductVO> getGroupBuyProgress(String userId) {
        // 1. 初始化返回列表，避免返回null
        List<GroupBuyProductVO> progressList = new ArrayList<>();

        // 2. 空值校验
        if (StringUtils.isBlank(userId)) {
            log.warn("查询拼团进度失败：用户ID为空");
            return progressList;
        }

        try {
            // 3. 获取该用户的所有拼团商品ID集合
            List<String> productIds = orderDao.selectProductIdsByUserId(userId);
            log.info("查询到用户{}的拼团商品ID列表：{}", userId, productIds);

            // 4. 遍历每个商品ID，组装拼团进度VO
            if (productIds != null && !productIds.isEmpty()) {
                for (String productId : productIds) {
                    // 空值跳过，避免无效调用
                    if (StringUtils.isBlank(productId)) {
                        log.warn("商品ID为空，跳过处理");
                        continue;
                    }

                    // 5. 查询商品基础信息
                    ProductInfo productInfo = productDao.selectByProductId(productId);
                    if (productInfo == null) {
                        log.warn("商品ID{}的基础信息为空，跳过处理", productId);
                        continue;
                    }

                    // 6. 调用已有方法组装拼团VO
                    GroupBuyProductVO groupBuyVO = handleGroupBuyDubboV2(productInfo, userId);
                    if (groupBuyVO != null) {
                        progressList.add(groupBuyVO);
                    }
                }
            } else {
                log.info("用户{}暂无拼团商品记录", userId);
            }
        } catch (Exception e) {
            // 捕获所有异常，避免单个商品处理失败导致整体接口报错
            log.error("查询用户{}拼团进度异常", userId, e);
        }

        return progressList;
    }

    /**
     * 根据单个商品查询本地拼团活动信息并组装GroupBuyProductVO（替代原Dubbo调用）
     */
    private GroupBuyProductVO handleGroupBuyDubboV2(ProductInfo productInfo, String userId) {
        GroupBuyProductVO groupBuyVO = new GroupBuyProductVO();
        if (productInfo == null) {
            log.warn("处理单个拼团商品失败：商品信息为空");
            return groupBuyVO;
        }
        if (StringUtils.isBlank(productInfo.getProductId())) {
            log.warn("处理单个拼团商品失败：商品ID为空");
            return groupBuyVO;
        }
        // 复制商品基础属性
        BeanUtils.copyProperties(productInfo, groupBuyVO);
        // 查询该商品当前进行中的拼团活动
        GroupBuyActivity activity = groupBuyActivityDao.selectActiveByProductId(productInfo.getProductId());
        if (activity != null) {
            groupBuyVO.setPayPrice(activity.getGroupBuyPrice());
            groupBuyVO.setActivityId(activity.getActivityId());
            groupBuyVO.setTargetCount(activity.getRequiredPeople());
            groupBuyVO.setActivityStartTime(activity.getStartTime());
            groupBuyVO.setActivityEndTime(activity.getEndTime());
        } else {
            log.info("商品{}暂无进行中的拼团活动", productInfo.getProductId());
        }
        return groupBuyVO;
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
        vo.setStock(entity.getStock());
        vo.setBrand(entity.getBrand());
        vo.setKeywords(entity.getKeywords());
        vo.setUserTags(entity.getUserTags());
        vo.setType("NORMAL");
        return vo;
    }
}
