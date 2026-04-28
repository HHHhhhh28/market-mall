package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.dao.CouponDao;
import com.zky.dao.LotteryCouponStrategyDao;
import com.zky.domain.po.Coupon;
import com.zky.domain.po.LotteryCouponStrategy;
import com.zky.domain.vo.LotteryStrategyVO;
import com.zky.service.ILotteryStrategyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 后台抽奖管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mall/admin/lottery")
public class AdminLotteryController {

    @Resource private ILotteryStrategyService strategyService;
    @Resource private CouponDao couponDao;
    @Resource private LotteryCouponStrategyDao strategyDao;

    /** 查询所有优惠券策略列表 */
    @GetMapping("/strategies")
    public Response<List<LotteryStrategyVO>> listStrategies() {
        log.info("[AdminLottery] 查询所有优惠券策略");
        return Response.<List<LotteryStrategyVO>>builder()
                .code("0000").info("Success")
                .data(strategyService.listAllStrategies()).build();
    }

    /** 对指定优惠券执行策略分析（预览，不写库） */
    @GetMapping("/analyze")
    public Response<LotteryStrategyVO> analyze(@RequestParam String couponId) {
        log.info("[AdminLottery] 策略分析 couponId={}", couponId);
        return Response.<LotteryStrategyVO>builder()
                .code("0000").info("Success")
                .data(strategyService.analyzeCoupon(couponId)).build();
    }

    /** 上架优惠券到抽奖池 */
    @PostMapping("/online")
    public Response<Void> online(@RequestParam String couponId) {
        log.info("[AdminLottery] 上架优惠券 couponId={}", couponId);
        strategyService.onlineCoupon(couponId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    /** 下架优惠券 */
    @PostMapping("/offline")
    public Response<Void> offline(@RequestParam String couponId) {
        log.info("[AdminLottery] 下架优惠券 couponId={}", couponId);
        strategyService.offlineCoupon(couponId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    /** 刷新策略数据（重新计算并写库） */
    @PostMapping("/refresh")
    public Response<LotteryStrategyVO> refresh(@RequestParam String couponId) {
        log.info("[AdminLottery] 刷新策略 couponId={}", couponId);
        return Response.<LotteryStrategyVO>builder()
                .code("0000").info("Success")
                .data(strategyService.refreshCoupon(couponId)).build();
    }

    /** 获取全部可用优惠券列表（含已分析/未分析） */
    @GetMapping("/coupons")
    public Response<List<Coupon>> listCoupons(
            @RequestParam(required = false) String category) {
        log.info("[AdminLottery] 查询优惠券列表 category={}", category);
        List<Coupon> list = couponDao.selectAllForAdmin();
        return Response.<List<Coupon>>builder().code("0000").info("Success").data(list).build();
    }

    /** 新增优惠券（同时插入 lottery_coupon_strategy 默认记录） */
    @PostMapping("/coupon/create")
    public Response<Void> createCoupon(@RequestBody Map<String, Object> body) {
        String category  = (String) body.get("category");
        String couponType= (String) body.get("couponType");
        double value     = Double.parseDouble(body.get("value").toString());
        double minOrder  = body.containsKey("minOrderAmount") ? Double.parseDouble(body.get("minOrderAmount").toString()) : 0.0;

        // 生成 coupon_id: c-{拼音简写}-{序号}
        String pinyin = toPinyin(category);
        long seq = System.currentTimeMillis() % 10000;
        String couponId = String.format("c-%s-%02d", pinyin, seq % 100);

        // 生成 name
        String name;
        if ("DIRECT".equals(couponType)) {
            name = category + "立减" + (int)value + "元券";
        } else if ("FULL".equals(couponType)) {
            name = category + "满" + (int)minOrder + "减" + (int)value + "券";
        } else {
            int discount = (int)Math.round(value * 10);
            name = category + discount + "折券";
        }

        // 插 coupon
        Coupon coupon = new Coupon();
        coupon.setCouponId(couponId);
        coupon.setCategory(category);
        coupon.setCouponType(couponType);
        coupon.setValue(BigDecimal.valueOf(value));
        coupon.setName(name);
        coupon.setStatus(1);
        couponDao.insert(coupon);

        // 插 lottery_coupon_strategy 默认记录
        String strategyId = "s-" + pinyin + "-" + String.format("%02d", seq % 100);
        LotteryCouponStrategy s = new LotteryCouponStrategy();
        s.setStrategyId(strategyId);
        s.setCouponId(couponId);
        s.setCategory(category);
        s.setCouponType(couponType);
        s.setCouponValue(BigDecimal.valueOf(value));
        s.setMinOrderAmount(BigDecimal.valueOf(minOrder));
        s.setAvgOrderPrice(BigDecimal.ZERO);
        s.setCategoryMargin(BigDecimal.valueOf(0.25));
        s.setOrderCount30d(0);
        s.setUserCount30d(0);
        s.setRepeatBuyRate(BigDecimal.ZERO);
        s.setElasticityScore(BigDecimal.ZERO);
        s.setConversionLift(BigDecimal.ZERO);
        s.setVolumeLift(BigDecimal.ZERO);
        s.setActualDiscount(BigDecimal.ZERO);
        s.setNetProfitRate(BigDecimal.ZERO);
        s.setRoiScore(BigDecimal.ZERO);
        s.setRecommendReason("待商家分析后决定是否上架");
        s.setStatus(0);
        s.setIsFallback(0);
        strategyDao.insert(s);

        log.info("[AdminLottery] 新增优惠券 couponId={} name={}", couponId, name);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    /** 修改优惠券 value / status */
    @PostMapping("/coupon/update")
    public Response<Void> updateCoupon(@RequestBody Map<String, Object> body) {
        String couponId = (String) body.get("couponId");
        double value    = Double.parseDouble(body.get("value").toString());
        int status      = Integer.parseInt(body.get("status").toString());
        Coupon coupon = new Coupon();
        coupon.setCouponId(couponId);
        coupon.setValue(BigDecimal.valueOf(value));
        coupon.setStatus(status);
        couponDao.update(coupon);
        // 禁用时自动下架
        if (status == 0) {
            LotteryCouponStrategy existing = strategyDao.selectByCouponId(couponId);
            if (existing != null && existing.getStatus() == 1) {
                strategyDao.updateStatus(couponId, 2);
            }
        }
        // 同步更新策略表的 coupon_value / min_order_amount
        LotteryCouponStrategy existing = strategyDao.selectByCouponId(couponId);
        if (existing != null) {
            existing.setCouponValue(BigDecimal.valueOf(value));
            if (body.containsKey("minOrderAmount")) {
                existing.setMinOrderAmount(BigDecimal.valueOf(Double.parseDouble(body.get("minOrderAmount").toString())));
            }
            strategyDao.update(existing);
        }
        log.info("[AdminLottery] 修改优惠券 couponId={} status={}", couponId, status);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    /** 删除优惠券（级联删除策略记录） */
    @DeleteMapping("/coupon/delete")
    public Response<Void> deleteCoupon(@RequestParam String couponId) {
        strategyDao.deleteByCouponId(couponId);
        couponDao.deleteByCouponId(couponId);
        log.info("[AdminLottery] 删除优惠券 couponId={}", couponId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    /** 品类拼音简写映射 */
    private String toPinyin(String category) {
        Map<String,String> m = new java.util.HashMap<>();
        m.put("3C数码","3c"); m.put("户外装备","outdoor"); m.put("汽车用品","auto");
        m.put("商务服饰","biz"); m.put("垂钓装备","fish"); m.put("机械器材","mach");
        m.put("男士腕表","watch"); m.put("运动护具","sport"); m.put("酒具酒品","wine");
        m.put("骑行装备","bike"); m.put("五金工具","tool"); m.put("电竞外设","gaming");
        m.put("美妆护肤","beauty"); m.put("轻奢首饰","jewel"); m.put("母婴用品","baby");
        m.put("香氛香水","perfume"); m.put("布艺家纺","fabric"); m.put("美甲彩妆","nail");
        m.put("网红配饰","acc"); m.put("代餐轻食","diet"); m.put("化妆工具","makeup");
        m.put("家居花艺","flower"); m.put("内衣睡衣","inner"); m.put("餐具茶具","tea");
        m.put("粮油米面","grain"); m.put("洗护清洁","clean"); m.put("家用电器","elec");
        m.put("办公设备","office"); m.put("厨房用具","kitchen"); m.put("饮用水饮","drink");
        m.put("收纳整理","storage"); m.put("日用百货","daily");
        return m.getOrDefault(category, "misc");
    }
}
