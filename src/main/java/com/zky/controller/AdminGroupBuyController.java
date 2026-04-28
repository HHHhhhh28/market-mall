package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.dao.GroupBuyActivityDao;
import com.zky.dao.GroupBuyProductDao;
import com.zky.dao.GroupBuyTeamDao;
import com.zky.dao.GroupBuyTeamMemberDao;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserDao;
import com.zky.domain.po.GroupBuyActivity;
import com.zky.domain.po.GroupBuyProduct;
import com.zky.domain.po.GroupBuyTeam;
import com.zky.domain.po.GroupBuyTeamMember;
import com.zky.domain.po.OrderItem;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import com.zky.domain.vo.GroupBuyStrategyVO;
import com.zky.service.IGroupBuyStrategyService;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 后台拼团策略管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/mall/admin/group-buy")
public class AdminGroupBuyController {

    @Resource
    private IGroupBuyStrategyService groupBuyStrategyService;
    @Resource
    private GroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private GroupBuyProductDao groupBuyProductDao;
    @Resource
    private GroupBuyTeamDao groupBuyTeamDao;
    @Resource
    private GroupBuyTeamMemberDao groupBuyTeamMemberDao;
    @Resource
    private ProductDao productDao;
    @Resource
    private UserDao userDao;
    @Resource
    private OrderDao orderDao;

    /**
     * 获取或创建当前全局活动
     */
    @GetMapping("/active-activity")
    public Response<GroupBuyActivity> getOrCreateActivity() {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/group-buy/active-activity");
        try {
            List<GroupBuyActivity> list = groupBuyActivityDao.selectActiveList();
            if (list != null && !list.isEmpty()) {
                return Response.<GroupBuyActivity>builder()
                        .code("0000").info("Success").data(list.get(0)).build();
            }
            GroupBuyActivity activity = new GroupBuyActivity();
            activity.setActivityId(UUID.randomUUID().toString());
            activity.setRequiredPeople(2);
            activity.setValidDuration(24);
            activity.setStatus(1);
            activity.setStartTime(new Date());
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.YEAR, 1);
            activity.setEndTime(cal.getTime());
            groupBuyActivityDao.insert(activity);
            return Response.<GroupBuyActivity>builder()
                    .code("0000").info("Success").data(activity).build();
        } catch (Exception e) {
            log.error("获取全局活动失败", e);
            return Response.<GroupBuyActivity>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 批量查询商品上架状态
     * 前端传入商品ID列表（逗号分隔），返回 productId -> onlineStatus(0未上架,1已上架) 的 Map
     */
    @GetMapping("/online-status-batch")
    public Response<Map<String, Integer>> getOnlineStatusBatch(
            @RequestParam("productIds") List<String> productIds) {
        log.info("接口 {} 被调用了，productIds={}", "/api/v1/mall/admin/group-buy/online-status-batch", productIds);
        try {
            Map<String, Integer> result = new HashMap<>();
            for (String productId : productIds) {
                GroupBuyProduct gbp = groupBuyProductDao.selectLatestByProductId(productId);
                // status=1 为已上架，其余（0未上架、2已下架、null）统一为0（未上架）
                int status = (gbp != null && gbp.getStatus() == 1) ? 1 : 0;
                result.put(productId, status);
            }
            return Response.<Map<String, Integer>>builder()
                    .code("0000").info("Success").data(result).build();
        } catch (Exception e) {
            log.error("批量查询上架状态失败", e);
            return Response.<Map<String, Integer>>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 策略算法分析：返回热度数据、建议价格、推送用户标签
     */
    @GetMapping("/analyze")
    public Response<GroupBuyStrategyVO> analyze(
            @RequestParam String productId,
            @RequestParam String activityId) {
        log.info("接口 {} 被调用了，productId={}, activityId={}", "/api/v1/mall/admin/group-buy/analyze", productId, activityId);
        try {
            GroupBuyStrategyVO vo = groupBuyStrategyService.analyzeProduct(productId, activityId);
            return Response.<GroupBuyStrategyVO>builder()
                    .code("0000").info("Success").data(vo).build();
        } catch (Exception e) {
            log.error("策略分析失败 productId={}", productId, e);
            return Response.<GroupBuyStrategyVO>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 上架商品（推送到目标用户拼团页面）
     */
    @PostMapping("/online")
    public Response<String> online(
            @RequestParam String productId,
            @RequestParam String activityId) {
        log.info("接口 {} 被调用了，productId={}, activityId={}", "/api/v1/mall/admin/group-buy/online", productId, activityId);
        try {
            groupBuyStrategyService.onlineProduct(productId, activityId);
            return Response.<String>builder()
                    .code("0000").info("Success").data("上架成功").build();
        } catch (Exception e) {
            log.error("商品上架失败 productId={}", productId, e);
            return Response.<String>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 下架商品
     */
    @PostMapping("/offline")
    public Response<String> offline(
            @RequestParam String productId,
            @RequestParam String activityId) {
        log.info("接口 {} 被调用了，productId={}, activityId={}", "/api/v1/mall/admin/group-buy/offline", productId, activityId);
        try {
            groupBuyStrategyService.offlineProduct(productId, activityId);
            return Response.<String>builder()
                    .code("0000").info("Success").data("下架成功").build();
        } catch (Exception e) {
            log.error("商品下架失败 productId={}", productId, e);
            return Response.<String>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 更新拼团（保持上架，重新计算定价写入数据库）
     */
    @PostMapping("/refresh")
    public Response<String> refresh(
            @RequestParam String productId,
            @RequestParam String activityId) {
        log.info("接口 {} 被调用了，productId={}, activityId={}", "/api/v1/mall/admin/group-buy/refresh", productId, activityId);
        try {
            groupBuyStrategyService.refreshProduct(productId, activityId);
            return Response.<String>builder()
                    .code("0000").info("Success").data("更新成功").build();
        } catch (Exception e) {
            log.error("拼团更新失败 productId={}", productId, e);
            return Response.<String>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 查询用户历史购买记录（商品名+价格）
     */
    @GetMapping("/user-purchase-history")
    public Response<List<GroupBuyStrategyVO.UserPurchaseHistoryVO>> getUserPurchaseHistory(
            @RequestParam String userId) {
        log.info("接口 {} 被调用了，userId={}", "/api/v1/mall/admin/group-buy/user-purchase-history", userId);
        try {
            List<OrderItem> items = orderDao.selectOrderItemsByUserId(userId);
            List<GroupBuyStrategyVO.UserPurchaseHistoryVO> result = new ArrayList<>();
            if (items != null) {
                for (OrderItem item : items) {
                    GroupBuyStrategyVO.UserPurchaseHistoryVO vo = new GroupBuyStrategyVO.UserPurchaseHistoryVO();
                    vo.setProductName(item.getProductName());
                    vo.setPrice(item.getPrice());
                    result.add(vo);
                }
            }
            return Response.<List<GroupBuyStrategyVO.UserPurchaseHistoryVO>>builder()
                    .code("0000").info("Success").data(result).build();
        } catch (Exception e) {
            log.error("查询用户购买历史失败 userId={}", userId, e);
            return Response.<List<GroupBuyStrategyVO.UserPurchaseHistoryVO>>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    /**
     * 查询所有用户拼团进度（管理端）
     */
    @GetMapping("/all-progress")
    public Response<List<AdminGroupProgressVO>> getAllGroupBuyProgress() {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/group-buy/all-progress");
        try {
            // 先将超时未成团的团队及其成员标记为失败
            groupBuyTeamDao.updateExpiredTeamsToFailed();
            groupBuyTeamMemberDao.updateStatusForExpiredTeams();
            List<GroupBuyTeam> teams = groupBuyTeamDao.selectAllTeams();
            List<AdminGroupProgressVO> result = new ArrayList<>();
            for (GroupBuyTeam team : teams) {
                AdminGroupProgressVO vo = new AdminGroupProgressVO();
                vo.setTeamId(team.getTeamId());
                vo.setActivityId(team.getActivityId());
                vo.setProductId(team.getProductId());
                vo.setRequiredPeople(team.getRequiredPeople());
                vo.setCurrentPeople(team.getCurrentPeople());
                vo.setGroupBuyPrice(team.getGroupBuyPrice());
                vo.setStatus(team.getStatus());
                vo.setStartTime(team.getStartTime());
                vo.setEndTime(team.getEndTime());
                vo.setCreateTime(team.getCreateTime());
                // 商品名称
                try {
                    ProductInfo product = productDao.selectByProductId(team.getProductId());
                    if (product != null) {
                        vo.setProductName(product.getName());
                        vo.setProductImage(product.getImageUrl());
                        vo.setOriginalPrice(product.getPrice());
                    }
                } catch (Exception ignored) {}
                // 团长用户名
                try {
                    UserInfo leader = userDao.selectByUserId(team.getLeaderUserId());
                    if (leader != null) vo.setLeaderUsername(leader.getUsername());
                    vo.setLeaderUserId(team.getLeaderUserId());
                } catch (Exception ignored) {}
                // 团队成员列表
                try {
                    List<GroupBuyTeamMember> members = groupBuyTeamMemberDao.selectByTeamId(team.getTeamId());
                    List<AdminGroupProgressVO.MemberVO> memberVOs = new ArrayList<>();
                    for (GroupBuyTeamMember m : members) {
                        AdminGroupProgressVO.MemberVO mvo = new AdminGroupProgressVO.MemberVO();
                        mvo.setUserId(m.getUserId());
                        mvo.setIsLeader(m.getIsLeader());
                        mvo.setJoinTime(m.getJoinTime());
                        mvo.setOrderId(m.getOrderId());
                        mvo.setPayPrice(m.getPayPrice());
                        try {
                            UserInfo u = userDao.selectByUserId(m.getUserId());
                            if (u != null) {
                                mvo.setUsername(u.getUsername());
                                mvo.setPhone(u.getPhone());
                                mvo.setCity(u.getCity());
                                mvo.setAge(u.getAge());
                                mvo.setGender(u.getGender());
                            }
                        } catch (Exception ignored) {}
                        memberVOs.add(mvo);
                    }
                    vo.setMembers(memberVOs);
                } catch (Exception ignored) {}
                result.add(vo);
            }
            return Response.<List<AdminGroupProgressVO>>builder()
                    .code("0000").info("Success").data(result).build();
        } catch (Exception e) {
            log.error("查询所有拼团进度失败", e);
            return Response.<List<AdminGroupProgressVO>>builder()
                    .code("0001").info(e.getMessage()).build();
        }
    }

    @Data
    public static class AdminGroupProgressVO {
        private String teamId;
        private String activityId;
        private String productId;
        private String productName;
        private String productImage;
        private BigDecimal originalPrice;
        private BigDecimal groupBuyPrice;
        private String leaderUserId;
        private String leaderUsername;
        private Integer requiredPeople;
        private Integer currentPeople;
        private Integer status;
        private Date startTime;
        private Date endTime;
        private Date createTime;
        private List<MemberVO> members;

        @Data
        public static class MemberVO {
            private String userId;
            private String username;
            private String phone;
            private String city;
            private Integer age;
            private Integer gender;
            private Integer isLeader;
            private Date joinTime;
            private String orderId;
            private BigDecimal payPrice;
        }
    }
}
