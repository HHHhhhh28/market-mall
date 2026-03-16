package com.zky.dao;

import com.zky.domain.po.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拼团活动DAO
 */
@Mapper
public interface GroupBuyActivityDao {

    /** 根据活动ID查询活动 */
    GroupBuyActivity selectByActivityId(@Param("activityId") String activityId);

    /** 根据商品ID查询当前进行中的活动（status=1 且在有效期内） */
    GroupBuyActivity selectActiveByProductId(@Param("productId") String productId);

    /** 查询所有进行中的拼团活动列表（含商品信息连接） */
    List<GroupBuyActivity> selectActiveList();

    /** 插入新活动 */
    int insert(GroupBuyActivity activity);

    /** 更新活动 */
    int update(GroupBuyActivity activity);
}
