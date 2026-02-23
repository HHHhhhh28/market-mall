package com.zky.dao;

import com.zky.algorithm.impl.UserProfileModel;
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
     * 查询单商品近N天的加权用户画像统计
     */
    List<UserProfileModel.BehaviorUserProfile> selectWeightedUserProfileByProductId(
            @Param("productId") String productId,
            @Param("days") Integer days
    );

    Integer selectDistinctUserCountByProductId(
            @Param("productId") String productId,
            @Param("days") Integer days
    );
}
