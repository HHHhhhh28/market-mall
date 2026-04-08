package com.zky.dao;

import com.zky.domain.po.LotteryCouponStrategy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LotteryCouponStrategyDao {

    /** 根据 couponId 查询策略 */
    LotteryCouponStrategy selectByCouponId(@Param("couponId") String couponId);

    /** 查询所有策略（管理端分页用） */
    List<LotteryCouponStrategy> selectAll();

    /** 查询已上架的策略（包含保底） */
    List<LotteryCouponStrategy> selectOnlineList();

    /** 查询保底优惠券策略 */
    List<LotteryCouponStrategy> selectFallbackList();

    /** 查询已上架的指定品类策略 */
    List<LotteryCouponStrategy> selectOnlineByCategories(
            @Param("categories") List<String> categories);

    /** 插入新策略 */
    int insert(LotteryCouponStrategy strategy);

    /** 更新策略（算法重算后更新） */
    int update(LotteryCouponStrategy strategy);

    /** 更新上架状态 */
    int updateStatus(
            @Param("couponId") String couponId,
            @Param("status") Integer status);

    /** 删除策略 */
    int deleteByCouponId(@Param("couponId") String couponId);
}
