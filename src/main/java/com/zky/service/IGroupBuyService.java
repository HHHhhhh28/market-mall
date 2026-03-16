package com.zky.service;

import com.github.pagehelper.PageInfo;
import com.zky.domain.dto.GroupBuyActivityRequestDTO;
import com.zky.domain.dto.GroupBuyJoinRequestDTO;
import com.zky.domain.vo.GroupBuyActivityVO;
import com.zky.domain.vo.GroupBuyProgressVO;
import com.zky.domain.vo.GroupBuyTeamVO;

import java.util.List;

/**
 * 拼团业务服务接口
 */
public interface IGroupBuyService {

    /**
     * 获取当前所有进行中的拼团活动列表（用于拼团推荐页展示）
     *
     * @param pageNum  页码
     * @param pageSize 每页数量
     * @return 分页后的拼团活动列表
     */
    PageInfo<GroupBuyActivityVO> getActiveGroupBuyList(int pageNum, int pageSize);

    /**
     * 获取某个活动的详情（包含当前可加入的团队列表）
     *
     * @param activityId 活动ID
     * @param userId     当前用户ID
     * @return 活动详情VO
     */
    GroupBuyActivityVO getActivityDetail(String activityId, String userId);

    /**
     * 获取团队详情（含成员列表，用于拼团详情页）
     *
     * @param teamId 团队ID
     * @return 团队详情VO
     */
    GroupBuyTeamVO getTeamDetail(String teamId);

    /**
     * 开团或加团下单
     * - teamId 为空：发起新团（开团），成为团长
     * - teamId 不为空：加入已有团队
     *
     * @param request 参团请求
     * @return 订单ID
     */
    String joinOrCreateTeam(GroupBuyJoinRequestDTO request);

    /**
     * 查询用户所有拼团进度（用于我的拼团进度页）
     *
     * @param userId 用户ID
     * @return 拼团进度列表
     */
    List<GroupBuyProgressVO> getUserGroupBuyProgress(String userId);

    /**
     * 创建或更新拼团活动（管理端使用）
     *
     * @param request 活动配置请求
     * @return 活动ID
     */
    String saveActivity(GroupBuyActivityRequestDTO request);
}
