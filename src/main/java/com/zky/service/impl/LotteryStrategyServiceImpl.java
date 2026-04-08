package com.zky.service.impl;

import com.zky.dao.CouponDao;
import com.zky.dao.LotteryCouponStrategyDao;
import com.zky.dao.OrderDao;
import com.zky.domain.po.Coupon;
import com.zky.domain.po.LotteryCouponStrategy;
import com.zky.domain.vo.LotteryStrategyVO;
import com.zky.service.ILotteryStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LotteryStrategyServiceImpl implements ILotteryStrategyService {

    /** 统计天数：近30天 */
    private static final int STAT_DAYS = 30;
    /** 最小利润率阈值：5% */
    private static final double MIN_MARGIN = 0.05;

    /** 品类基准利润率配置 */
    private static final Map<String, Double> CATEGORY_MARGIN = new HashMap<>();
    static {
        // ... (保持原有配置不变)
        CATEGORY_MARGIN.put("3C数码", 0.15);    CATEGORY_MARGIN.put("电竞外设", 0.20);
        CATEGORY_MARGIN.put("家用电器", 0.18);   CATEGORY_MARGIN.put("办公设备", 0.20);
        CATEGORY_MARGIN.put("男士腕表", 0.35);   CATEGORY_MARGIN.put("轻奢首饰", 0.40);
        CATEGORY_MARGIN.put("美妆护肤", 0.45);   CATEGORY_MARGIN.put("香氛香水", 0.50);
        CATEGORY_MARGIN.put("美甲彩妆", 0.45);   CATEGORY_MARGIN.put("化妆工具", 0.40);
        CATEGORY_MARGIN.put("内衣睡衣", 0.45);   CATEGORY_MARGIN.put("网红配饰", 0.50);
        CATEGORY_MARGIN.put("母婴用品", 0.30);   CATEGORY_MARGIN.put("代餐轻食", 0.40);
        CATEGORY_MARGIN.put("户外装备", 0.30);   CATEGORY_MARGIN.put("骑行装备", 0.28);
        CATEGORY_MARGIN.put("运动护具", 0.30);   CATEGORY_MARGIN.put("垂钓装备", 0.30);
        CATEGORY_MARGIN.put("汽车用品", 0.35);   CATEGORY_MARGIN.put("五金工具", 0.25);
        CATEGORY_MARGIN.put("机械器材", 0.20);   CATEGORY_MARGIN.put("商务服饰", 0.40);
        CATEGORY_MARGIN.put("布艺家纺", 0.40);   CATEGORY_MARGIN.put("家居花艺", 0.45);
        CATEGORY_MARGIN.put("收纳整理", 0.35);   CATEGORY_MARGIN.put("厨房用具", 0.35);
        CATEGORY_MARGIN.put("餐具茶具", 0.40);   CATEGORY_MARGIN.put("酒具酒品", 0.35);
        CATEGORY_MARGIN.put("粮油米面", 0.15);   CATEGORY_MARGIN.put("洗护清洁", 0.30);
        CATEGORY_MARGIN.put("饮用水饮", 0.25);   CATEGORY_MARGIN.put("日用百货", 0.30);
        CATEGORY_MARGIN.put("通用", 0.25);
    }

    @Resource private CouponDao couponDao;
    @Resource private LotteryCouponStrategyDao strategyDao;
    @Resource private OrderDao orderDao;

    /**
     * 内部市场数据承载类
     */
    private static class MarketData {
        /** 近30天订单总数 */
        int orderCount30d; 
        /** 近30天活跃用户数 */
        int userCount30d; 
        /** 平均客单价 */
        double avgOrderPrice; 
        /** 复购率 */
        double repeatBuyRate;
    }

    /**
     * 需求弹性计算结果承载类
     */
    private static class ElasticityResult {
        /** 综合弹性得分 */
        double score; 
        /** 订单密度得分 */
        double densityScore; 
        /** 复购惯性得分 */
        double repeatScore; 
        /** 价格感知得分 */
        double perceptionScore; 
        /** 优惠敏感得分 */
        double sensitivityScore;
    }

    /**
     * 分析优惠券策略价值
     * 基于品类历史数据、弹性模型、利润模型进行综合评估
     */
    @Override
    public LotteryStrategyVO analyzeCoupon(String couponId) {
        // 1. 获取优惠券基础信息
        Coupon coupon = couponDao.selectByCouponId(couponId);
        if (coupon == null) throw new RuntimeException("优惠券不存在：" + couponId);

        // 2. 采集所属品类的市场大盘数据
        MarketData market = collectMarketData(coupon.getCategory());
        double avgPrice = market.avgOrderPrice;

        // 3. 计算优惠券的实际让利金额（处理折扣券/直减券/满减券）
        double actualDiscount = calcActualDiscount(coupon, avgPrice);
        // 获取品类基准毛利率
        double margin = CATEGORY_MARGIN.getOrDefault(coupon.getCategory(), 0.25);

        // 4. 计算需求价格弹性模型
        ElasticityResult elasticity = calcElasticity(market, actualDiscount, avgPrice);

        // 5. 预测业务增益指标
        // 预测转化率提升
        double conversionLift = calcConversionLift(elasticity.score, actualDiscount, avgPrice);
        // 预测销量提升（结合复购率）
        double volumeLift = conversionLift * (1 + market.repeatBuyRate * 0.5);
        
        // 6. 财务模型计算
        double discountRatio = avgPrice > 0 ? actualDiscount / avgPrice : 0;
        // 计算净利润率变动（考虑销量提升带来的边际贡献）
        double netProfitRate = Math.max(margin - discountRatio + volumeLift * 0.05, -0.05);
        // 计算综合 ROI 评分 (0-100)
        double roiScore = Math.min(100, Math.max(0, calcROI(margin, netProfitRate, volumeLift, elasticity.score, discountRatio)));
        
        // 7. 计算盈亏平衡点让利上限
        double breakEvenDiscount = avgPrice * (margin - MIN_MARGIN);
        // 预测带来的总营收增长
        double expectedRevenueLift = avgPrice * volumeLift * Math.max(market.orderCount30d, 10);

        // 8. 封装分析结果 VO
        LotteryCouponStrategy existing = strategyDao.selectByCouponId(couponId);
        LotteryStrategyVO vo = new LotteryStrategyVO();
        vo.setCouponId(couponId); vo.setCouponName(coupon.getName());
        vo.setCategory(coupon.getCategory()); vo.setCouponType(coupon.getCouponType());
        vo.setCouponValue(coupon.getValue()); vo.setMinOrderAmount(calcMinOrderAmount(coupon));
        vo.setStatus(existing != null ? existing.getStatus() : 0);
        vo.setAvgOrderPrice(bd(avgPrice)); vo.setCategoryMargin(bd(margin));
        vo.setOrderCount30d(market.orderCount30d); vo.setUserCount30d(market.userCount30d);
        vo.setRepeatBuyRate(bd(market.repeatBuyRate));
        vo.setElasticityScore(bd(elasticity.score)); vo.setConversionLift(bd(conversionLift));
        vo.setVolumeLift(bd(volumeLift)); vo.setActualDiscount(bd(actualDiscount));
        vo.setNetProfitRate(bd(netProfitRate)); vo.setRoiScore(bd(roiScore));
        vo.setRoiLevel(roiLevel(roiScore)); vo.setBreakEvenDiscount(bd(breakEvenDiscount));
        vo.setExpectedRevenueLift(bd(expectedRevenueLift));
        
        // 生成算法可解释性说明文本
        vo.setRecommendReason(buildReason(coupon, market, elasticity, roiScore,
                netProfitRate, conversionLift, volumeLift, actualDiscount, breakEvenDiscount, avgPrice));
        return vo;
    }

    @Override @Transactional
    public void onlineCoupon(String couponId) {
        Coupon coupon = couponDao.selectByCouponId(couponId);
        if (coupon == null) throw new RuntimeException("优惠券不存在");
        if (coupon.getStatus() == null || coupon.getStatus() == 0) {
            throw new RuntimeException("优惠券已禁用，请先在优惠券管理中调整为可用状态再上架");
        }
        LotteryStrategyVO vo = analyzeCoupon(couponId);
        LotteryCouponStrategy existing = strategyDao.selectByCouponId(couponId);
        if (existing != null) {
            fillStrategy(existing, vo); existing.setStatus(1);
            strategyDao.update(existing); strategyDao.updateStatus(couponId, 1);
        } else {
            LotteryCouponStrategy s = new LotteryCouponStrategy();
            s.setStrategyId(UUID.randomUUID().toString()); s.setCouponId(couponId);
            s.setCategory(coupon.getCategory()); s.setCouponType(coupon.getCouponType());
            s.setCouponValue(coupon.getValue()); s.setMinOrderAmount(vo.getMinOrderAmount());
            s.setIsFallback(0); s.setStatus(1); fillStrategy(s, vo);
            strategyDao.insert(s);
        }
    }

    @Override @Transactional
    public void offlineCoupon(String couponId) { strategyDao.updateStatus(couponId, 2); }

    @Override @Transactional
    public LotteryStrategyVO refreshCoupon(String couponId) {
        LotteryStrategyVO vo = analyzeCoupon(couponId);
        LotteryCouponStrategy ex = strategyDao.selectByCouponId(couponId);
        if (ex != null) { fillStrategy(ex, vo); strategyDao.update(ex); }
        return vo;
    }

    @Override
    public List<LotteryStrategyVO> listAllStrategies() {
        return strategyDao.selectAll().stream().map(s -> {
            Coupon c = couponDao.selectByCouponId(s.getCouponId());
            if (c == null) return null;
            LotteryStrategyVO vo = new LotteryStrategyVO();
            vo.setCouponId(s.getCouponId()); vo.setCouponName(c.getName());
            vo.setCategory(s.getCategory()); vo.setCouponType(s.getCouponType());
            vo.setCouponValue(s.getCouponValue()); vo.setMinOrderAmount(s.getMinOrderAmount());
            vo.setStatus(s.getStatus()); vo.setAvgOrderPrice(s.getAvgOrderPrice());
            vo.setCategoryMargin(s.getCategoryMargin()); vo.setOrderCount30d(s.getOrderCount30d());
            vo.setUserCount30d(s.getUserCount30d()); vo.setRepeatBuyRate(s.getRepeatBuyRate());
            vo.setElasticityScore(s.getElasticityScore()); vo.setConversionLift(s.getConversionLift());
            vo.setVolumeLift(s.getVolumeLift()); vo.setActualDiscount(s.getActualDiscount());
            vo.setNetProfitRate(s.getNetProfitRate()); vo.setRoiScore(s.getRoiScore());
            vo.setRoiLevel(roiLevel(s.getRoiScore() != null ? s.getRoiScore().doubleValue() : 0));
            vo.setRecommendReason(s.getRecommendReason());
            vo.setIsFallback(s.getIsFallback());
            vo.setCouponStatus(c.getStatus()); // coupon表可用状态
            vo.setBreakEvenDiscount(s.getBreakEvenDiscount()); // 直接读库，与analyze保持一致
            return vo;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    private MarketData collectMarketData(String category) {
        MarketData m = new MarketData();
        try {
            List<Map<String,Object>> stats = orderDao.selectCategoryStats(category, STAT_DAYS);
            if (stats != null && !stats.isEmpty()) {
                Map<String,Object> row = stats.get(0);
                m.orderCount30d = toInt(row.get("orderCount"));
                m.userCount30d  = toInt(row.get("userCount"));
                m.avgOrderPrice = toDouble(row.get("avgOrderPrice"), 100.0);
            }
            if (m.avgOrderPrice <= 0) m.avgOrderPrice = 100.0;
            int repeatCount = orderDao.selectRepeatBuyerCount(category, STAT_DAYS);
            m.repeatBuyRate = m.userCount30d > 0 ? (double) repeatCount / m.userCount30d : 0;
        } catch (Exception ex) {
            log.warn("品类数据采集异常：{}", ex.getMessage());
            m.avgOrderPrice = 100.0;
        }
        return m;
    }

    private double calcActualDiscount(Coupon coupon, double avgPrice) {
        double v = coupon.getValue().doubleValue();
        if ("DIRECT".equals(coupon.getCouponType())) return v;
        if ("FULL".equals(coupon.getCouponType()))   return v;
        if ("DISCOUNT".equals(coupon.getCouponType())) return avgPrice * (1 - v);
        return v;
    }

    private BigDecimal calcMinOrderAmount(Coupon coupon) {
        if ("FULL".equals(coupon.getCouponType()))
            return coupon.getValue().multiply(BigDecimal.valueOf(6));
        return BigDecimal.ZERO;
    }

    private ElasticityResult calcElasticity(MarketData m, double actualDiscount, double avgPrice) {
        ElasticityResult r = new ElasticityResult();
        r.densityScore    = m.orderCount30d >= 100 ? 30 : m.orderCount30d >= 50 ? 22
                          : m.orderCount30d >= 20  ? 15 : m.orderCount30d >= 5  ? 8 : 3;
        r.repeatScore     = Math.min(25, m.repeatBuyRate * 50);
        double ratio      = avgPrice > 0 ? actualDiscount / avgPrice : 0;
        r.perceptionScore = Math.min(25, ratio * 100);
        r.sensitivityScore= avgPrice < 50 ? 20 : avgPrice < 100 ? 16
                          : avgPrice < 300 ? 12 : avgPrice < 1000 ? 7 : 3;
        r.score = Math.min(100, r.densityScore + r.repeatScore + r.perceptionScore + r.sensitivityScore);
        return r;
    }

    private double calcConversionLift(double elasticityScore, double actualDiscount, double avgPrice) {
        double base          = elasticityScore / 100.0;
        double discountFactor = avgPrice > 0 ? Math.min(1.0, actualDiscount / avgPrice * 5) : 0;
        return base * discountFactor * 0.6;
    }

    private double calcROI(double margin, double netProfitRate, double volumeLift,
                           double elasticityScore, double discountRatio) {
        double profitScore  = (margin > 0 ? netProfitRate / margin : 0) * 40;
        double volumeScore  = Math.min(30, volumeLift * 100);
        double elasticBonus = elasticityScore * 0.30;
        return Math.max(0, profitScore + volumeScore + elasticBonus);
    }

    private String roiLevel(double score) {
        if (score >= 75) return "EXCELLENT";
        if (score >= 55) return "HIGH";
        if (score >= 35) return "MID";
        return "LOW";
    }

    private String buildReason(Coupon coupon, MarketData m, ElasticityResult e,
                               double roiScore, double netProfitRate, double conversionLift,
                               double volumeLift, double actualDiscount, double breakEvenDiscount, double avgPrice) {
        String safe = actualDiscount <= breakEvenDiscount ? "商家可安心上架" : "让利超保本线建议调整";
        return String.format(
                "【%s】%s — 近30天%d笔订单/%d位买家，均价¥%.0f；" +
                "弹性分%.0f，转化+%.1f%%，销量+%.1f%%；" +
                "实际让利¥%.2f（保本上限¥%.2f），净利润率%.1f%%，%s；" +
                "综合ROI %.0f分（%s）。",
                coupon.getCategory(), coupon.getName(),
                m.orderCount30d, m.userCount30d, avgPrice,
                e.score, conversionLift*100, volumeLift*100,
                actualDiscount, breakEvenDiscount, netProfitRate*100, safe,
                roiScore, roiLevel(roiScore));
    }

    private void fillStrategy(LotteryCouponStrategy s, LotteryStrategyVO vo) {
        s.setAvgOrderPrice(vo.getAvgOrderPrice());   s.setCategoryMargin(vo.getCategoryMargin());
        s.setOrderCount30d(vo.getOrderCount30d());   s.setUserCount30d(vo.getUserCount30d());
        s.setRepeatBuyRate(vo.getRepeatBuyRate());   s.setElasticityScore(vo.getElasticityScore());
        s.setConversionLift(vo.getConversionLift()); s.setVolumeLift(vo.getVolumeLift());
        s.setActualDiscount(vo.getActualDiscount()); s.setNetProfitRate(vo.getNetProfitRate());
        s.setRoiScore(vo.getRoiScore());             s.setBreakEvenDiscount(vo.getBreakEvenDiscount());
        s.setRecommendReason(vo.getRecommendReason());
    }

    private BigDecimal bd(double v) { return BigDecimal.valueOf(v).setScale(4, RoundingMode.HALF_UP); }
    private int    toInt(Object o)               { return o == null ? 0   : ((Number) o).intValue(); }
    private double toDouble(Object o, double def) { return o == null ? def : ((Number) o).doubleValue(); }
}
