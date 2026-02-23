package com.zky.service.impl;

import com.zky.algorithm.impl.UserCFModel;
import com.zky.dao.CouponDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserCouponDao;
import com.zky.dao.UserDao;
import com.zky.dao.UserLotteryDao;
import com.zky.domain.po.Coupon;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserCoupon;
import com.zky.domain.po.UserInfo;
import com.zky.domain.po.UserLottery;
import com.zky.domain.vo.LotteryDrawResultVO;
import com.zky.domain.vo.LotteryGridItemVO;
import com.zky.domain.vo.LotteryInfoVO;
import com.zky.service.ILotteryService;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LotteryServiceImpl implements ILotteryService {

    @Resource
    private UserLotteryDao userLotteryDao;
    @Resource
    private UserDao userDao;
    @Resource
    private ProductDao productDao;
    @Resource
    private UserCFModel userCFModel;
    @Resource
    private CouponDao couponDao;
    @Resource
    private UserCouponDao userCouponDao;

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
        List<String> topCategories = getTopCategories(userId);
        if (CollectionUtils.isEmpty(topCategories)) {
            return Collections.emptyList();
        }
        return buildGridItems(topCategories);
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

    private List<String> getTopCategories(String userId) {
        UserInfo user = userDao.selectByUserId(userId);
        List<ProductInfo> candidates = productDao.selectAll();
        List<ProductInfo> recommend = userCFModel.recommend(user, candidates);
        if (CollectionUtils.isEmpty(recommend)) {
            return Collections.emptyList();
        }
        List<String> categories = new ArrayList<>();
        for (ProductInfo p : recommend) {
            if (p.getCategory() == null) {
                continue;
            }
            if (!categories.contains(p.getCategory())) {
                categories.add(p.getCategory());
            }
            if (categories.size() >= 3) {
                break;
            }
        }
        return categories;
    }

    private List<LotteryGridItemVO> buildGridItems(List<String> topCategories) {
        int categoryCount = topCategories.size();
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

        List<Coupon> couponsPool = new ArrayList<>();
        if (categoryCount >= 3) {
            for (String category : topCategories.stream().limit(3).collect(Collectors.toList())) {
                List<Coupon> coupons = couponDao.selectAvailableByCategory(category);
                if (CollectionUtils.isEmpty(coupons)) {
                    continue;
                }
                Collections.shuffle(coupons);
                int take = Math.min(2, coupons.size());
                couponsPool.addAll(coupons.subList(0, take));
            }
        } else if (categoryCount == 2) {
            for (String category : topCategories) {
                List<Coupon> coupons = couponDao.selectAvailableByCategory(category);
                if (!CollectionUtils.isEmpty(coupons)) {
                    couponsPool.addAll(coupons);
                }
            }
        } else if (categoryCount == 1) {
            String category = topCategories.get(0);
            List<Coupon> coupons = couponDao.selectAvailableByCategory(category);
            if (!CollectionUtils.isEmpty(coupons)) {
                couponsPool.addAll(coupons);
                couponsPool.addAll(coupons);
            }
        }

        if (couponsPool.size() > 6) {
            couponsPool = couponsPool.subList(0, 6);
        }

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

        while (items.size() < 9) {
            LotteryGridItemVO placeholder = new LotteryGridItemVO();
            placeholder.setType("NONE");
            placeholder.setName("谢谢惠顾");
            items.add(placeholder);
        }

        if (items.size() > 9) {
            items = items.subList(0, 9);
        }
        return items;
    }
}
