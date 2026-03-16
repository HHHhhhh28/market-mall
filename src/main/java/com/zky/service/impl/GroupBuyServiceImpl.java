package com.zky.service.impl;

import com.github.pagehelper.PageInfo;
import com.zky.dao.GroupBuyActivityDao;
import com.zky.dao.GroupBuyTeamDao;
import com.zky.dao.GroupBuyTeamMemberDao;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserLotteryDao;
import com.zky.domain.dto.GroupBuyActivityRequestDTO;
import com.zky.domain.dto.GroupBuyJoinRequestDTO;
import com.zky.domain.po.GroupBuyActivity;
import com.zky.domain.po.GroupBuyTeam;
import com.zky.domain.po.GroupBuyTeamMember;
import com.zky.domain.po.OrderInfo;
import com.zky.domain.po.OrderItem;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserLottery;
import com.zky.domain.vo.GroupBuyActivityVO;
import com.zky.domain.vo.GroupBuyMemberVO;
import com.zky.domain.vo.GroupBuyProgressVO;
import com.zky.domain.vo.GroupBuyTeamVO;
import com.zky.service.IGroupBuyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/** 拼团业务服务实现 */
@Slf4j
@Service
public class GroupBuyServiceImpl implements IGroupBuyService {

    @Resource
    private GroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private GroupBuyTeamDao groupBuyTeamDao;
    @Resource
    private GroupBuyTeamMemberDao groupBuyTeamMemberDao;
    @Resource
    private ProductDao productDao;
    @Resource
    private OrderDao orderDao;
    @Resource
    private UserLotteryDao userLotteryDao;

    // ========== 拼团活动列表 ==========
    @Override
    public PageInfo<GroupBuyActivityVO> getActiveGroupBuyList(int pageNum, int pageSize) {
        List<GroupBuyActivity> activities = groupBuyActivityDao.selectActiveList();
        List<GroupBuyActivityVO> voList = activities.stream()
                .map(this::convertActivityToVO).collect(Collectors.toList());
        int total = voList.size();
        int pn = pageNum > 0 ? pageNum : 1;
        int ps = pageSize > 0 ? pageSize : 9;
        int start = (pn - 1) * ps;
        List<GroupBuyActivityVO> pagedList = new ArrayList<>();
        if (start < total) {
            pagedList = voList.subList(start, Math.min(start + ps, total));
        }
        PageInfo<GroupBuyActivityVO> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(pn);
        pageInfo.setPageSize(ps);
        pageInfo.setTotal(total);
        pageInfo.setList(pagedList);
        return pageInfo;
    }

    // ========== 活动详情 ==========
    @Override
    public GroupBuyActivityVO getActivityDetail(String activityId, String userId) {
        GroupBuyActivity activity = groupBuyActivityDao.selectByActivityId(activityId);
        if (activity == null) {
            throw new RuntimeException("拼团活动不存在：" + activityId);
        }
        GroupBuyActivityVO vo = convertActivityToVO(activity);
        List<GroupBuyTeam> joinableTeams = groupBuyTeamDao.selectJoinableTeams(activityId, userId);
        vo.setOpenTeamCount(joinableTeams == null ? 0 : joinableTeams.size());
        return vo;
    }

    // ========== 团队详情 ==========
    @Override
    public GroupBuyTeamVO getTeamDetail(String teamId) {
        GroupBuyTeam team = groupBuyTeamDao.selectByTeamId(teamId);
        if (team == null) {
            throw new RuntimeException("拼团团队不存在：" + teamId);
        }
        return convertTeamToVO(team);
    }

    // ========== 开团/加团核心下单 ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String joinOrCreateTeam(GroupBuyJoinRequestDTO request) {
        // 1. 校验活动
        GroupBuyActivity activity = groupBuyActivityDao.selectByActivityId(request.getActivityId());
        if (activity == null || activity.getStatus() != 1) {
            throw new RuntimeException("拼团活动不存在或已结束");
        }
        Date now = new Date();
        if (now.before(activity.getStartTime()) || now.after(activity.getEndTime())) {
            throw new RuntimeException("拼团活动不在有效期内");
        }
        // 2. 校验商品
        ProductInfo product = productDao.selectByProductId(request.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1) {
            throw new RuntimeException("商品不存在或已下架");
        }
        if (product.getStock() == null || product.getStock() < 1) {
            throw new RuntimeException("商品【" + product.getName() + "】库存不足");
        }
        // 3. 确定团队：加入已有团队 或 新开一个团
        GroupBuyTeam team;
        boolean isLeader = false;
        if (request.getTeamId() != null && !request.getTeamId().isEmpty()) {
            // 加入已有团队
            team = groupBuyTeamDao.selectByTeamId(request.getTeamId());
            if (team == null || team.getStatus() != 0) {
                throw new RuntimeException("该拼团团队不存在或已结束");
            }
            if (team.getEndTime().before(now)) {
                throw new RuntimeException("该拼团团队已超时");
            }
            if (team.getCurrentPeople() >= team.getRequiredPeople()) {
                throw new RuntimeException("该拼团团队已满员");
            }
            // 检查用户是否已在该团队
            GroupBuyTeamMember existMember = groupBuyTeamMemberDao
                    .selectByTeamIdAndUserId(team.getTeamId(), request.getUserId());
            if (existMember != null) {
                throw new RuntimeException("您已参加过该拼团");
            }
        } else {
            // 发起新团，成为团长
            isLeader = true;
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.HOUR_OF_DAY, activity.getValidDuration());
            team = new GroupBuyTeam();
            team.setTeamId(UUID.randomUUID().toString());
            team.setActivityId(activity.getActivityId());
            team.setProductId(activity.getProductId());
            team.setLeaderUserId(request.getUserId());
            team.setRequiredPeople(activity.getRequiredPeople());
            team.setCurrentPeople(0);
            team.setStatus(0);
            team.setGroupBuyPrice(activity.getGroupBuyPrice());
            team.setStartTime(now);
            team.setEndTime(cal.getTime());
            groupBuyTeamDao.insert(team);
        }
        // 4. 创建订单
        String orderId = UUID.randomUUID().toString();
        BigDecimal payPrice = team.getGroupBuyPrice();
        OrderItem orderItem = new OrderItem();
        orderItem.setOrderId(orderId);
        orderItem.setProductId(product.getProductId());
        orderItem.setProductName(product.getName());
        orderItem.setProductImage(product.getImageUrl());
        orderItem.setPrice(payPrice);
        orderItem.setQuantity(1);
        orderItem.setProductType("GROUP_BUY");
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderId(orderId);
        orderInfo.setUserId(request.getUserId());
        orderInfo.setTotalPrice(payPrice);
        orderInfo.setStatus("0"); // 拼团中，等待成团
        orderInfo.setAddress(request.getAddress());
        orderInfo.setContactName(request.getContactName());
        orderInfo.setContactPhone(request.getContactPhone());
        orderInfo.setCreateTime(now);
        orderInfo.setUpdateTime(now);
        orderDao.insertOrder(orderInfo);
        orderDao.insertOrderItem(orderItem);
        // 5. 扣减库存（乐观锁）
        int updateRow = productDao.updateStockReduce(product.getProductId(), 1);
        if (updateRow == 0) {
            throw new RuntimeException("商品【" + product.getName() + "】库存扣减失败");
        }
        // 6. 记录成员参团信息
        GroupBuyTeamMember member = new GroupBuyTeamMember();
        member.setMemberId(UUID.randomUUID().toString());
        member.setTeamId(team.getTeamId());
        member.setActivityId(activity.getActivityId());
        member.setProductId(activity.getProductId());
        member.setUserId(request.getUserId());
        member.setOrderId(orderId);
        member.setPayPrice(payPrice);
        member.setIsLeader(isLeader ? 1 : 0);
        member.setAddress(request.getAddress());
        member.setContactName(request.getContactName());
        member.setContactPhone(request.getContactPhone());
        member.setStatus(0);
        groupBuyTeamMemberDao.insert(member);
        // 7. 团队人数 +1，判断是否已成团
        groupBuyTeamDao.incrementCurrentPeople(team.getTeamId());
        GroupBuyTeam updatedTeam = groupBuyTeamDao.selectByTeamId(team.getTeamId());
        if (updatedTeam.getCurrentPeople() >= updatedTeam.getRequiredPeople()) {
            // 成团：更新团队状态和所有成员状态
            groupBuyTeamDao.updateStatus(team.getTeamId(), 1);
            groupBuyTeamMemberDao.updateStatusByTeamId(team.getTeamId(), 1);
            log.info("拼团成功！团队ID={}，商品={}，已满{}人",
                    team.getTeamId(), product.getName(), updatedTeam.getCurrentPeople());
        }
        // 8. 参团奖励抽奖次数
        increaseLotteryCount(request.getUserId(), 1);
        log.info("用户{}{}，团队ID={}，订单ID={}",
                request.getUserId(), isLeader ? "开团" : "加团", team.getTeamId(), orderId);
        return orderId;
    }

    // ========== 我的拼团进度 ==========
    @Override
    public List<GroupBuyProgressVO> getUserGroupBuyProgress(String userId) {
        List<GroupBuyTeamMember> members = groupBuyTeamMemberDao.selectByUserId(userId);
        if (members == null || members.isEmpty()) {
            return new ArrayList<>();
        }
        List<GroupBuyProgressVO> progressList = new ArrayList<>();
        for (GroupBuyTeamMember member : members) {
            GroupBuyTeam team = groupBuyTeamDao.selectByTeamId(member.getTeamId());
            if (team == null) { continue; }
            ProductInfo product = productDao.selectByProductId(member.getProductId());
            GroupBuyProgressVO vo = new GroupBuyProgressVO();
            vo.setTeamId(team.getTeamId());
            vo.setActivityId(team.getActivityId());
            vo.setProductId(member.getProductId());
            vo.setProductName(product != null ? product.getName() : "");
            vo.setImageUrl(product != null ? product.getImageUrl() : "");
            vo.setGroupBuyPrice(team.getGroupBuyPrice());
            vo.setRequiredPeople(team.getRequiredPeople());
            vo.setCurrentPeople(team.getCurrentPeople());
            vo.setRemainingPeople(Math.max(0, team.getRequiredPeople() - team.getCurrentPeople()));
            vo.setStatus(team.getStatus());
            vo.setEndTime(team.getEndTime());
            vo.setCountdownDesc(buildCountdownDesc(team.getEndTime()));
            vo.setIsLeader(member.getIsLeader());
            vo.setOrderId(member.getOrderId());
            progressList.add(vo);
        }
        return progressList;
    }

    // ========== 管理端：创建/更新活动 ==========
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveActivity(GroupBuyActivityRequestDTO request) {
        ProductInfo product = productDao.selectByProductId(request.getProductId());
        if (product == null) {
            throw new RuntimeException("商品不存在：" + request.getProductId());
        }
        if (request.getActivityId() != null && !request.getActivityId().isEmpty()) {
            GroupBuyActivity existing = groupBuyActivityDao.selectByActivityId(request.getActivityId());
            if (existing == null) { throw new RuntimeException("活动不存在：" + request.getActivityId()); }
            existing.setActivityName(request.getActivityName());
            existing.setGroupBuyPrice(request.getGroupBuyPrice());
            existing.setRequiredPeople(request.getRequiredPeople());
            existing.setValidDuration(request.getValidDuration());
            existing.setStartTime(request.getStartTime());
            existing.setEndTime(request.getEndTime());
            groupBuyActivityDao.update(existing);
            return existing.getActivityId();
        } else {
            String activityId = UUID.randomUUID().toString();
            GroupBuyActivity activity = new GroupBuyActivity();
            activity.setActivityId(activityId);
            activity.setProductId(request.getProductId());
            activity.setActivityName(request.getActivityName());
            activity.setGroupBuyPrice(request.getGroupBuyPrice());
            activity.setOriginalPrice(product.getPrice());
            activity.setRequiredPeople(request.getRequiredPeople());
            activity.setValidDuration(request.getValidDuration());
            activity.setStatus(1);
            activity.setStartTime(request.getStartTime());
            activity.setEndTime(request.getEndTime());
            groupBuyActivityDao.insert(activity);
            return activityId;
        }
    }

    // ========== 私有辅助方法 ==========

    /** 活动PO转VO，补充商品快照 */
    private GroupBuyActivityVO convertActivityToVO(GroupBuyActivity activity) {
        GroupBuyActivityVO vo = new GroupBuyActivityVO();
        vo.setActivityId(activity.getActivityId());
        vo.setProductId(activity.getProductId());
        vo.setGroupBuyPrice(activity.getGroupBuyPrice());
        vo.setOriginalPrice(activity.getOriginalPrice());
        vo.setRequiredPeople(activity.getRequiredPeople());
        vo.setValidDuration(activity.getValidDuration());
        vo.setStatus(activity.getStatus());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        ProductInfo product = productDao.selectByProductId(activity.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setImageUrl(product.getImageUrl());
        }
        return vo;
    }

    /** 团队PO转VO，含成员列表 */
    private GroupBuyTeamVO convertTeamToVO(GroupBuyTeam team) {
        GroupBuyTeamVO vo = new GroupBuyTeamVO();
        vo.setTeamId(team.getTeamId());
        vo.setActivityId(team.getActivityId());
        vo.setProductId(team.getProductId());
        vo.setLeaderUserId(team.getLeaderUserId());
        vo.setRequiredPeople(team.getRequiredPeople());
        vo.setCurrentPeople(team.getCurrentPeople());
        vo.setRemainingPeople(Math.max(0, team.getRequiredPeople() - team.getCurrentPeople()));
        vo.setStatus(team.getStatus());
        vo.setGroupBuyPrice(team.getGroupBuyPrice());
        vo.setStartTime(team.getStartTime());
        vo.setEndTime(team.getEndTime());
        vo.setCountdownDesc(buildCountdownDesc(team.getEndTime()));
        ProductInfo product = productDao.selectByProductId(team.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setImageUrl(product.getImageUrl());
            vo.setOriginalPrice(product.getPrice());
        }
        List<GroupBuyTeamMember> members = groupBuyTeamMemberDao.selectByTeamId(team.getTeamId());
        List<GroupBuyMemberVO> memberVOs = members.stream().map(m -> {
            GroupBuyMemberVO mvo = new GroupBuyMemberVO();
            mvo.setUserId(m.getUserId());
            mvo.setIsLeader(m.getIsLeader());
            mvo.setPayPrice(m.getPayPrice());
            mvo.setStatus(m.getStatus());
            mvo.setJoinTime(m.getJoinTime());
            return mvo;
        }).collect(Collectors.toList());
        vo.setMembers(memberVOs);
        return vo;
    }

    /** 计算倒计时描述 */
    private String buildCountdownDesc(Date endTime) {
        if (endTime == null) { return "已结束"; }
        long diff = endTime.getTime() - System.currentTimeMillis();
        if (diff <= 0) { return "已结束"; }
        long hours = diff / (1000 * 60 * 60);
        long minutes = (diff % (1000 * 60 * 60)) / (1000 * 60);
        return hours + "小时" + minutes + "分";
    }

    /** 增加用户抽奖次数 */
    private void increaseLotteryCount(String userId, int delta) {
        UserLottery record = userLotteryDao.selectByUserId(userId);
        if (record == null) {
            UserLottery insert = new UserLottery();
            insert.setUserId(userId);
            insert.setLotteryCount(Math.max(delta, 0));
            userLotteryDao.insert(insert);
            return;
        }
        int current = record.getLotteryCount() == null ? 0 : record.getLotteryCount();
        record.setLotteryCount(current + delta);
        userLotteryDao.update(record);
    }
}
