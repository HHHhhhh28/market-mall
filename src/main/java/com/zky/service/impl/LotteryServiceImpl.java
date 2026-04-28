package com.zky.service.impl;

import com.zky.dao.*;
import com.zky.domain.po.Coupon;
import com.zky.domain.po.LotteryCouponStrategy;
import com.zky.domain.po.UserCoupon;
import com.zky.domain.po.UserLottery;
import com.zky.domain.vo.LotteryDrawResultVO;
import com.zky.domain.vo.LotteryGridItemVO;
import com.zky.domain.vo.LotteryInfoVO;
import com.zky.service.ILotteryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LotteryServiceImpl implements ILotteryService {

    @Resource
    private UserLotteryDao userLotteryDao;
    @Resource
    private CouponDao couponDao;
    @Resource
    private UserCouponDao userCouponDao;
    @Resource
    private LotteryCouponStrategyDao lotteryStrategyDao;
    @Resource
    private UserBehaviorDao userBehaviorDao;

    @Override
    public LotteryInfoVO getLotteryInfo(String userId) {
        UserLottery record = userLotteryDao.selectByUserId(userId);
        LotteryInfoVO vo = new LotteryInfoVO();
        vo.setUserId(userId);
        if (record == null) {
            vo.setLotteryCount(0);
            vo.setSignedToday(false);
            return vo;
        }
        vo.setLotteryCount(record.getLotteryCount() == null ? 0 : record.getLotteryCount());
        vo.setSignedToday(isSameDate(record.getLastSignDate(), new Date()));
        return vo;
    }

    @Override
    public Integer signIn(String userId) {
        Date today = new Date();
        UserLottery record = userLotteryDao.selectByUserId(userId);
        if (record == null) {
            UserLottery insert = new UserLottery();
            insert.setUserId(userId);
            insert.setLotteryCount(1);
            insert.setLastSignDate(today);
            userLotteryDao.insert(insert);
            return 1;
        }
        if (isSameDate(record.getLastSignDate(), today)) {
            return record.getLotteryCount() == null ? 0 : record.getLotteryCount();
        }
        int current = record.getLotteryCount() == null ? 0 : record.getLotteryCount();
        record.setLotteryCount(current + 1);
        record.setLastSignDate(today);
        userLotteryDao.update(record);
        return record.getLotteryCount();
    }

    @Override
    public LotteryDrawResultVO draw(String userId, List<LotteryGridItemVO> gridItems) {
        UserLottery record = userLotteryDao.selectByUserId(userId);
        if (record == null || record.getLotteryCount() == null || record.getLotteryCount() <= 0) {
            throw new RuntimeException("无可用抽奖次数");
        }

        if (CollectionUtils.isEmpty(gridItems)) {
            throw new RuntimeException("当前无可用抽奖网格配置");
        }

        int currentCount = record.getLotteryCount();
        record.setLotteryCount(currentCount - 1);
        userLotteryDao.update(record);

        Random random = new Random();
        int hitIndex = random.nextInt(gridItems.size());
        LotteryGridItemVO hitItem = gridItems.get(hitIndex);

        if ("COUPON".equals(hitItem.getType()) && hitItem.getCouponId() != null) {
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(hitItem.getCouponId());
            userCoupon.setCategory(hitItem.getCategory());
            userCoupon.setStatus(0);
            userCouponDao.insert(userCoupon);
        } else if ("LOTTERY_ADD_1".equals(hitItem.getType())) {
            adjustLotteryCount(userId, 1);
        } else if ("LOTTERY_ADD_2".equals(hitItem.getType())) {
            adjustLotteryCount(userId, 2);
        }

        LotteryDrawResultVO result = new LotteryDrawResultVO();
        result.setGridItems(gridItems);
        result.setHitIndex(hitIndex);
        LotteryInfoVO info = getLotteryInfo(userId);
        result.setRemainingLotteryCount(info.getLotteryCount());
        result.setHitItem(hitItem);
        return result;
    }

    @Override
    public List<LotteryGridItemVO> previewGrid(String userId) {
        // 1. 按用户行为加权获取兴趣品类（view=1, collect/cart=3, buy=5）
        List<String> topCategories = getTopCategoriesByBehavior(userId);

        // 2. 按品类顺序从已上架策略中取优惠券，取满6个为止
        List<LotteryGridItemVO> couponItems = buildGridFromStrategy(topCategories);

        // 3. 不足6个时用保底券补齐
        if (couponItems.size() < 6) {
            List<LotteryCouponStrategy> fallbacks = lotteryStrategyDao.selectFallbackList();
            Collections.shuffle(fallbacks);
            for (LotteryCouponStrategy fb : fallbacks) {
                if (couponItems.size() >= 6) break;
                // 避免重复
                boolean dup = couponItems.stream().anyMatch(i -> fb.getCouponId().equals(i.getCouponId()));
                if (!dup) {
                    Coupon c = couponDao.selectByCouponId(fb.getCouponId());
                    if (c != null) couponItems.add(toGridItem(c));
                }
            }
        }

        // 4. 仍不足6个，直接从 coupon 表取通用保底券
        if (couponItems.size() < 6) {
            List<Coupon> generic = couponDao.selectAvailableByCategory("通用");
            Collections.shuffle(generic);
            for (Coupon c : generic) {
                if (couponItems.size() >= 6) break;
                boolean dup = couponItems.stream().anyMatch(i -> c.getCouponId().equals(i.getCouponId()));
                if (!dup) couponItems.add(toGridItem(c));
            }
        }

        // 5. 最终截断到6个
        if (couponItems.size() > 6) couponItems = couponItems.subList(0, 6);

        // 6. 组装固定3格 + 6个优惠券格 = 9格
        List<LotteryGridItemVO> grid = new ArrayList<>();
        LotteryGridItemVO thanks = new LotteryGridItemVO();
        thanks.setType("NONE"); thanks.setName("谢谢惠顾");
        LotteryGridItemVO add1 = new LotteryGridItemVO();
        add1.setType("LOTTERY_ADD_1"); add1.setName("抽奖次数+1");
        LotteryGridItemVO add2 = new LotteryGridItemVO();
        add2.setType("LOTTERY_ADD_2"); add2.setName("抽奖次数+2");
        grid.add(thanks); grid.add(add1); grid.add(add2);
        grid.addAll(couponItems);

        // 7. 补充空格兜底（理论不会触发）
        while (grid.size() < 9) { grid.add(thanks); }
        if (grid.size() > 9) grid = grid.subList(0, 9);

        Collections.shuffle(grid);
        return grid;
    }

    /** 从已上架策略中按用户兴趣品类取优惠券VO（最多6个） */
    private List<LotteryGridItemVO> buildGridFromStrategy(List<String> topCategories) {
        List<LotteryGridItemVO> result = new ArrayList<>();
        if (!CollectionUtils.isEmpty(topCategories)) {
            List<LotteryCouponStrategy> strategies =
                    lotteryStrategyDao.selectOnlineByCategories(topCategories);
            for (LotteryCouponStrategy s : strategies) {
                if (result.size() >= 6) break;
                Coupon c = couponDao.selectByCouponId(s.getCouponId());
                if (c != null) result.add(toGridItem(c));
            }
        }
        // 品类优惠券不足时，补充任意已上架非保底优惠券
        if (result.size() < 6) {
            List<LotteryCouponStrategy> all = lotteryStrategyDao.selectOnlineList();
            for (LotteryCouponStrategy s : all) {
                if (result.size() >= 6) break;
                if (s.getIsFallback() != null && s.getIsFallback() == 1) continue;
                boolean dup = result.stream().anyMatch(i -> s.getCouponId().equals(i.getCouponId()));
                if (!dup) {
                    Coupon c = couponDao.selectByCouponId(s.getCouponId());
                    if (c != null) result.add(toGridItem(c));
                }
            }
        }
        return result;
    }

    private LotteryGridItemVO toGridItem(Coupon c) {
        LotteryGridItemVO item = new LotteryGridItemVO();
        item.setType("COUPON");
        item.setCouponId(c.getCouponId());
        item.setName(c.getName());
        item.setCategory(c.getCategory());
        item.setCouponType(c.getCouponType());
        item.setValue(c.getValue());
        return item;
    }

    private boolean isSameDate(Date d1, Date d2) {
        if (d1 == null || d2 == null) {
            return false;
        }
        LocalDate l1 = d1.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate l2 = d2.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return l1.equals(l2);
    }

    private void adjustLotteryCount(String userId, int delta) {
        UserLottery record = userLotteryDao.selectByUserId(userId);
        if (record == null) {
            UserLottery insert = new UserLottery();
            insert.setUserId(userId);
            insert.setLotteryCount(Math.max(delta, 0));
            insert.setLastSignDate(null);
            userLotteryDao.insert(insert);
            return;
        }
        int current = record.getLotteryCount() == null ? 0 : record.getLotteryCount();
        record.setLotteryCount(current + delta);
        userLotteryDao.update(record);
    }

    /**
     * 基于用户行为加权获取兴趣品类列表（按权重降序）
     * 行为权重：view/click=1, collect/cart=3, buy=5
     */
    private List<String> getTopCategoriesByBehavior(String userId) {
        try {
            List<java.util.Map<String, Object>> rows = userBehaviorDao.selectCategoryWeightByUser(userId);
            if (CollectionUtils.isEmpty(rows)) return Collections.emptyList();
            List<String> categories = new ArrayList<>();
            for (java.util.Map<String, Object> row : rows) {
                Object cat = row.get("category");
                if (cat != null && !categories.contains(cat.toString())) {
                    categories.add(cat.toString());
                }
            }
            return categories;
        } catch (Exception e) {
            log.warn("获取用户兴趣品类失败，返回空列表：{}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private List<LotteryGridItemVO> buildGridItems(List<String> topCategories) {
        // 1. 初始化固定的3个基础奖品（1个谢谢惠顾 + 1个抽奖+1 + 1个抽奖+2）
        List<LotteryGridItemVO> items = new ArrayList<>();

        LotteryGridItemVO thanks = new LotteryGridItemVO();
        thanks.setType("NONE");
        thanks.setName("谢谢惠顾");
        items.add(thanks);

        LotteryGridItemVO add1 = new LotteryGridItemVO();
        add1.setType("LOTTERY_ADD_1");
        add1.setName("抽奖次数+1");
        items.add(add1);

        LotteryGridItemVO add2 = new LotteryGridItemVO();
        add2.setType("LOTTERY_ADD_2");
        add2.setName("抽奖次数+2");
        items.add(add2);

        // 2. 按品类数量规则抽取优惠券
        List<Coupon> couponsPool = new ArrayList<>();
        int categoryCount = topCategories.size();

        if (categoryCount >= 3) {
            // 品类数=3：每个品类随机取2个（3×2=6）
            for (String category : topCategories.stream().limit(3).collect(Collectors.toList())) {
                List<Coupon> coupons = couponDao.selectAvailableByCategory(category);
                // Spring写法：判断集合为空（null/空列表）
                if (CollectionUtils.isEmpty(coupons)) {
                    continue;
                }
                Collections.shuffle(coupons);
                couponsPool.addAll(coupons.subList(0, 2));
            }
        } else if (categoryCount == 2) {
            // 品类数=2：每个品类取全部3个（2×3=6）
            for (String category : topCategories) {
                List<Coupon> coupons = couponDao.selectAvailableByCategory(category);
                // Spring写法：判断集合非空（!isEmpty）
                if (!CollectionUtils.isEmpty(coupons)) {
                    Collections.shuffle(coupons);
                    couponsPool.addAll(coupons.subList(0, 3));
                }
            }
        } else if (categoryCount == 1) {
            // 品类数=1：取该品类全部3个，重复2遍（3×2=6）
            String category = topCategories.get(0);
            List<Coupon> coupons = couponDao.selectAvailableByCategory(category);
            if (!CollectionUtils.isEmpty(coupons)) {
                Collections.shuffle(coupons);
                List<Coupon> categoryCoupons = coupons.subList(0, 3);
                couponsPool.addAll(categoryCoupons);
                couponsPool.addAll(categoryCoupons);
            }
        }

        // 3. 校验优惠券数量（仅日志提示，无重复补充）
        if (couponsPool.size() != 6) {
            log.warn("优惠券数量异常，预期6个，实际{}个，品类数：{}", couponsPool.size(), categoryCount);
            couponsPool = couponsPool.stream().limit(6).collect(Collectors.toList());
        }

        // 4. 转换优惠券为VO
        for (Coupon coupon : couponsPool) {
            LotteryGridItemVO item = new LotteryGridItemVO();
            item.setType("COUPON");
            item.setCouponId(coupon.getCouponId());
            item.setName(coupon.getName());
            item.setCategory(coupon.getCategory());
            item.setCouponType(coupon.getCouponType());
            item.setValue(coupon.getValue());
            items.add(item);
        }

        // 5. 兜底（仅异常时触发）
        while (items.size() < 9) {
            LotteryGridItemVO placeholder = new LotteryGridItemVO();
            placeholder.setType("NONE");
            placeholder.setName("谢谢惠顾");
            items.add(placeholder);
        }
        if (items.size() > 9) {
            items = items.subList(0, 9);
        }

        // 6. 整体乱序
        Collections.shuffle(items);

        return items;
    }
}
