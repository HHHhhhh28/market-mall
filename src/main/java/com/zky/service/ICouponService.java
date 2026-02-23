package com.zky.service;

import com.zky.domain.vo.UserCouponVO;

import java.util.List;

public interface ICouponService {
    java.util.List<UserCouponVO> getUserCoupons(String userId);
}

