package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.vo.UserCouponVO;
import com.zky.service.ICouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/coupon")
public class CouponController {

    @Resource
    private ICouponService couponService;

    @GetMapping("/list")
    public Response<List<UserCouponVO>> list(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/coupon/list");
        List<UserCouponVO> list = couponService.getUserCoupons(userId);
        return Response.<List<UserCouponVO>>builder().code("0000").info("Success").data(list).build();
    }
}

