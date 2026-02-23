package com.zky.service.impl;

import com.zky.dao.CouponDao;
import com.zky.dao.UserCouponDao;
import com.zky.domain.po.Coupon;
import com.zky.domain.po.UserCoupon;
import com.zky.domain.vo.UserCouponVO;
import com.zky.service.ICouponService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class CouponServiceImpl implements ICouponService {

    @Resource
    private UserCouponDao userCouponDao;
    @Resource
    private CouponDao couponDao;

    @Override
    public List<UserCouponVO> getUserCoupons(String userId) {
        List<UserCoupon> list = userCouponDao.selectByUserId(userId);
        List<UserCouponVO> result = new ArrayList<>();
        for (UserCoupon uc : list) {
            Coupon coupon = couponDao.selectByCouponId(uc.getCouponId());
            if (coupon == null) {
                continue;
            }
            UserCouponVO vo = new UserCouponVO();
            vo.setCouponId(coupon.getCouponId());
            vo.setName(coupon.getName());
            vo.setCategory(coupon.getCategory());
            vo.setCouponType(coupon.getCouponType());
            vo.setValue(coupon.getValue());
            vo.setStatus(uc.getStatus());
            result.add(vo);
        }
        return result;
    }
}

