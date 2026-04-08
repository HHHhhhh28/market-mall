package com.zky.dao;

import com.zky.domain.po.GroupBuyTeamMember;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拼团团队成员DAO
 */
@Mapper
public interface GroupBuyTeamMemberDao {

    /** 查询团队的所有成员 */
    List<GroupBuyTeamMember> selectByTeamId(@Param("teamId") String teamId);

    /** 查询用户在某团队的成员记录 */
    GroupBuyTeamMember selectByTeamIdAndUserId(
            @Param("teamId") String teamId,
            @Param("userId") String userId);

    /** 查询用户所有参团记录（用于我的拼团进度） */
    List<GroupBuyTeamMember> selectByUserId(@Param("userId") String userId);

    /** 插入成员记录 */
    int insert(GroupBuyTeamMember member);

    /** 更新成员关联的订单ID */
    int updateOrderId(
            @Param("memberId") String memberId,
            @Param("orderId") String orderId);

    /** 批量更新团队所有成员状态 */
    int updateStatusByTeamId(
            @Param("teamId") String teamId,
            @Param("status") Integer status);

    /** 将超时未成团的团队成员状态更新为失败 */
    int updateStatusForExpiredTeams();
}
