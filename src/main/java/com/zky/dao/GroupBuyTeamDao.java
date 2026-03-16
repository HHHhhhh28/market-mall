package com.zky.dao;

import com.zky.domain.po.GroupBuyTeam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拼团团队DAO
 */
@Mapper
public interface GroupBuyTeamDao {

    /** 根据团队ID查询团队 */
    GroupBuyTeam selectByTeamId(@Param("teamId") String teamId);

    /**
     * 查询活动下可加入的团队（状态为拼团中 且 未满员 且 未超时）
     * 排除指定用户已参加的团队
     */
    List<GroupBuyTeam> selectJoinableTeams(
            @Param("activityId") String activityId,
            @Param("userId") String userId);

    /** 插入新团队 */
    int insert(GroupBuyTeam team);

    /** 更新当前人数（每次有人加入时 +1） */
    int incrementCurrentPeople(@Param("teamId") String teamId);

    /** 更新团队状态 */
    int updateStatus(@Param("teamId") String teamId, @Param("status") Integer status);

    /** 批量更新超时未成团的团队状态为失败（定时任务用） */
    int updateExpiredTeamsToFailed();
}
