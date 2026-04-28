package com.zky.dao;

import com.zky.domain.po.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserBehaviorDao {
    List<UserBehavior> selectAll();

    int insert(UserBehavior behavior);

    /**
     * 查询近N天有用户行为的商品ID
     */
    List<String> selectProductIdsWithBehavior(@Param("days") Integer days);

    /**
     * 查询指定用户近N天有行为的商品ID
     */
    List<String> selectProductIdsByUserAndDays(@Param("userId") String userId, @Param("days") Integer days);



    Integer selectDistinctUserCountByProductId(
            @Param("productId") String productId,
            @Param("days") Integer days
    );

    /**
     * 查询指定用户的品类行为加权统计
     * 权重：view=1, cart/collect=3, buy=5
     * 返回按权重降序排列的品类列表
     */
    List<java.util.Map<String, Object>> selectCategoryWeightByUser(@Param("userId") String userId);
}
