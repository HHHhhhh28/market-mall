package com.zky.dao;

import com.zky.domain.po.GroupBuyActivity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 全局拼团活动DAO
 */
@Mapper
public interface GroupBuyActivityDao {

    /** 根据活动ID查询活动 */
    GroupBuyActivity selectByActivityId(@Param("activityId") String activityId);

    /** 查询所有进行中的拼团活动 */
    List<GroupBuyActivity> selectActiveList();

    /** 插入新活动 */
    int insert(GroupBuyActivity activity);

    /** 更新活动状态 */
    int update(GroupBuyActivity activity);
}
