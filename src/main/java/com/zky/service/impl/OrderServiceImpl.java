package com.zky.service.impl;

import com.zky.dao.GroupBuyActivityDao;
import com.zky.dao.GroupBuyTeamDao;
import com.zky.dao.GroupBuyTeamMemberDao;
import com.zky.domain.po.GroupBuyActivity;
import com.zky.domain.po.GroupBuyTeam;
import com.zky.domain.po.GroupBuyTeamMember;
import com.zky.dao.CartDao;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.dao.CouponDao;
import com.zky.dao.UserCouponDao;
import com.zky.dao.UserLotteryDao;
import com.zky.domain.dto.OrderInfoRequestDTO;
import com.zky.domain.po.CartInfo;
import com.zky.domain.po.OrderInfo;
import com.zky.domain.po.OrderItem;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.Coupon;
import com.zky.domain.po.UserCoupon;
import com.zky.domain.po.UserLottery;
import com.zky.service.IOrderService;
import com.zky.domain.vo.OrderItemVO;
import com.zky.domain.vo.OrderVO;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements IOrderService {

    @Resource
    private OrderDao orderDao;
    @Resource
    private CartDao cartDao;
    @Resource
    private ProductDao productDao;
    @Resource
    private CouponDao couponDao;
    @Resource
    private UserCouponDao userCouponDao;
    @Resource
    private UserLotteryDao userLotteryDao;
    @Resource
    private GroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private GroupBuyTeamDao groupBuyTeamDao;
    @Resource
    private GroupBuyTeamMemberDao groupBuyTeamMemberDao;

    @Override
    @Transactional(rollbackFor = Exception.class) // 关键：事务注解，异常时回滚所有操作
    public String createOrder(OrderInfoRequestDTO request) {
        // 1. 获取购物车中选中的商品
        List<CartInfo> carts = cartDao.selectByUserId(request.getUserId()).stream()
                .filter(c -> request.getProductIds().contains(c.getProductId()))
                .collect(Collectors.toList());

        if (carts.isEmpty()) {
            throw new RuntimeException("没有选择有效的商品进行下单");
        }

        // 2. 初始化订单基础信息
        String orderId = UUID.randomUUID().toString();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();
        List<String> categories = new ArrayList<>();

        // 3. 遍历购物车，校验商品+封装订单项+预校验库存
        for (CartInfo cart : carts) {
            String productId = cart.getProductId();
            Integer buyNum = cart.getQuantity(); // 购买数量

            // 3.1 查询商品详情（含最新库存），商品不存在则跳过并抛出异常
            ProductInfo product = productDao.selectByProductId(productId);
            if (product == null) {
                throw new RuntimeException("商品【" + productId + "】已下架，无法下单");
            }
            // 3.2 校验购买数量（不能为0/负数）
            if (buyNum == null || buyNum <= 0) {
                throw new RuntimeException("商品【" + product.getName() + "】购买数量无效");
            }
            // 3.3 核心：库存充足性校验（最新库存 ≥ 购买数量）
            Integer stock = product.getStock(); // 假设ProductInfo有stock字段，存储商品库存
            if (stock == null || stock < buyNum) {
                throw new RuntimeException("商品【" + product.getName() + "】库存不足，当前库存：" + (stock == null ? 0 : stock) + "，购买数量：" + buyNum);
            }

            // 3.4 封装订单项
            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(productId);
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setPrice(product.getPrice());
            item.setQuantity(buyNum);
            item.setProductType("NORMAL");

            // 3.5 计算订单项小计并累加订单总金额
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(buyNum));
            totalAmount = totalAmount.add(itemTotal);
            orderItems.add(item);
            if (product.getCategory() != null && !categories.contains(product.getCategory())) {
                categories.add(product.getCategory());
            }

            // 3.6 从购物车移除已下单商品
            cartDao.delete(request.getUserId(), productId);
        }

        String couponId = request.getCouponId();
        if (couponId != null && !couponId.isEmpty()) {
            UserCoupon userCoupon = userCouponDao.selectAvailableOne(request.getUserId(), couponId);
            if (userCoupon == null) {
                throw new RuntimeException("优惠券不可用");
            }
            Coupon coupon = couponDao.selectByCouponId(couponId);
            if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != 1) {
                throw new RuntimeException("优惠券不存在或已失效");
            }
            if (!categories.contains(coupon.getCategory())) {
                throw new RuntimeException("优惠券品类与订单不匹配");
            }
            BigDecimal discountAmount = calculateDiscount(totalAmount, coupon);
            totalAmount = totalAmount.subtract(discountAmount);
            if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
                totalAmount = BigDecimal.ZERO;
            }
        }

        OrderInfo order = new OrderInfo();
        order.setOrderId(orderId);
        order.setUserId(request.getUserId());
        order.setTotalPrice(totalAmount);
        order.setStatus("1"); // 已支付
        order.setAddress(request.getAddress());
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setCouponId(couponId);
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        // 5. 插入订单和订单项到数据库（先落库，再扣库存）
        orderDao.insertOrder(order);
        for (OrderItem item : orderItems) {
            orderDao.insertOrderItem(item);
        }

        // 6. 核心：订单创建成功后，批量扣减商品库存（关键步骤）
        for (OrderItem item : orderItems) {
            String productId = item.getProductId();
            Integer buyNum = item.getQuantity();
            // 调用productDao的库存扣减方法，扣减对应数量
            int updateRow = productDao.updateStockReduce(productId, buyNum);
            // 校验扣减结果（防止扣减时库存被其他订单抢光，行锁保护）
            if (updateRow == 0) {
                throw new RuntimeException("商品【" + item.getProductName() + "】库存扣减失败，可能已被抢购一空");
            }
        }

        if (couponId != null && !couponId.isEmpty()) {
            userCouponDao.markUsed(request.getUserId(), couponId);
        }

        increaseLotteryCount(request.getUserId(), 1);

        return orderId;
    }

    @Override
    public List<OrderVO> getOrderList(String userId,List<String> productTypes) {
        List<OrderInfo> orders = orderDao.selectOrdersByUserId(userId);
        // 调用convertToVO时传入productTypes筛选条件
        return orders.stream()
                .map(order -> convertToVO(order, productTypes)) // 传参调整
                .filter(vo -> !vo.getItems().isEmpty()) // 过滤掉筛选后无订单项的订单
                .collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderDetail(String orderId) {
        OrderInfo order = orderDao.selectOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return convertToVO(order,null);
    }

    /**
     * 拼团下单（本地实现，替代原Dubbo调用）
     * 直接调用本地GroupBuyService的joinOrCreateTeam逻辑
     * 此方法保留兼容旧接口，内部委托给 GroupBuyServiceImpl
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createGroupBuyOrder(OrderInfoRequestDTO request) {
        if (request.getProductId() == null || request.getProductId().isEmpty()) {
            throw new RuntimeException("拼团订单必须指定一个商品");
        }
        if (request.getActivityId() == null || request.getActivityId().isEmpty()) {
            throw new RuntimeException("拼团订单必须指定活动ID");
        }
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
        // 3. 确定团队
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
            GroupBuyTeamMember existMember = groupBuyTeamMemberDao
                    .selectByTeamIdAndUserId(team.getTeamId(), request.getUserId());
            if (existMember != null) {
                throw new RuntimeException("您已参加过该拼团");
            }
        } else {
            // 开团，成为团长
            isLeader = true;
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTime(now);
            cal.add(java.util.Calendar.HOUR_OF_DAY, activity.getValidDuration());
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
        orderInfo.setStatus("0"); // 拼团中，等待成团后发货
        orderInfo.setAddress(request.getAddress());
        orderInfo.setContactName(request.getContactName());
        orderInfo.setContactPhone(request.getContactPhone());
        orderInfo.setCreateTime(now);
        orderInfo.setUpdateTime(now);
        orderDao.insertOrder(orderInfo);
        orderDao.insertOrderItem(orderItem);
        // 5. 扣减库存
        int updateRow = productDao.updateStockReduce(product.getProductId(), 1);
        if (updateRow == 0) {
            throw new RuntimeException("商品【" + product.getName() + "】库存扣减失败");
        }
        // 6. 记录成员
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
        // 7. 团队人数 +1，判断是否成团
        groupBuyTeamDao.incrementCurrentPeople(team.getTeamId());
        GroupBuyTeam updatedTeam = groupBuyTeamDao.selectByTeamId(team.getTeamId());
        if (updatedTeam.getCurrentPeople() >= updatedTeam.getRequiredPeople()) {
            groupBuyTeamDao.updateStatus(team.getTeamId(), 1);
            groupBuyTeamMemberDao.updateStatusByTeamId(team.getTeamId(), 1);
        }
        // 8. 参团奖励抽奖次数
        increaseLotteryCount(request.getUserId(), 1);
        return orderId;
    }

    private OrderVO convertToVO(OrderInfo order, List<String> productTypes) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setStatus(order.getStatus());
        vo.setTotalPrice(order.getTotalPrice());
        vo.setCreateTime(order.getCreateTime());
        vo.setAddress(order.getAddress());
        vo.setContactName(order.getContactName());
        vo.setContactPhone(order.getContactPhone());

        List<OrderItem> items = orderDao.selectOrderItemsByOrderId(order.getOrderId());
        List<OrderItemVO> itemVOs = items.stream()
                // 2. 根据productTypes筛选订单项（核心改动）
                .filter(item -> {
                    // 如果传null/空列表，代表“全部”，不筛选
                    if (productTypes == null || productTypes.isEmpty()) {
                        return true;
                    }
                    // 否则只保留匹配productType的订单项
                    return productTypes.contains(item.getProductType());
                })
                .map(item -> {
                    OrderItemVO itemVO = new OrderItemVO();
                    itemVO.setProductId(item.getProductId());
                    itemVO.setProductName(item.getProductName());
                    itemVO.setProductImage(item.getProductImage());
                    itemVO.setPrice(item.getPrice());
                    itemVO.setQuantity(item.getQuantity());
                    itemVO.setProductType(item.getProductType());
                    return itemVO;
                })
                .collect(Collectors.toList());

        vo.setItems(itemVOs);
        return vo;
    }

    private BigDecimal calculateDiscount(BigDecimal totalAmount, Coupon coupon) {
        if (coupon == null || coupon.getCouponType() == null || coupon.getValue() == null) {
            return BigDecimal.ZERO;
        }
        if ("DIRECT".equals(coupon.getCouponType())) {
            return coupon.getValue();
        }
        if ("DISCOUNT".equals(coupon.getCouponType())) {
            BigDecimal rate = coupon.getValue().divide(new BigDecimal("100"));
            BigDecimal discounted = totalAmount.multiply(BigDecimal.ONE.subtract(rate));
            return discounted.max(BigDecimal.ZERO);
        }
        return BigDecimal.ZERO;
    }

    private void increaseLotteryCount(String userId, int delta) {
        UserLottery record = userLotteryDao.selectByUserId(userId);
        if (record == null) {
            UserLottery insert = new UserLottery();
            insert.setUserId(userId);
            insert.setLotteryCount(Math.max(delta, 0));
            insert.setLastSignDate(null);
            userLotteryDao.insert(insert);
            return;
        }
        int current = record.getLotteryCount() == null ? 0 : record.getLotteryCount();
        record.setLotteryCount(current + delta);
        userLotteryDao.update(record);
    }
}
