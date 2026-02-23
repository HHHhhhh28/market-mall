package com.zky.service.impl;

import com.github.pagehelper.Page;
import com.zky.algorithm.RecommendationStrategy;
import com.zky.api.IMarketIndexService;
import com.zky.api.dto.GoodsMarketRequestDTO;
import com.zky.api.dto.GoodsMarketResponseDTO;
import com.zky.api.dto.ProductInfoDTO;
import com.zky.api.response.Response;
import com.zky.common.enums.RecommendationType;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.RecommendationRequestDTO;
import com.github.pagehelper.PageInfo;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.domain.vo.GroupBuyProductVO;
import com.zky.domain.vo.HomeProductVO;
import com.zky.service.IRecommendationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
public class RecommendationServiceImpl implements IRecommendationService {

    private final Map<RecommendationType, RecommendationStrategy> strategyMap;

    @DubboReference(interfaceClass = IMarketIndexService.class,version = "1.0")
    private IMarketIndexService iMarketIndexService;

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

    // ====================== 重构 Dubbo 调用逻辑（直接返回 GroupBuyProductVO） ======================
    /**
     * 拼团商品专属：调用Dubbo接口并直接转换为 GroupBuyProductVO
     */
    private List<GroupBuyProductVO> handleGroupBuyDubboV2(List<ProductInfo> recommendedEntities, String userId) {
        List<GroupBuyProductVO> groupBuyVOList = new ArrayList<>();
        if (CollectionUtils.isEmpty(recommendedEntities)) {
            return groupBuyVOList;
        }

        // 1. 构建Dubbo入参
        GoodsMarketRequestDTO dubboRequest = new GoodsMarketRequestDTO();
        dubboRequest.setUserId(userId);
        dubboRequest.setSource("group_buy_market");
        dubboRequest.setChannel("recommend");
        dubboRequest.setGoodsId(null);

        List<ProductInfoDTO> productInfoDTOList = recommendedEntities.stream()
                .map(productInfo -> {
                    ProductInfoDTO dto = new ProductInfoDTO();
                    dto.setProductId(productInfo.getProductId());
                    dto.setName(productInfo.getName()); // 注意：ProductInfo的name字段需和DTO匹配，若为productName需修改
                    dto.setPrice(productInfo.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());
        dubboRequest.setProductList(productInfoDTOList);

        // 2. 调用Dubbo接口
        Response<List<GoodsMarketResponseDTO>> dubboResponse = iMarketIndexService.queryGroupBuyMarketTrial(dubboRequest);

        // 3. 解析Dubbo响应，构建商品ID映射（核心修复：声明为final）
        final Map<String, GoodsMarketResponseDTO> productDubboMap = new HashMap<>(); // 关键：加final
        if (dubboResponse != null && dubboResponse.getData() != null) {
            // 仅向map中put数据，不重新赋值map本身
            dubboResponse.getData().stream()
                    .filter(dto -> dto.getGoods() != null)
                    .forEach(dto -> productDubboMap.put(
                            dto.getGoods().getGoodsId(),
                            dto
                    ));
            // 替代原Collectors.toMap，避免重新赋值map，彻底解决effectively final问题
        }

        // 4. ProductInfo + Dubbo数据 → GroupBuyProductVO
        groupBuyVOList = recommendedEntities.stream()
                .map(productInfo -> {
                    GroupBuyProductVO vo = new GroupBuyProductVO();
                    // 4.1 复制ProductInfo基础属性
                    BeanUtils.copyProperties(productInfo, vo);
                    // 4.2 补充Dubbo返回的拼团专属属性
                    GoodsMarketResponseDTO dubboDTO = productDubboMap.get(productInfo.getProductId()); // 现在无爆红
                    if (dubboDTO != null) {
                        // 商品价格相关
                        if (dubboDTO.getGoods() != null) {
                            vo.setPayPrice(dubboDTO.getGoods().getPayPrice()); // 拼团支付价
                        }
                        // 活动ID
                        vo.setActivityId(dubboDTO.getActivityId());
                        // 组队信息（取单商品组队信息）
                        GoodsMarketResponseDTO.Team team = dubboDTO.getTeam();
                        if (team != null) {
                            vo.setUserId(team.getUserId());
                            vo.setTeamId(team.getTeamId());
                            vo.setTargetCount(team.getTargetCount());
                            vo.setCompleteCount(team.getCompleteCount());
                            vo.setLockCount(team.getLockCount());
                            vo.setValidStartTime(team.getValidStartTime());
                            vo.setValidEndTime(team.getValidEndTime());
                            vo.setValidTimeCountdown(team.getValidTimeCountdown());
                            vo.setActivityStartTime(team.getActivityStartTime());
                            vo.setActivityEndTime(team.getActivityEndTime());
                            vo.setOutTradeNo(team.getOutTradeNo());
                        }
                    }
                    return vo;
                })
                .collect(Collectors.toList());

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
