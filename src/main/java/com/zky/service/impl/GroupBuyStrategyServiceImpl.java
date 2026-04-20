package com.zky.service.impl;

import com.zky.dao.*;
import com.zky.domain.po.*;
import com.zky.domain.vo.GroupBuyStrategyVO;
import com.zky.service.IGroupBuyStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 拼团商品智能定价策略服务
 *
 * ========== 核心算法说明 ==========
 *
 * 目标：找到一个「商家可接受 & 用户有动力拼团」的最优价格区间
 *
 * 一、商家底线（最低可接受价格）
 *   根据品类预估毛利率，计算商家保本价：
 *   保本价 = 原价 × (1 - 品类毛利率) / (1 - 最低毛利容忍率)
 *   即：允许让利到毛利率压缩至 MIN_MARGIN 时的价格
 *
 * 二、用户价格弹性评分（0~100分）
 *   衡量「用户对这个商品多感兴趣、愿意为优惠行动」：
 *   - 转化漏斗得分：浏览→收藏→购买 各阶段转化比例
 *   - 行为趋势得分：近7天行为 vs 近30天行为的趋势
 *   - 用户广度得分：感兴趣用户数量（去重）
 *   - 价格敏感度得分：收藏/浏览比（高收藏低购买=价格敏感）
 *
 * 三、最终定价公式
 *   弹性折扣 = 高弹性商品给更低折扣（更大让利吸引成团）
 *            + 低弹性商品给较高折扣（减少商家损失）
 *   最终价格 = max(保本价, 原价 × 弹性折扣)
 *   并向上取整到整数或0.5元，心理定价更美观
 */
@Slf4j
@Service
public class GroupBuyStrategyServiceImpl implements IGroupBuyStrategyService {

    /** 行为数据统计天数：近30天 */
    private static final int BEHAVIOR_DAYS = 30;
    /** 趋势分析天数：近7天 */
    private static final int TREND_DAYS = 7;

    /** 品类基准利润率配置 (品类 -> 平均毛利率) */
    private static final Map<String, Double> CATEGORY_MARGIN = new HashMap<>();
    static {
        CATEGORY_MARGIN.put("3C数码",     0.15); CATEGORY_MARGIN.put("电竞外设",   0.20);
        CATEGORY_MARGIN.put("家用电器",   0.18); CATEGORY_MARGIN.put("办公设备",   0.20);
        CATEGORY_MARGIN.put("男士腕表",   0.35); CATEGORY_MARGIN.put("轻奢首饰",   0.40);
        CATEGORY_MARGIN.put("美妆护肤",   0.45); CATEGORY_MARGIN.put("香氛香水",   0.50);
        CATEGORY_MARGIN.put("美甲彩妆",   0.45); CATEGORY_MARGIN.put("化妆工具",   0.40);
        CATEGORY_MARGIN.put("内衣睡衣",   0.45); CATEGORY_MARGIN.put("网红配饰",   0.50);
        CATEGORY_MARGIN.put("母婴用品",   0.30); CATEGORY_MARGIN.put("代餐轻食",   0.40);
        CATEGORY_MARGIN.put("户外装备",   0.30); CATEGORY_MARGIN.put("骑行装备",   0.28);
        CATEGORY_MARGIN.put("运动护具",   0.30); CATEGORY_MARGIN.put("垂钓装备",   0.30);
        CATEGORY_MARGIN.put("汽车用品",   0.35); CATEGORY_MARGIN.put("五金工具",   0.25);
        CATEGORY_MARGIN.put("机械器材",   0.20); CATEGORY_MARGIN.put("商务服饰",   0.40);
        CATEGORY_MARGIN.put("布艺家纺",   0.40); CATEGORY_MARGIN.put("家居花艺",   0.45);
        CATEGORY_MARGIN.put("收纳整理",   0.35); CATEGORY_MARGIN.put("厨房用具",   0.35);
        CATEGORY_MARGIN.put("餐具茶具",   0.40); CATEGORY_MARGIN.put("酒具酒品",   0.35);
        CATEGORY_MARGIN.put("粮油米面",   0.15); CATEGORY_MARGIN.put("洗护清洁",   0.30);
        CATEGORY_MARGIN.put("饮用水饮",   0.25); CATEGORY_MARGIN.put("日用百货",   0.30);
    }

    /** 商家拼团最低毛利容忍率：5% */
    private static final double MIN_MARGIN = 0.05;
    /** 用户感知优惠最低折扣上限：9.5折 */
    private static final double MAX_USER_DISCOUNT = 0.95;
    /** 绝对最低折扣（防止严重亏损）：6.0折 */
    private static final double ABS_MIN_DISCOUNT = 0.60;

    @Resource private UserBehaviorDao userBehaviorDao;
    @Resource private FavoriteDao favoriteDao;
    @Resource private ProductDao productDao;
    @Resource private GroupBuyActivityDao groupBuyActivityDao;
    @Resource private GroupBuyProductDao groupBuyProductDao;
    @Resource private UserDao userDao;
    @Resource private OrderDao orderDao;
    @Resource private CartDao cartDao;

    /**
     * 分析商品价值并生成拼团策略
     */
    @Override
    public GroupBuyStrategyVO analyzeProduct(String productId, String activityId) {
        log.info("========== 拼团策略算法开始执行 ==========");
        log.info("输入参数：productId={}, activityId={}", productId, activityId);

        // 1. 获取商品基础信息
        ProductInfo product = productDao.selectByProductId(productId);
        if (product == null) throw new RuntimeException("商品不存在：" + productId);
        log.info("【1/17】获取商品基础信息完成：商品ID={}, 商品名称={}, 商品原价={}, 商品品类={}",
                product.getProductId(), product.getName(), product.getPrice(), product.getCategory());

        // 2. 获取活动信息
        GroupBuyActivity activity = groupBuyActivityDao.selectByActivityId(activityId);
        if (activity == null) throw new RuntimeException("活动不存在：" + activityId);
        log.info("【2/17】获取活动信息完成：活动ID={}, 活动时间={}~{}",
                activity.getActivityId(), activity.getStartTime(), activity.getEndTime());

        // 3. 统计用户行为数据
        BehaviorStats stats = calcBehaviorStats(productId);
        log.info("【3/17】用户行为数据统计完成：近30天浏览={}, 收藏={}, 购买={}, 近7天浏览={}, 购买={}, 感兴趣用户数={}",
                stats.viewCount, stats.favoriteCount, stats.purchaseCount,
                stats.viewCount7d, stats.purchaseCount7d, stats.interestedUserIds.size());

        // 4. 计算热度综合分：浏览(1) + 收藏(3) + 购买(5)
        BigDecimal behaviorScore = BigDecimal.valueOf(
                stats.viewCount * 1.0 + stats.favoriteCount * 3.0 + stats.purchaseCount * 5.0
        ).setScale(2, RoundingMode.HALF_UP);
        log.info("【4/17】热度综合分计算完成：behaviorScore={}", behaviorScore);

        // 5. 计算用户价格弹性评分 (0~100)
        ElasticityResult elasticity = calcElasticity(stats);
        log.info("【5/17】用户价格弹性评分计算完成：综合分={}/100, 转化漏斗={}/30, 行为趋势={}/25, 用户广度={}/25, 价格敏感={}/20, 弹性等级={}",
                elasticity.score, elasticity.funnelScore, elasticity.trendScore,
                elasticity.breadthScore, elasticity.sensitivityScore, elasticity.level);

        // 6. 获取品类基准毛利率
        double categoryMargin = CATEGORY_MARGIN.getOrDefault(
                product.getCategory() != null ? product.getCategory() : "", 0.25);
        log.info("【6/17】获取品类基准毛利率完成：categoryMargin={}%", categoryMargin * 100);

        // 7. 计算商家保本折扣下限
        double cost = product.getPrice().doubleValue() * (1 - categoryMargin);
        double floorPrice = cost / (1 - MIN_MARGIN);
        double floorDiscount = floorPrice / product.getPrice().doubleValue();
        floorDiscount = Math.max(floorDiscount, ABS_MIN_DISCOUNT);
        floorDiscount = Math.min(floorDiscount, 0.999);
        log.info("【7/17】商家保本折扣下限计算完成：商品成本价={}, 保本价={}, 保本折扣={}折",
                cost, floorPrice, floorDiscount * 10);

        // 8. 根据弹性评分计算目标折扣
        double targetDiscount = calcTargetDiscount(elasticity.score, floorDiscount);
        log.info("【8/17】根据弹性评分计算目标折扣完成：targetDiscount={}折", targetDiscount * 10);

        // 9. 确保折扣在用户感知范围内
        targetDiscount = Math.min(targetDiscount, MAX_USER_DISCOUNT);
        log.info("【9/17】折扣范围约束完成（用户感知上限）：targetDiscount={}折", targetDiscount * 10);

        // 10. 确定最终折扣（不低于保本线）
        double finalDiscount = Math.max(floorDiscount, Math.min(targetDiscount, MAX_USER_DISCOUNT));
        BigDecimal discountRate = BigDecimal.valueOf(finalDiscount).setScale(4, RoundingMode.HALF_UP);
        log.info("【10/17】确定最终折扣完成：finalDiscount={}折, discountRate={}",
                finalDiscount * 10, discountRate);

        // 11. 执行心理定价策略
        double rawPrice = product.getPrice().doubleValue() * finalDiscount;
        double suggestedPriceDouble = psychoPrice(rawPrice);
        BigDecimal suggestedPrice = BigDecimal.valueOf(suggestedPriceDouble).setScale(2, RoundingMode.HALF_UP);
        log.info("【11/17】心理定价策略执行完成：原始计算价={}, 心理定价优化后={}", rawPrice, suggestedPrice);

        // 12. 更新最终折扣率
        discountRate = suggestedPrice.divide(product.getPrice(), 4, RoundingMode.HALF_UP);
        log.info("【12/17】更新最终折扣率完成：discountRate={}", discountRate);

        // 13. 生成推送标签
        String targetTags = buildTargetTags(product, stats, elasticity);
        log.info("【13/17】生成推送标签完成：targetTags={}", targetTags);

        // 14. 构建高意向用户明细 (应用升级后的多维意向分模型)
        List<GroupBuyStrategyVO.InterestedUserVO> interestedUserList = buildInterestedUserList(productId);
        log.info("【14/17】构建高意向用户明细完成：筛选出高意向用户数={}", interestedUserList.size());
        for (int i = 0; i < Math.min(5, interestedUserList.size()); i++) {
            GroupBuyStrategyVO.InterestedUserVO user = interestedUserList.get(i);
            log.info("  高意向用户TOP{}：userId={}, 用户名={}, 意向分={}, 意向等级={}",
                    i+1, user.getUserId(), user.getUsername(), user.getIntentScore(), user.getIntentLevel());
        }

        // 15. 生成算法分析说明文本
        String explain = buildExplain(stats, elasticity, discountRate, floorDiscount,
                categoryMargin, suggestedPrice, product.getPrice(), product.getCategory() != null ? product.getCategory() : "未知品类");
        log.info("【15/17】生成算法分析说明文本完成");

        // 16. 获取当前上架状态
        GroupBuyProduct existing = groupBuyProductDao.selectByActivityAndProduct(activityId, productId);
        int onlineStatus = existing != null ? existing.getStatus() : 0;
        log.info("【16/17】获取当前上架状态完成：onlineStatus={}", onlineStatus);

        // 17. 组装返回 VO
        GroupBuyStrategyVO vo = new GroupBuyStrategyVO();
        vo.setProductId(productId);
        vo.setProductName(product.getName());
        vo.setImageUrl(product.getImageUrl());
        vo.setOriginalPrice(product.getPrice());
        vo.setInterestedUsers(interestedUserList.size());
        vo.setViewCount(stats.viewCount);
        vo.setFavoriteCount(stats.favoriteCount);
        vo.setPurchaseCount(stats.purchaseCount);
        vo.setBehaviorScore(behaviorScore);
        vo.setDiscountRate(discountRate);
        vo.setSuggestedGroupBuyPrice(suggestedPrice);
        vo.setTargetUserTags(targetTags);
        vo.setAlgorithmExplain(explain);
        vo.setOnlineStatus(onlineStatus);
        vo.setActivityId(activityId);
        vo.setInterestedUserList(interestedUserList);

        log.info("【17/17】组装返回VO完成");
        log.info("========== 拼团策略算法执行完成 ==========");
        log.info("最终结果：商品={}, 原价={}, 建议拼团价={}, 折扣={}折, 优惠金额={}",
                product.getName(), product.getPrice(), suggestedPrice,
                discountRate.multiply(BigDecimal.valueOf(10)),
                product.getPrice().subtract(suggestedPrice));
        log.info("智能定价分析完成：商品={}, 品类={}, 建议价={}", product.getName(), product.getCategory(), suggestedPrice);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onlineProduct(String productId, String activityId) {
        log.info("========== 拼团商品上架开始 ==========");
        log.info("输入参数：productId={}, activityId={}", productId, activityId);
        ProductInfo product = productDao.selectByProductId(productId);
        if (product == null) throw new RuntimeException("商品不存在：" + productId);
        GroupBuyActivity activity = groupBuyActivityDao.selectByActivityId(activityId);
        if (activity == null) throw new RuntimeException("活动不存在：" + activityId);
        GroupBuyStrategyVO vo = analyzeProduct(productId, activityId);
        GroupBuyProduct existing = groupBuyProductDao.selectByActivityAndProduct(activityId, productId);
        Date now = new Date();
        Date offlineTime = activity.getEndTime();
        if (existing != null) {
            existing.setGroupBuyPrice(vo.getSuggestedGroupBuyPrice());
            existing.setOriginalPrice(product.getPrice());
            existing.setDiscountRate(vo.getDiscountRate());
            existing.setInterestedUsers(vo.getInterestedUsers());
            existing.setTargetUserTags(vo.getTargetUserTags());
            existing.setBehaviorScore(vo.getBehaviorScore());
            existing.setPurchaseCount(vo.getPurchaseCount());
            existing.setViewCount(vo.getViewCount());
            existing.setFavoriteCount(vo.getFavoriteCount());
            existing.setStatus(1);
            existing.setOnlineTime(now);
            existing.setOfflineTime(offlineTime);
            groupBuyProductDao.update(existing);
            log.info("更新已有拼团商品成功");
        } else {
            GroupBuyProduct gbp = new GroupBuyProduct();
            gbp.setActivityId(activityId); gbp.setProductId(productId);
            gbp.setGroupBuyPrice(vo.getSuggestedGroupBuyPrice());
            gbp.setOriginalPrice(product.getPrice());
            gbp.setDiscountRate(vo.getDiscountRate());
            gbp.setInterestedUsers(vo.getInterestedUsers());
            gbp.setTargetUserTags(vo.getTargetUserTags());
            gbp.setBehaviorScore(vo.getBehaviorScore());
            gbp.setPurchaseCount(vo.getPurchaseCount());
            gbp.setViewCount(vo.getViewCount());
            gbp.setFavoriteCount(vo.getFavoriteCount());
            gbp.setStatus(1); gbp.setOnlineTime(now); gbp.setOfflineTime(offlineTime);
            groupBuyProductDao.insert(gbp);
            log.info("新增拼团商品成功");
        }
        log.info("========== 拼团商品上架完成 ==========");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offlineProduct(String productId, String activityId) {
        log.info("========== 拼团商品下架开始 ==========");
        log.info("输入参数：productId={}, activityId={}", productId, activityId);
        groupBuyProductDao.updateStatus(activityId, productId, 2);
        log.info("========== 拼团商品下架完成 ==========");
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refreshProduct(String productId, String activityId) {
        log.info("========== 拼团商品策略刷新开始 ==========");
        log.info("输入参数：productId={}, activityId={}", productId, activityId);
        GroupBuyProduct existing = groupBuyProductDao.selectByActivityAndProduct(activityId, productId);
        if (existing == null) throw new RuntimeException("未上架无法刷新");
        GroupBuyStrategyVO vo = analyzeProduct(productId, activityId);
        existing.setGroupBuyPrice(vo.getSuggestedGroupBuyPrice());
        existing.setInterestedUsers(vo.getInterestedUsers());
        existing.setBehaviorScore(vo.getBehaviorScore());
        groupBuyProductDao.update(existing);
        log.info("========== 拼团商品策略刷新完成 ==========");
    }

    // ========== 内部辅助算法与数据结构 ==========

    private static class BehaviorStats {
        int viewCount; int favoriteCount; int purchaseCount;
        int viewCount7d; int favoriteCount7d; int purchaseCount7d;
        Set<String> interestedUserIds = new HashSet<>();
    }

    private static class ElasticityResult {
        double score;           // 0~100
        double funnelScore;     // 转化漏斗得分
        double trendScore;      // 行为趋势得分
        double breadthScore;    // 用户广度得分
        double sensitivityScore;// 价格敏感度得分
        String level;           // LOW/MID/HIGH/VERY_HIGH
    }

    /**
     * 计算近30天与近7天的行为统计
     */
    private BehaviorStats calcBehaviorStats(String productId) {
        log.info("【calcBehaviorStats】开始统计用户行为数据，productId={}", productId);
        List<UserBehavior> allBehaviors = userBehaviorDao.selectAll();
        Date cutoff30 = daysAgo(BEHAVIOR_DAYS);
        Date cutoff7 = daysAgo(TREND_DAYS);
        BehaviorStats s = new BehaviorStats();
        s.favoriteCount = favoriteDao.countByProductId(productId);
        log.info("【calcBehaviorStats】全量行为数据查询完成，总行为数={}, 30天截止时间={}, 7天截止时间={}",
                allBehaviors.size(), cutoff30, cutoff7);
        for (UserBehavior b : allBehaviors) {
            if (!productId.equals(b.getProductId())) continue;
            if (b.getCreateTime() == null || b.getCreateTime().before(cutoff30)) continue;
            String t = b.getBehaviorType();
            if ("CLICK".equals(t)) s.viewCount++;
            else if ("BUY".equals(t)) s.purchaseCount++;
            s.interestedUserIds.add(b.getUserId());
            if (b.getCreateTime().after(cutoff7)) {
                if ("CLICK".equals(t)) s.viewCount7d++;
                else if ("BUY".equals(t)) s.purchaseCount7d++;
            }
        }
        s.favoriteCount7d = Math.max(1, (int)(s.favoriteCount * 0.3));
        log.info("【calcBehaviorStats】用户行为数据统计完成：近30天浏览={}, 收藏={}, 购买={}, 近7天浏览={}, 购买={}, 感兴趣用户数={}",
                s.viewCount, s.favoriteCount, s.purchaseCount,
                s.viewCount7d, s.purchaseCount7d, s.interestedUserIds.size());
        return s;
    }

    /**
     * 计算用户价格弹性模型
     */
    private ElasticityResult calcElasticity(BehaviorStats s) {
        log.info("【calcElasticity】开始计算用户价格弹性模型");
        ElasticityResult r = new ElasticityResult();
        // 1. 转化漏斗：从浏览到购买的效率
        double collectRate = s.viewCount > 0 ? (double) s.favoriteCount / s.viewCount : 0;
        double buyRate = s.favoriteCount > 0 ? (double) s.purchaseCount / s.favoriteCount :
                s.viewCount > 0 ? (double) s.purchaseCount / s.viewCount * 0.5 : 0;
        r.funnelScore = Math.min(30, collectRate * 40 + buyRate * 10);
        log.info("【calcElasticity】转化漏斗得分计算完成：收藏率={}%, 购买率={}%, 漏斗得分={}/30",
                collectRate * 100, buyRate * 100, r.funnelScore);

        // 2. 行为趋势：近7天活跃度对比
        double density30 = (s.viewCount + s.favoriteCount * 2.0 + s.purchaseCount * 3.0) / 30.0;
        double density7 = (s.viewCount7d + s.favoriteCount7d * 2.0 + s.purchaseCount7d * 3.0) / 7.0;
        double trendRatio = density30 > 0 ? density7 / density30 : 0;
        r.trendScore = Math.min(25, trendRatio * 15);
        log.info("【calcElasticity】行为趋势得分计算完成：30天日均密度={}, 7天日均密度={}, 趋势比例={}, 趋势得分={}/25",
                density30, density7, trendRatio, r.trendScore);

        // 3. 用户广度：潜在买家基数
        int userCount = s.interestedUserIds.size();
        r.breadthScore = userCount >= 50 ? 25 : userCount >= 20 ? 15 : userCount >= 5 ? 7 : 0;
        log.info("【calcElasticity】用户广度得分计算完成：感兴趣用户数={}, 广度得分={}/25",
                userCount, r.breadthScore);

        // 4. 价格敏感度：高收藏低购买即为高度敏感
        double sensitivityRatio = s.viewCount > 0 ? (double)(s.favoriteCount - s.purchaseCount * 2) / s.viewCount : 0;
        r.sensitivityScore = Math.min(20, Math.max(0, sensitivityRatio * 30));
        log.info("【calcElasticity】价格敏感度得分计算完成：敏感比例={}, 敏感度得分={}/20",
                sensitivityRatio, r.sensitivityScore);

        r.score = Math.min(100, Math.max(0, r.funnelScore + r.trendScore + r.breadthScore + r.sensitivityScore));
        r.level = r.score >= 70 ? "VERY_HIGH" : r.score >= 45 ? "HIGH" : r.score >= 20 ? "MID" : "LOW";
        log.info("【calcElasticity】用户价格弹性模型计算完成：综合分={}/100, 弹性等级={}",
                r.score, r.level);
        return r;
    }

    /**
     * 根据弹性得分计算目标折扣
     */
    private double calcTargetDiscount(double elasticityScore, double floorDiscount) {
        log.info("【calcTargetDiscount】开始计算目标折扣：elasticityScore={}, floorDiscount={}",
                elasticityScore, floorDiscount);
        double t = elasticityScore / 100.0;
        double curve = Math.sqrt(t);
        double discount = MAX_USER_DISCOUNT - curve * (MAX_USER_DISCOUNT - floorDiscount);
        double result = Math.max(floorDiscount, Math.min(MAX_USER_DISCOUNT, discount));
        log.info("【calcTargetDiscount】目标折扣计算完成：弹性比例={}, 曲线值={}, 计算折扣={}, 最终结果={}折",
                t, curve, discount, result * 10);
        return result;
    }

    /**
     * 心理定价：调整为更具诱惑力的数字
     */
    private double psychoPrice(double raw) {
        log.info("【psychoPrice】开始心理定价：原始价格={}", raw);
        double result;
        if (raw >= 100) {
            long base = (long)(raw / 10) * 10;
            result = base + 9 > raw ? base - 1 : base + 9;
        } else {
            result = Math.floor(raw) + 0.99;
        }
        log.info("【psychoPrice】心理定价完成：原始价格={}, 优化后价格={}", raw, result);
        return result;
    }

    /**
     * 构建建议推送的目标用户标签
     */
    private String buildTargetTags(ProductInfo product, BehaviorStats stats, ElasticityResult e) {
        log.info("【buildTargetTags】开始构建目标用户标签");
        List<String> tags = new ArrayList<>();
        if (product.getUserTags() != null && !product.getUserTags().isEmpty()) tags.add(product.getUserTags());
        if ("VERY_HIGH".equals(e.level) || "HIGH".equals(e.level)) tags.add("高活跃用户");
        if (e.sensitivityScore > 10) tags.add("价格敏感用户");
        tags.add(product.getCategory() + "深度用户");
        String result = String.join("、", tags.stream().distinct().collect(Collectors.toList()));
        log.info("【buildTargetTags】目标用户标签构建完成：tags={}", result);
        return result;
    }

    /**
     * 核心算法升级：多维意向分模型，筛选高概率下单用户
     */
    private List<GroupBuyStrategyVO.InterestedUserVO> buildInterestedUserList(String productId) {
        log.info("【buildInterestedUserList】开始构建高意向用户明细，productId={}", productId);
        List<UserBehavior> allBehaviors = userBehaviorDao.selectAll();
        Map<String, List<UserBehavior>> userBehaviors = allBehaviors.stream()
                .filter(b -> productId.equals(b.getProductId()))
                .collect(Collectors.groupingBy(UserBehavior::getUserId));
        log.info("【buildInterestedUserList】用户行为分组完成，有行为用户数={}", userBehaviors.size());

        List<GroupBuyStrategyVO.InterestedUserVO> results = new ArrayList<>();
        Date now = new Date();

        for (Map.Entry<String, List<UserBehavior>> entry : userBehaviors.entrySet()) {
            String userId = entry.getKey();
            List<UserBehavior> behaviors = entry.getValue();

            double score = 0;
            int view = 0, buy = 0, collect = 0, cart = 0;
            Set<String> types7d = new HashSet<>();

            // 1. 行为价值 + 时间衰减权重
            for (UserBehavior b : behaviors) {
                double weight = "BUY".equals(b.getBehaviorType()) ? 15.0 : 1.0;
                if ("CLICK".equals(b.getBehaviorType())) view++;
                else if ("BUY".equals(b.getBehaviorType())) buy++;

                long days = (now.getTime() - b.getCreateTime().getTime()) / (1000 * 3600 * 24);
                double decay = days <= 7 ? 1.0 : days <= 30 ? 0.6 : days <= 90 ? 0.3 : 0.1;
                score += weight * decay;
                if (days <= 7) types7d.add(b.getBehaviorType());
            }

            // 2. 补充收藏与加购维度
            if (favoriteDao.selectOne(userId, productId) != null) { score += 5.0; collect++; }
            if (cartDao.selectOne(userId, productId) != null) { score += 8.0; cart++; }

            // 3. 频率与冲刺加成
            if (behaviors.size() > 3) score *= 1.2;
            if (types7d.size() >= 2) score *= 2.0;

            // 4. 只有意向分 >= 15 的用户才入选明细列表
            if (score >= 15) {
                UserInfo user = userDao.selectByUserId(userId);
                if (user == null) continue;

                GroupBuyStrategyVO.InterestedUserVO vo = new GroupBuyStrategyVO.InterestedUserVO();
                vo.setUserId(userId); vo.setUsername(user.getUsername());
                vo.setGender(user.getGender()); vo.setAge(user.getAge()); vo.setCity(user.getCity());
                vo.setIntentScore(BigDecimal.valueOf(score).setScale(1, RoundingMode.HALF_UP));
                vo.setIntentLevel(score >= 40 ? "极高意向" : score >= 25 ? "高意向" : "较高意向");
                vo.setDirectViewCount(view); vo.setDirectBuyCount(buy);
                vo.setDirectCollectCount(collect);

                Map<String, BigDecimal> details = new HashMap<>();
                details.put("行为基础分", BigDecimal.valueOf(score / 1.2).setScale(1, RoundingMode.HALF_UP));
                details.put("频率/冲刺系数", BigDecimal.valueOf(score >= 15 ? (score > 25 ? 2.0 : 1.2) : 1.0));
                vo.setScoreDetails(details);

                results.add(vo);
                log.info("【buildInterestedUserList】筛选到高意向用户：userId={}, 用户名={}, 意向分={}, 意向等级={}",
                        userId, user.getUsername(), vo.getIntentScore(), vo.getIntentLevel());
            }
        }
        List<GroupBuyStrategyVO.InterestedUserVO> sortedResults = results.stream()
                .sorted((a, b) -> b.getIntentScore().compareTo(a.getIntentScore()))
                .collect(Collectors.toList());
        log.info("【buildInterestedUserList】高意向用户明细构建完成，共筛选出{}位高意向用户", sortedResults.size());
        return sortedResults;
    }

    /**
     * 生成详细的算法分析说明文本
     */
    private String buildExplain(BehaviorStats stats, ElasticityResult e,
                                BigDecimal discountRate, double floorDiscount,
                                double categoryMargin, BigDecimal suggestedPrice, BigDecimal originalPrice,
                                String category) {
        log.info("【buildExplain】开始生成算法分析说明文本");
        StringBuilder sb = new StringBuilder();
        int userCount = stats.interestedUserIds.size();
        String levelText = "VERY_HIGH".equals(e.level) ? "非常高" : "HIGH".equals(e.level) ? "较高" :
                "MID".equals(e.level) ? "中等" : "较低";
        sb.append(String.format("【行为数据】近30天：浏览%d次、收藏%d次、购买%d次，感兴趣用户%d人。\n",
                stats.viewCount, stats.favoriteCount, stats.purchaseCount, userCount));
        sb.append(String.format("近7天趋势：浏览%d次、购买%d次（%s）。\n",
                stats.viewCount7d, stats.purchaseCount7d,
                stats.viewCount7d > stats.viewCount / 4 ? "↑热度上升" : "→热度平稳"));
        sb.append("\n");
        sb.append(String.format("【弹性评估】用户价格弹性：%s（综合弹性分 %.1f/100）\n", levelText, e.score));
        sb.append(String.format("  · 转化漏斗 %.1f/30（收藏率%.1f%%，购买转化%.1f%%）\n",
                e.funnelScore,
                stats.viewCount > 0 ? stats.favoriteCount * 100.0 / stats.viewCount : 0,
                stats.favoriteCount > 0 ? stats.purchaseCount * 100.0 / stats.favoriteCount : 0));
        sb.append(String.format("  · 行为趋势 %.1f/25（%s）\n", e.trendScore,
                e.trendScore > 10 ? "近期热度明显上升" : e.trendScore > 5 ? "近期略有上升" : "趋于平稳"));
        sb.append(String.format("  · 用户广度 %.1f/25（%d位潜在买家）\n", e.breadthScore, userCount));
        sb.append(String.format("  · 价格敏感 %.1f/20（%s）\n", e.sensitivityScore,
                e.sensitivityScore > 12 ? "高度敏感，降价效果显著" :
                        e.sensitivityScore > 6 ? "有一定价格敏感度" : "对价格不太敏感"));
        sb.append("\n");
        double savingPct = (1.0 - discountRate.doubleValue()) * 100;
        double saving = originalPrice.doubleValue() - suggestedPrice.doubleValue();
        sb.append(String.format("【定价分析】品类(%s)预估毛利率%.0f%%，商家保本折扣下限%.0f折。\n",
                category, categoryMargin * 100, floorDiscount * 10));
        sb.append(String.format("综合弹性评分，建议拼团折扣 %.1f折，用户直降 ¥%.2f（省%.1f%%）。\n",
                discountRate.doubleValue() * 10, saving, savingPct));
        sb.append("\n");

        if ("VERY_HIGH".equals(e.level)) {
            sb.append("【结论】该商品用户需求旺盛、热度持续上升，强烈建议以最优惠价格上架拼团。");
        } else if ("HIGH".equals(e.level)) {
            sb.append("【结论】该商品有稳定的用户关注群体，折扣有吸引力，建议上架。");
        } else if ("MID".equals(e.level)) {
            sb.append("【结论】该商品有一定热度，可适度推广测试市场反应。");
        } else {
            sb.append("【结论】该商品当前用户行为数据较少，建议先提升曝光后再考虑上架。");
        }
        log.info("【buildExplain】算法分析说明文本生成完成");
        return sb.toString();
    }

    private Date daysAgo(int days) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -days);
        return cal.getTime();
    }
}