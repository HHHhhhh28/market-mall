package com.zky.dao;

import com.zky.domain.po.UserCoupon;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户优惠券持有记录 DAO
 */
@Mapper
public interface UserCouponDao {

    /**
     * 按用户ID查询全部优惠券
     */
    List<UserCoupon> selectByUserId(@Param("userId") String userId);

    /**
     * 查询用户某张未使用的优惠券
     */
    UserCoupon selectAvailableOne(@Param("userId") String userId, @Param("couponId") String couponId);

    /**
     * 查询用户指定品类下未使用的优惠券
     */
    List<UserCoupon> selectAvailableByCategory(@Param("userId") String userId, @Param("category") String category);

    /**
     * 新增用户优惠券记录
     */
    int insert(UserCoupon record);

    /**
     * 标记优惠券为已使用
     */
    int markUsed(@Param("userId") String userId, @Param("couponId") String couponId);
}

