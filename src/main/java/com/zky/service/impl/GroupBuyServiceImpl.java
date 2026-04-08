package com.zky.service.impl;

import com.github.pagehelper.PageInfo;
import com.zky.dao.GroupBuyActivityDao;
import com.zky.dao.GroupBuyProductDao;
import com.zky.dao.GroupBuyTeamDao;
import com.zky.dao.GroupBuyTeamMemberDao;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.dao.UserLotteryDao;
import com.zky.dao.UserBehaviorDao;
import com.zky.dao.FavoriteDao;
import com.zky.dao.UserDao;
import com.zky.domain.dto.GroupBuyActivityRequestDTO;
import com.zky.domain.dto.GroupBuyJoinRequestDTO;
import com.zky.domain.po.*;
import com.zky.domain.vo.*;
import com.zky.service.IGroupBuyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GroupBuyServiceImpl implements IGroupBuyService {

    @Resource private GroupBuyActivityDao groupBuyActivityDao;
    @Resource private GroupBuyProductDao groupBuyProductDao;
    @Resource private GroupBuyTeamDao groupBuyTeamDao;
    @Resource private GroupBuyTeamMemberDao groupBuyTeamMemberDao;
    @Resource private ProductDao productDao;
    @Resource private OrderDao orderDao;
    @Resource private UserLotteryDao userLotteryDao;
    @Resource private UserBehaviorDao userBehaviorDao;
    @Resource private FavoriteDao favoriteDao;
    @Resource private UserDao userDao;

    @Override
    public PageInfo<GroupBuyActivityVO> getActiveGroupBuyList(int pageNum, int pageSize) {
        return getActiveGroupBuyListForUser(pageNum, pageSize, null);
    }

    /**
     * 获取拼团活动列表，按用户兴趣过滤
     * 如果 userId 为空，返回所有已上架商品
     * 如果 userId 不为空，只返回用户感兴趣的商品（有收藏或行为记录）
     */
    public PageInfo<GroupBuyActivityVO> getActiveGroupBuyListForUser(int pageNum, int pageSize, String userId) {
        // 先将超时未成团的团队及其成员标记为失败
        int expired = groupBuyTeamDao.updateExpiredTeamsToFailed();
        if (expired > 0) {
            groupBuyTeamMemberDao.updateStatusForExpiredTeams();
        }
        List<GroupBuyProduct> online = groupBuyProductDao.selectOnlineList();
        List<GroupBuyActivityVO> vos = online.stream()
                .map(this::convertGbpToVO).filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        // 如果指定了 userId，按用户兴趣过滤
        if (userId != null && !userId.trim().isEmpty()) {
            // 获取用户收藏的商品
            List<FavoriteInfo> favorites = favoriteDao.selectByUserId(userId);
            Set<String> favoriteProductIds = favorites.stream()
                    .map(FavoriteInfo::getProductId)
                    .collect(Collectors.toSet());
            
            // 获取用户有行为记录的商品（近30天）
            List<String> behaviorProductIds = userBehaviorDao.selectProductIdsByUserAndDays(userId, 30);
            Set<String> interestedProductIds = new HashSet<>(favoriteProductIds);
            if (behaviorProductIds != null) {
                interestedProductIds.addAll(behaviorProductIds);
            }
            
        // 只保留用户感兴趣的商品，若无感兴趣商品则返回全部
        List<GroupBuyActivityVO> filtered = vos.stream()
                .filter(vo -> interestedProductIds.contains(vo.getProductId()))
                .collect(Collectors.toList());
        if (!filtered.isEmpty()) {
            vos = filtered;
        }
        }
        
        int total = vos.size(), pn = Math.max(pageNum, 1), ps = Math.max(pageSize, 9);
        int s = (pn - 1) * ps;
        List<GroupBuyActivityVO> paged = s < total ? vos.subList(s, Math.min(s + ps, total)) : new ArrayList<>();
        PageInfo<GroupBuyActivityVO> pi = new PageInfo<>();
        pi.setPageNum(pn); pi.setPageSize(ps); pi.setTotal(total); pi.setList(paged);
        return pi;
    }

    @Override
    public GroupBuyActivityVO getActivityDetail(String productId, String userId) {
        // 先将超时未成团的团队及其成员标记为失败
        int expired = groupBuyTeamDao.updateExpiredTeamsToFailed();
        if (expired > 0) {
            groupBuyTeamMemberDao.updateStatusForExpiredTeams();
        }
        GroupBuyProduct gbp = groupBuyProductDao.selectLatestByProductId(productId);
        if (gbp == null || gbp.getStatus() != 1) throw new RuntimeException("该商品未上架拼团活动：" + productId);
        GroupBuyActivityVO vo = convertGbpToVO(gbp);
        if (vo != null) {
            // 可加入的团队（排除当前用户已参与的）
            List<GroupBuyTeam> joinableTeams = groupBuyTeamDao.selectJoinableTeams(gbp.getActivityId(), gbp.getProductId(), userId);
            vo.setOpenTeamCount(joinableTeams == null ? 0 : joinableTeams.size());
            
            // 所有进行中的团队（用于详情页展示进度）
            List<GroupBuyTeam> allOpenTeams = groupBuyTeamDao.selectOpenTeamsByProductId(productId);
            if (allOpenTeams != null && !allOpenTeams.isEmpty()) {
                List<GroupBuyActivityVO.OpenTeamInfo> openTeamInfos = allOpenTeams.stream().map(t -> {
                    GroupBuyActivityVO.OpenTeamInfo info = new GroupBuyActivityVO.OpenTeamInfo();
                    info.setTeamId(t.getTeamId());
                    info.setCurrentPeople(t.getCurrentPeople());
                    info.setRequiredPeople(t.getRequiredPeople());
                    info.setEndTimeMs(t.getEndTime() != null ? t.getEndTime().getTime() : null);
                    info.setLeaderUserId(t.getLeaderUserId());
                    // 获取团长用户名
                    UserInfo leaderUser = userDao.selectByUserId(t.getLeaderUserId());
                    info.setLeaderUsername(leaderUser != null ? leaderUser.getUsername() : t.getLeaderUserId());
                    // 检查用户是否已参团
                    boolean isJoined = false;
                    if (userId != null && !userId.trim().isEmpty()) {
                        isJoined = groupBuyTeamMemberDao.selectByTeamIdAndUserId(t.getTeamId(), userId) != null;
                    }
                    info.setIsJoined(isJoined);
                    return info;
                }).collect(Collectors.toList());
                vo.setOpenTeams(openTeamInfos);
            }
            
            // 可加入的团队列表（用于列表页展示）
            if (joinableTeams != null && !joinableTeams.isEmpty()) {
                List<GroupBuyActivityVO.OpenTeamInfo> joinableTeamInfos = joinableTeams.stream().map(t -> {
                    GroupBuyActivityVO.OpenTeamInfo info = new GroupBuyActivityVO.OpenTeamInfo();
                    info.setTeamId(t.getTeamId());
                    info.setCurrentPeople(t.getCurrentPeople());
                    info.setRequiredPeople(t.getRequiredPeople());
                    info.setEndTimeMs(t.getEndTime() != null ? t.getEndTime().getTime() : null);
                    info.setLeaderUserId(t.getLeaderUserId());
                    // 获取团长用户名
                    UserInfo leaderUser = userDao.selectByUserId(t.getLeaderUserId());
                    info.setLeaderUsername(leaderUser != null ? leaderUser.getUsername() : t.getLeaderUserId());
                    info.setIsJoined(false); // joinableTeams 中的团队用户都未参团
                    return info;
                }).collect(Collectors.toList());
                vo.setJoinableTeams(joinableTeamInfos);
            }
        }
        return vo;
    }

    @Override
    public GroupBuyTeamVO getTeamDetail(String teamId) {
        GroupBuyTeam team = groupBuyTeamDao.selectByTeamId(teamId);
        if (team == null) throw new RuntimeException("拼团团队不存在：" + teamId);
        return convertTeamToVO(team);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String joinOrCreateTeam(GroupBuyJoinRequestDTO request) {
        GroupBuyActivity activity = groupBuyActivityDao.selectByActivityId(request.getActivityId());
        if (activity == null || activity.getStatus() != 1)
            throw new RuntimeException("拼团活动不存在或已结束");
        Date now = new Date();
        if (now.before(activity.getStartTime()) || now.after(activity.getEndTime()))
            throw new RuntimeException("拼团活动不在有效期内");
        GroupBuyProduct gbp = groupBuyProductDao.selectByActivityAndProduct(
                request.getActivityId(), request.getProductId());
        if (gbp == null || gbp.getStatus() != 1)
            throw new RuntimeException("该商品未上架拼团活动");
        ProductInfo product = productDao.selectByProductId(request.getProductId());
        if (product == null || product.getStatus() == null || product.getStatus() != 1)
            throw new RuntimeException("商品不存在或已下架");
        if (product.getStock() == null || product.getStock() < 1)
            throw new RuntimeException("商品【" + product.getName() + "】库存不足");
        GroupBuyTeam team;
        boolean isLeader = false;
        if (request.getTeamId() != null && !request.getTeamId().trim().isEmpty()) {
            team = groupBuyTeamDao.selectByTeamId(request.getTeamId());
            if (team == null || team.getStatus() != 0) throw new RuntimeException("团队不存在或已结束");
            if (team.getEndTime().before(now)) throw new RuntimeException("团队已超时");
            if (team.getCurrentPeople() >= team.getRequiredPeople()) throw new RuntimeException("团队已满员");
            if (groupBuyTeamMemberDao.selectByTeamIdAndUserId(team.getTeamId(), request.getUserId()) != null)
                throw new RuntimeException("您已参加过该拼团");
        } else {
            isLeader = true;
            Calendar cal = Calendar.getInstance();
            cal.setTime(now);
            cal.add(Calendar.HOUR_OF_DAY, activity.getValidDuration());
            team = new GroupBuyTeam();
            team.setTeamId(UUID.randomUUID().toString());
            team.setActivityId(activity.getActivityId());
            team.setProductId(request.getProductId());
            team.setLeaderUserId(request.getUserId());
            team.setRequiredPeople(activity.getRequiredPeople());
            team.setCurrentPeople(0); team.setStatus(0);
            team.setGroupBuyPrice(gbp.getGroupBuyPrice());
            team.setStartTime(now); team.setEndTime(cal.getTime());
            groupBuyTeamDao.insert(team);
        }
        String orderId = UUID.randomUUID().toString();
        BigDecimal payPrice = team.getGroupBuyPrice();
        OrderItem oi = new OrderItem();
        oi.setOrderId(orderId); oi.setProductId(product.getProductId());
        oi.setProductName(product.getName()); oi.setProductImage(product.getImageUrl());
        oi.setPrice(payPrice); oi.setQuantity(1); oi.setProductType("GROUP_BUY");
        OrderInfo order = new OrderInfo();
        order.setOrderId(orderId); order.setUserId(request.getUserId());
        order.setTotalPrice(payPrice); order.setStatus("1");
        order.setAddress(request.getAddress()); order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setCreateTime(now); order.setUpdateTime(now);
        orderDao.insertOrder(order); orderDao.insertOrderItem(oi);
        if (productDao.updateStockReduce(product.getProductId(), 1) == 0)
            throw new RuntimeException("库存扣减失败");
        GroupBuyTeamMember member = new GroupBuyTeamMember();
        member.setMemberId(UUID.randomUUID().toString());
        member.setTeamId(team.getTeamId()); member.setActivityId(activity.getActivityId());
        member.setProductId(request.getProductId()); member.setUserId(request.getUserId());
        member.setOrderId(orderId); member.setPayPrice(payPrice);
        member.setIsLeader(isLeader ? 1 : 0);
        member.setAddress(request.getAddress()); member.setContactName(request.getContactName());
        member.setContactPhone(request.getContactPhone()); member.setStatus(0);
        groupBuyTeamMemberDao.insert(member);
        groupBuyTeamDao.incrementCurrentPeople(team.getTeamId());
        GroupBuyTeam updated = groupBuyTeamDao.selectByTeamId(team.getTeamId());
        if (updated.getCurrentPeople() >= updated.getRequiredPeople()) {
            groupBuyTeamDao.updateStatus(team.getTeamId(), 1);
            groupBuyTeamMemberDao.updateStatusByTeamId(team.getTeamId(), 1);
        }
        increaseLotteryCount(request.getUserId(), 1);
        return orderId;
    }

    @Override
    public List<GroupBuyProgressVO> getUserGroupBuyProgress(String userId) {
        // 先将超时未成团的团队及其成员标记为失败
        int expired = groupBuyTeamDao.updateExpiredTeamsToFailed();
        if (expired > 0) {
            groupBuyTeamMemberDao.updateStatusForExpiredTeams();
        }
        List<GroupBuyTeamMember> members = groupBuyTeamMemberDao.selectByUserId(userId);
        if (members == null || members.isEmpty()) return new ArrayList<>();
        List<GroupBuyProgressVO> result = new ArrayList<>();
        for (GroupBuyTeamMember m : members) {
            GroupBuyTeam team = groupBuyTeamDao.selectByTeamId(m.getTeamId());
            if (team == null) continue;
            ProductInfo p = productDao.selectByProductId(m.getProductId());
            GroupBuyProgressVO vo = new GroupBuyProgressVO();
            vo.setTeamId(team.getTeamId()); vo.setActivityId(team.getActivityId());
            vo.setProductId(m.getProductId());
            vo.setProductName(p != null ? p.getName() : "");
            vo.setImageUrl(p != null ? p.getImageUrl() : "");
            vo.setGroupBuyPrice(team.getGroupBuyPrice());
            vo.setRequiredPeople(team.getRequiredPeople()); vo.setCurrentPeople(team.getCurrentPeople());
            vo.setRemainingPeople(Math.max(0, team.getRequiredPeople() - team.getCurrentPeople()));
            vo.setStatus(team.getStatus()); vo.setEndTime(team.getEndTime());
            vo.setCountdownDesc(buildCountdownDesc(team.getEndTime()));
            vo.setIsLeader(m.getIsLeader()); vo.setOrderId(m.getOrderId());
            // 填充团队成员列表
            try {
                List<GroupBuyTeamMember> teamMembers = groupBuyTeamMemberDao.selectByTeamId(team.getTeamId());
                List<GroupBuyProgressVO.MemberVO> memberVOs = new ArrayList<>();
                for (GroupBuyTeamMember tm : teamMembers) {
                    GroupBuyProgressVO.MemberVO mvo = new GroupBuyProgressVO.MemberVO();
                    mvo.setUserId(tm.getUserId());
                    mvo.setIsLeader(tm.getIsLeader());
                    mvo.setPayPrice(tm.getPayPrice());
                    mvo.setJoinTime(tm.getJoinTime());
                    try {
                        com.zky.domain.po.UserInfo u = userDao.selectByUserId(tm.getUserId());
                        if (u != null) {
                            mvo.setUsername(u.getUsername());
                            mvo.setGender(u.getGender());
                        }
                    } catch (Exception ignored) {}
                    memberVOs.add(mvo);
                }
                vo.setMembers(memberVOs);
            } catch (Exception ignored) {}
            result.add(vo);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String saveActivity(GroupBuyActivityRequestDTO request) {
        if (request.getActivityId() != null && !request.getActivityId().trim().isEmpty()) {
            GroupBuyActivity ex = groupBuyActivityDao.selectByActivityId(request.getActivityId());
            if (ex == null) throw new RuntimeException("活动不存在");
            ex.setRequiredPeople(request.getRequiredPeople());
            ex.setValidDuration(request.getValidDuration());
            ex.setStartTime(request.getStartTime()); ex.setEndTime(request.getEndTime());
            groupBuyActivityDao.update(ex);
            return ex.getActivityId();
        }
        String aid = UUID.randomUUID().toString();
        GroupBuyActivity a = new GroupBuyActivity();
        a.setActivityId(aid);
        a.setRequiredPeople(request.getRequiredPeople() != null ? request.getRequiredPeople() : 2);
        a.setValidDuration(request.getValidDuration() != null ? request.getValidDuration() : 24);
        a.setStatus(1);
        a.setStartTime(request.getStartTime() != null ? request.getStartTime() : new Date());
        if (request.getEndTime() != null) { a.setEndTime(request.getEndTime()); }
        else { Calendar c = Calendar.getInstance(); c.add(Calendar.YEAR, 1); a.setEndTime(c.getTime()); }
        groupBuyActivityDao.insert(a);
        return aid;
    }

    private GroupBuyActivityVO convertGbpToVO(GroupBuyProduct gbp) {
        if (gbp == null) return null;
        ProductInfo p = productDao.selectByProductId(gbp.getProductId());
        if (p == null) return null;
        GroupBuyActivity act = groupBuyActivityDao.selectByActivityId(gbp.getActivityId());
        GroupBuyActivityVO vo = new GroupBuyActivityVO();
        vo.setActivityId(gbp.getActivityId()); vo.setProductId(gbp.getProductId());
        vo.setProductName(p.getName()); vo.setImageUrl(p.getImageUrl());
        vo.setOriginalPrice(gbp.getOriginalPrice()); vo.setGroupBuyPrice(gbp.getGroupBuyPrice());
        if (act != null) {
            vo.setRequiredPeople(act.getRequiredPeople()); vo.setValidDuration(act.getValidDuration());
            vo.setStatus(act.getStatus()); vo.setStartTime(act.getStartTime()); vo.setEndTime(act.getEndTime());
        }
        // 计算可加入的团队数量（不传 userId，返回所有可加入的团队）
        List<GroupBuyTeam> joinableTeams = groupBuyTeamDao.selectJoinableTeams(gbp.getActivityId(), gbp.getProductId(), "");
        vo.setOpenTeamCount(joinableTeams == null ? 0 : joinableTeams.size());
        return vo;
    }

    private GroupBuyTeamVO convertTeamToVO(GroupBuyTeam team) {
        GroupBuyTeamVO vo = new GroupBuyTeamVO();
        vo.setTeamId(team.getTeamId()); vo.setActivityId(team.getActivityId());
        vo.setProductId(team.getProductId()); vo.setLeaderUserId(team.getLeaderUserId());
        vo.setRequiredPeople(team.getRequiredPeople()); vo.setCurrentPeople(team.getCurrentPeople());
        vo.setRemainingPeople(Math.max(0, team.getRequiredPeople() - team.getCurrentPeople()));
        vo.setStatus(team.getStatus()); vo.setGroupBuyPrice(team.getGroupBuyPrice());
        vo.setStartTime(team.getStartTime()); vo.setEndTime(team.getEndTime());
        vo.setCountdownDesc(buildCountdownDesc(team.getEndTime()));
        ProductInfo p = productDao.selectByProductId(team.getProductId());
        if (p != null) { vo.setProductName(p.getName()); vo.setImageUrl(p.getImageUrl()); vo.setOriginalPrice(p.getPrice()); }
        List<GroupBuyMemberVO> mvos = groupBuyTeamMemberDao.selectByTeamId(team.getTeamId()).stream()
                .map(m -> { GroupBuyMemberVO mv = new GroupBuyMemberVO();
                    mv.setUserId(m.getUserId()); mv.setIsLeader(m.getIsLeader());
                    mv.setPayPrice(m.getPayPrice()); mv.setStatus(m.getStatus());
                    mv.setJoinTime(m.getJoinTime()); return mv; })
                .collect(Collectors.toList());
        vo.setMembers(mvos);
        return vo;
    }

    private String buildCountdownDesc(Date endTime) {
        if (endTime == null) return "已结束";
        long diff = endTime.getTime() - System.currentTimeMillis();
        if (diff <= 0) return "已结束";
        return diff / 3600000 + "小时" + (diff % 3600000) / 60000 + "分";
    }

    private void increaseLotteryCount(String userId, int delta) {
        UserLottery r = userLotteryDao.selectByUserId(userId);
        if (r == null) {
            UserLottery ins = new UserLottery(); ins.setUserId(userId);
            ins.setLotteryCount(Math.max(delta, 0)); userLotteryDao.insert(ins); return;
        }
        r.setLotteryCount((r.getLotteryCount() == null ? 0 : r.getLotteryCount()) + delta);
        userLotteryDao.update(r);
    }
}
