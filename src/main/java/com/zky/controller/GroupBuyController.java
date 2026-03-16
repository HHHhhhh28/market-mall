package com.zky.controller;

import com.github.pagehelper.PageInfo;
import com.zky.common.response.Response;
import com.zky.domain.dto.GroupBuyActivityRequestDTO;
import com.zky.domain.dto.GroupBuyJoinRequestDTO;
import com.zky.domain.vo.GroupBuyActivityVO;
import com.zky.domain.vo.GroupBuyProgressVO;
import com.zky.domain.vo.GroupBuyTeamVO;
import com.zky.service.IGroupBuyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 拼团模块控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mall/group-buy")
public class GroupBuyController {

    @Resource
    private IGroupBuyService groupBuyService;

    /**
     * 获取所有进行中的拼团活动列表（拼团推荐页）
     */
    @GetMapping("/activities")
    public Response<PageInfo<GroupBuyActivityVO>> getActiveGroupBuyList(
            @RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
            @RequestParam(value = "pageSize", defaultValue = "9") int pageSize) {
        log.info("接口 {} 被调用了", "/api/v1/mall/group-buy/activities");
        PageInfo<GroupBuyActivityVO> result = groupBuyService.getActiveGroupBuyList(pageNum, pageSize);
        return Response.<PageInfo<GroupBuyActivityVO>>builder()
                .code("0000").info("Success").data(result).build();
    }

    /**
     * 获取拼团活动详情（含可加入团队数量）
     */
    @GetMapping("/activity/{activityId}")
    public Response<GroupBuyActivityVO> getActivityDetail(
            @PathVariable String activityId,
            @RequestParam(value = "userId", required = false, defaultValue = "") String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/group-buy/activity/" + activityId);
        GroupBuyActivityVO result = groupBuyService.getActivityDetail(activityId, userId);
        return Response.<GroupBuyActivityVO>builder()
                .code("0000").info("Success").data(result).build();
    }

    /**
     * 获取拼团团队详情（含成员列表，用于拼团详情页）
     */
    @GetMapping("/team/{teamId}")
    public Response<GroupBuyTeamVO> getTeamDetail(@PathVariable String teamId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/group-buy/team/" + teamId);
        GroupBuyTeamVO result = groupBuyService.getTeamDetail(teamId);
        return Response.<GroupBuyTeamVO>builder()
                .code("0000").info("Success").data(result).build();
    }

    /**
     * 开团或加团下单
     * - request.teamId 为空：发起新团（开团）
     * - request.teamId 不为空：加入已有团队
     */
    @PostMapping("/join")
    public Response<String> joinOrCreateTeam(@RequestBody GroupBuyJoinRequestDTO request) {
        log.info("接口 {} 被调用了，userId={}, activityId={}, teamId={}",
                "/api/v1/mall/group-buy/join",
                request.getUserId(), request.getActivityId(), request.getTeamId());
        String orderId = groupBuyService.joinOrCreateTeam(request);
        return Response.<String>builder()
                .code("0000").info("Success").data(orderId).build();
    }

    /**
     * 查询用户拼团进度列表（我的拼团进度页）
     */
    @GetMapping("/progress")
    public Response<List<GroupBuyProgressVO>> getUserGroupBuyProgress(
            @RequestParam(value = "userId") String userId) {
        log.info("接口 {} 被调用了，userId={}", "/api/v1/mall/group-buy/progress", userId);
        List<GroupBuyProgressVO> result = groupBuyService.getUserGroupBuyProgress(userId);
        return Response.<List<GroupBuyProgressVO>>builder()
                .code("0000").info("Success").data(result).build();
    }

    /**
     * 创建或更新拼团活动（管理端使用）
     */
    @PostMapping("/activity/save")
    public Response<String> saveActivity(@RequestBody GroupBuyActivityRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/group-buy/activity/save");
        String activityId = groupBuyService.saveActivity(request);
        return Response.<String>builder()
                .code("0000").info("Success").data(activityId).build();
    }
}
