package com.zky.service.impl;

import com.github.pagehelper.Page;
import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.github.pagehelper.PageInfo;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.service.IRecommendationService;
import com.zky.domain.vo.ProductVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class RecommendationServiceImpl implements IRecommendationService {

    private final Map<RecommendationType, RecommendationStrategy> strategyMap;

    @Resource
    private UserDao userDao;

    @Resource
    private ProductDao productDao;

    public RecommendationServiceImpl(List<RecommendationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(RecommendationStrategy::getType, Function.identity()));
    }

    @Override
    public PageInfo<ProductVO> recommend(RecommendationRequestDTO request) {
        if (request.getType() == null) {
            throw new IllegalArgumentException("Recommendation Type is required");
        }

        RecommendationStrategy strategy = strategyMap.get(request.getType());
        if (strategy == null) {
            throw new UnsupportedOperationException("Strategy not implemented for: " + request.getType());
        }

        UserInfo user = null;
        if (request.getUserId() != null) {
            user = userDao.selectByUserId(request.getUserId());
        }
        if (user == null) {
            user = new UserInfo();
            user.setAge(25);
            user.setGender(0);
        }
        //获取全量上架商品
        List<ProductInfo> candidates = getCandidates(request.getType());

        List<ProductInfo> recommendedEntities = strategy.recommend(user, candidates);

        int pageNum = request.getPageNum() > 0 ? request.getPageNum() : 1;
        int defaultSize = RecommendationType.GROUP_BUY.equals(request.getType()) ? 9 : 16;
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : defaultSize;

        // 核心改造：手动实现内存分页逻辑
        int total = recommendedEntities.size();
        int start = (pageNum - 1) * pageSize;
        
        List<ProductInfo> pagedList;
        if (start >= total) {
            pagedList = new ArrayList<>();
        } else {
            int end = Math.min(start + pageSize, total);
            pagedList = recommendedEntities.subList(start, end);
        }

        // 使用PageHelper.Page对象封装分页结果，确保PageInfo能正确解析total等参数
        Page<ProductInfo> page = new Page<>(pageNum, pageSize);
        page.setTotal(total);
        page.addAll(pagedList);
        
        // 一键生成完整的PageInfo（所有分页参数自动赋值）
        PageInfo<ProductInfo> pageInfoDO = new PageInfo<>(page);

        // DO-VO转换：基于分页后的结果集转换，无需手动处理subList
        List<ProductVO> productVOs = pageInfoDO.getList().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 复用DO层的分页属性，仅替换数据列表为VO（保留所有自动生成的分页参数）
        PageInfo<ProductVO> pageInfoVO = new PageInfo<>();
        BeanUtils.copyProperties(pageInfoDO, pageInfoVO);
        pageInfoVO.setList(productVOs);

        return pageInfoVO;
    }

    private List<ProductInfo> getCandidates(RecommendationType type) {
        List<ProductInfo> candidates;
        if (type == RecommendationType.MALL_HOME) {
            candidates = productDao.selectAll();
        } else {
            candidates = new ArrayList<>();
        }

        if (candidates == null || candidates.isEmpty()) {
            //若没有真实商品数据，则生成模拟数据
            return generateMockCandidates(type);
        }
        return candidates;
    }

    private List<ProductInfo> generateMockCandidates(RecommendationType type) {
        List<ProductInfo> products = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            ProductInfo p = new ProductInfo();
            p.setProductId("MOCK_" + type.getCode() + "_" + i);
            p.setName("Mock Product " + i + " for " + type.getCode());
            p.setPrice(new BigDecimal(10 + i * 5));
            p.setBrand(i % 2 == 0 ? "BrandA" : "BrandB");
            p.setCategory(i % 3 == 0 ? "Elec" : "Food");
            p.setKeywords(i % 3 == 0 ? "digital,phone" : "food,snack");
            products.add(p);
        }
        return products;
    }

    private ProductVO convertToVO(ProductInfo entity) {
        ProductVO vo = new ProductVO();
        vo.setProductId(entity.getProductId());
        vo.setName(entity.getName());
        vo.setPrice(entity.getPrice());
        vo.setDescription(entity.getDescription());
        vo.setType("NORMAL"); // Default
        vo.setImageUrl(entity.getImageUrl());
        vo.setCategory(entity.getCategory());
        return vo;
    }
}
