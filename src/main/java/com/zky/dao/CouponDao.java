package com.zky.dao;

import com.zky.domain.po.Coupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 平台优惠券配置 DAO
 */
@Mapper
public interface CouponDao {

    /**
     * 按优惠券ID查询
     */
    Coupon selectByCouponId(@Param("couponId") String couponId);

    /**
     * 查询指定品类下可用优惠券
     */
    List<Coupon> selectAvailableByCategory(@Param("category") String category);

    /**
     * 查询多品类下可用优惠券
     */
    List<Coupon> selectAvailableByCategories(@Param("categories") List<String> categories);
}

