package com.zky.service.impl;

import com.github.pagehelper.PageInfo;
import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.dao.GroupBuyActivityDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.zky.domain.po.GroupBuyActivity;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.domain.vo.GroupBuyProductVO;
import com.zky.domain.vo.HomeProductVO;
import com.zky.service.IRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
public class RecommendationServiceImpl implements IRecommendationService {

    private final Map<RecommendationType, RecommendationStrategy> strategyMap;

    @Resource
    private GroupBuyActivityDao groupBuyActivityDao;

    @Resource
    private UserDao userDao;

    @Resource
    private ProductDao productDao;

    public RecommendationServiceImpl(List<RecommendationStrategy> strategies) {
        this.strategyMap = strategies.stream()
                .collect(Collectors.toMap(RecommendationStrategy::getType, Function.identity()));
    }

    // ====================== 拼团商品推荐（适配 GroupBuyProductVO） ======================
    @Override
    public PageInfo<GroupBuyProductVO> getGroupBuyRecommend(RecommendationRequestDTO request) {
        // 1. 入参校验 & 类型强制赋值（避免传错类型）
        request.setType(RecommendationType.GROUP_BUY);
        validateRequest(request);

        // 2. 获取推荐策略
        RecommendationStrategy strategy = getStrategy(request.getType());

        // 3. 获取用户信息（兜底默认值）
        UserInfo user = getUserInfo(request.getUserId());

        // 4. 查询候选商品 + 策略推荐
        List<ProductInfo> candidates = getCandidates(request.getType());
        List<ProductInfo> recommendedEntities = strategy.recommend(user, candidates);

        // 5. 拼团商品专属：调用Dubbo获取完整拼团信息
        List<GroupBuyProductVO> groupBuyProductVOList = handleGroupBuyDubboV2(recommendedEntities, request.getUserId());

        // 6. 分页处理（拼团默认页大小9）
        PageInfo<GroupBuyProductVO> pageInfo = handleGroupBuyPagination(groupBuyProductVOList, request, 9);

        return pageInfo;
    }

    // ====================== 本地拼团活动数据查询（替代原Dubbo调用） ======================
    /**
     * 根据推荐商品列表，查询本地拼团活动信息并组装GroupBuyProductVO
     */
    private List<GroupBuyProductVO> handleGroupBuyDubboV2(List<ProductInfo> recommendedEntities, String userId) {
        List<GroupBuyProductVO> groupBuyVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(recommendedEntities)) {
            return groupBuyVOList;
        }
        for (ProductInfo productInfo : recommendedEntities) {
            GroupBuyProductVO vo = new GroupBuyProductVO();
            // 复制商品基础属性
            BeanUtils.copyProperties(productInfo, vo);
            // 查询该商品当前进行中的拼团活动
            GroupBuyActivity activity = groupBuyActivityDao.selectActiveByProductId(productInfo.getProductId());
            if (activity != null) {
                // 补充拼团专属字段
                vo.setPayPrice(activity.getGroupBuyPrice());
                vo.setActivityId(activity.getActivityId());
                vo.setTargetCount(activity.getRequiredPeople());
                vo.setActivityStartTime(activity.getStartTime());
                vo.setActivityEndTime(activity.getEndTime());
            }
            groupBuyVOList.add(vo);
        }
        return groupBuyVOList;
    }

    // ====================== 拼团商品专属分页方法 ======================
    /**
     * 基于 GroupBuyProductVO 分页
     */
    private PageInfo<GroupBuyProductVO> handleGroupBuyPagination(List<GroupBuyProductVO> dataList,
                                                                 RecommendationRequestDTO request,
                                                                 int defaultPageSize) {
        // 页码兜底：小于1则置为1
        int pageNum = request.getPageNum() > 0 ? request.getPageNum() : 1;
        // 页大小兜底：小于1则用默认值9
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : defaultPageSize;
        int total = dataList.size();

        // 计算分页索引
        int start = (pageNum - 1) * pageSize;
        List<GroupBuyProductVO> pagedList = new ArrayList<>();
        if (start < total) {
            int end = Math.min(start + pageSize, total);
            pagedList = dataList.subList(start, end);
        }

        // 封装分页对象
        PageInfo<GroupBuyProductVO> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setList(pagedList);
        return pageInfo;
    }

    // ====================== 保留原有通用方法 ======================
    private void validateRequest(RecommendationRequestDTO request) {
        if (request.getType() == null) {
            throw new IllegalArgumentException("Recommendation Type is required");
        }
    }

    private RecommendationStrategy getStrategy(RecommendationType type) {
        RecommendationStrategy strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new UnsupportedOperationException("Strategy not implemented for: " + type);
        }
        return strategy;
    }

    private UserInfo getUserInfo(String userId) {
        UserInfo user = null;
        if (userId != null) {
            user = userDao.selectByUserId(userId);
        }
        if (user == null) {
            user = new UserInfo();
            user.setAge(25);
            user.setGender(0);
        }
        return user;
    }

    private List<ProductInfo> getCandidates(RecommendationType type) {
        List<ProductInfo> candidates;
        candidates = productDao.selectAll();
        return candidates;
    }

    // ====================== 首页商品推荐（适配 HomeProductVO） ======================
    @Override
    public PageInfo<HomeProductVO> getMallHomeRecommend(RecommendationRequestDTO request) {
        // 1. 入参校验 & 类型强制赋值
        request.setType(RecommendationType.MALL_HOME);
        validateRequest(request);

        // 2. 获取推荐策略
        RecommendationStrategy strategy = getStrategy(request.getType());

        // 3. 获取用户信息（兜底默认值）
        UserInfo user = getUserInfo(request.getUserId());

        // 4. 查询候选商品 + 策略推荐
        List<ProductInfo> candidates = getCandidates(request.getType());
        List<ProductInfo> recommendedEntities = strategy.recommend(user, candidates);

        // 5. 直接转换为 HomeProductVO（移除中间VO转换）
        List<HomeProductVO> homeProductVOList = convertToHomeProductVO(recommendedEntities);

        // 6. 分页处理（首页默认页大小16）
        PageInfo<HomeProductVO> pageInfo = handleHomeProductPagination(homeProductVOList, request, 16);

        // 7. 补充 type 字段（首页商品类型为 NORMAL）
        pageInfo.getList().forEach(vo -> vo.setType("NORMAL"));

        return pageInfo;
    }

    // ====================== 新增/修改 私有方法 ======================
    /**
     * ProductInfo 直接转换为 HomeProductVO
     */
    private List<HomeProductVO> convertToHomeProductVO(List<ProductInfo> recommendedEntities) {
        if (CollectionUtils.isEmpty(recommendedEntities)) {
            return new ArrayList<>();
        }
        return recommendedEntities.stream()
                .map(productInfo -> {
                    HomeProductVO vo = new HomeProductVO();
                    // 复制属性（匹配字段名：productId/name/imageUrl/price 等）
                    BeanUtils.copyProperties(productInfo, vo);
                    // 补充默认值（首页商品无拼团属性，置空）
                    vo.setPayPrice(null);
                    vo.setActivityId(null);
                    return vo;
                })
                .collect(Collectors.toList());
    }

    /**
     * 首页商品专属分页处理（直接基于 HomeProductVO 分页）
     */
    private PageInfo<HomeProductVO> handleHomeProductPagination(List<HomeProductVO> dataList,
                                                                RecommendationRequestDTO request,
                                                                int defaultPageSize) {
        int pageNum = request.getPageNum() > 0 ? request.getPageNum() : 1;
        int pageSize = request.getPageSize() > 0 ? request.getPageSize() : defaultPageSize;
        int total = dataList.size();

        // 计算分页索引
        int start = (pageNum - 1) * pageSize;
        List<HomeProductVO> pagedList = new ArrayList<>();
        if (start < total) {
            int end = Math.min(start + pageSize, total);
            pagedList = dataList.subList(start, end);
        }

        // 封装分页对象
        PageInfo<HomeProductVO> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pageNum);
        pageInfo.setPageSize(pageSize);
        pageInfo.setTotal(total);
        pageInfo.setList(pagedList);
        return pageInfo;
    }
}
