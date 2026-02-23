package com.zky.service.impl;

import com.alibaba.nacos.api.remote.response.ResponseCode;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;

import java.util.ArrayList;
import java.util.Collections;

import com.zky.algorithm.RecommendationStrategy;
import com.zky.api.IMarketIndexService;
import com.zky.api.dto.GoodsMarketRequestDTO;
import com.zky.api.dto.GoodsMarketResponseDTO;
import com.zky.api.dto.ProductInfoDTO;
import com.zky.api.response.Response;
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

    @DubboReference(interfaceClass = IMarketIndexService.class, version = "1.0")
    private IMarketIndexService iMarketIndexService;

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

    private GroupBuyProductVO handleGroupBuyDubboV2(ProductInfo productInfo, String userId) {
        // 初始化返回对象，避免返回null
        GroupBuyProductVO groupBuyVO = new GroupBuyProductVO();

        // 1. 空值校验：如果商品信息为空，直接返回空的VO
        if (productInfo == null) {
            log.warn("处理单个拼团商品失败：商品信息为空");
            return groupBuyVO;
        }
        if (StringUtils.isBlank(productInfo.getProductId())) {
            log.warn("处理单个拼团商品失败：商品ID为空");
            return groupBuyVO;
        }

        try {
            // 2. 构建Dubbo入参（适配单个商品接口）
            GoodsMarketRequestDTO dubboRequest = new GoodsMarketRequestDTO();
            dubboRequest.setUserId(userId);
            dubboRequest.setSource("s01");
            dubboRequest.setChannel("c01");
            // 单个商品接口不使用goodsId和productList，置为null
            dubboRequest.setGoodsId(null);
            dubboRequest.setProductList(null);

            // 构建单个商品的ProductInfoDTO（核心：赋值给productInfoDTO字段）
            ProductInfoDTO productInfoDTO = new ProductInfoDTO();
            productInfoDTO.setProductId(productInfo.getProductId());
            productInfoDTO.setName(productInfo.getName()); // 注意字段匹配，若为productName需修改
            productInfoDTO.setPrice(productInfo.getPrice());
            dubboRequest.setProductInfoDTO(productInfoDTO);

            // 3. 调用单个商品的Dubbo接口（注意：接口名改为单个商品的方法名）
            Response<GoodsMarketResponseDTO> dubboResponse = iMarketIndexService.queryGroupBuyMarketTrialSingle(dubboRequest);

            // 4. 复制ProductInfo基础属性到VO
            BeanUtils.copyProperties(productInfo, groupBuyVO);

            // 5. 解析Dubbo响应，补充拼团专属属性
            if (dubboResponse != null && dubboResponse.getData() != null) {
                GoodsMarketResponseDTO dubboDTO = dubboResponse.getData();

                // 商品价格相关
                if (dubboDTO.getGoods() != null) {
                    groupBuyVO.setPayPrice(dubboDTO.getGoods().getPayPrice()); // 拼团支付价
                }
                // 活动ID
                groupBuyVO.setActivityId(dubboDTO.getActivityId());
                // 组队信息
                GoodsMarketResponseDTO.Team team = dubboDTO.getTeam();
                if (team != null) {
                    groupBuyVO.setUserId(team.getUserId());
                    groupBuyVO.setTeamId(team.getTeamId());
                    groupBuyVO.setTargetCount(team.getTargetCount());
                    groupBuyVO.setCompleteCount(team.getCompleteCount());
                    groupBuyVO.setLockCount(team.getLockCount());
                    groupBuyVO.setValidStartTime(team.getValidStartTime());
                    groupBuyVO.setValidEndTime(team.getValidEndTime());
                    groupBuyVO.setValidTimeCountdown(team.getValidTimeCountdown());
                    groupBuyVO.setActivityStartTime(team.getActivityStartTime());
                    groupBuyVO.setActivityEndTime(team.getActivityEndTime());
                    groupBuyVO.setOutTradeNo(team.getOutTradeNo());
                }
            } else {
                // Dubbo响应异常/无数据，打印日志
                log.warn("单个拼团商品Dubbo接口响应异常：productId={}, response={}",
                        productInfo.getProductId(), dubboResponse);
            }
        } catch (Exception e) {
            // 异常捕获，避免单个商品处理失败导致整体报错
            log.error("处理单个拼团商品异常：productId={}", productInfo.getProductId(), e);
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
        vo.setType("NORMAL");
        return vo;
    }
}
