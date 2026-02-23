package com.zky.service.impl;

import com.zky.api.IMarketIndexService;
import com.zky.api.IMarketTradeService;
import com.zky.api.dto.*;
import com.zky.api.response.Response;
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
import org.apache.dubbo.config.annotation.DubboReference;
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
    @DubboReference(interfaceClass = IMarketTradeService.class,version = "1.0")
    private IMarketTradeService iMarketTradeService;

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

    @Override
    public String createGroupBuyOrder(OrderInfoRequestDTO request) {
        if (request.getProductId() == null) {
            throw new RuntimeException("拼团订单必须指定一个商品");
        }


        String orderId = UUID.randomUUID().toString();
        BigDecimal totalAmount = BigDecimal.ZERO;

        String productId = request.getProductId();
        Integer buyNum = 1; // 购买数量

        ProductInfo product = productDao.selectByProductId(productId);
        if (product == null) {
            throw new RuntimeException("商品【" + productId + "】已下架，无法下单");
        }

        // 库存充足性校验（最新库存 ≥ 购买数量）
        Integer stock = product.getStock(); // 假设ProductInfo有stock字段，存储商品库存
        if (stock == null || stock < buyNum) {
            throw new RuntimeException("商品【" + product.getName() + "】库存不足，当前库存：" + (stock == null ? 0 : stock) + "，购买数量：" + buyNum);
        }
        LockMarketPayOrderRequestDTO.NotifyConfigVO notifyConfigVO = new LockMarketPayOrderRequestDTO.NotifyConfigVO();
        notifyConfigVO.setNotifyType("MQ");
        ProductInfoDTO productInfoDTO = new ProductInfoDTO();
        productInfoDTO.setProductId(request.getProductId());
        productInfoDTO.setName(product.getName());
        productInfoDTO.setPrice(product.getPrice())
        ;
        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId(request.getUserId());
        lockMarketPayOrderRequestDTO.setTeamId(request.getTeamId());
        lockMarketPayOrderRequestDTO.setActivityId(request.getActivityId());
        lockMarketPayOrderRequestDTO.setGoodsId(request.getProductId());
        lockMarketPayOrderRequestDTO.setSource("s01");
        lockMarketPayOrderRequestDTO.setChannel("c01");
        lockMarketPayOrderRequestDTO.setNotifyConfigVO(notifyConfigVO);
        lockMarketPayOrderRequestDTO.setProductInfoDTO(productInfoDTO);
        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = iMarketTradeService.lockMarketPayOrder(lockMarketPayOrderRequestDTO);
        LockMarketPayOrderResponseDTO lockMarketPayOrderResponseDTO = lockMarketPayOrderResponseDTOResponse.getData();
        if (lockMarketPayOrderResponseDTO == null) {
            throw new RuntimeException("锁单失败");
        }

        SettlementMarketPayOrderRequestDTO settlementMarketPayOrderRequestDTO = new SettlementMarketPayOrderRequestDTO();
        settlementMarketPayOrderRequestDTO.setSource("s01");
        settlementMarketPayOrderRequestDTO.setChannel("c01");
        settlementMarketPayOrderRequestDTO.setUserId(lockMarketPayOrderResponseDTO.getUserId());
        settlementMarketPayOrderRequestDTO.setOutTradeNo(lockMarketPayOrderResponseDTO.getOutTradeNo());
        settlementMarketPayOrderRequestDTO.setOutTradeTime(new Date());

        Response<SettlementMarketPayOrderResponseDTO> settlementMarketPayOrderResponseDTOResponse = iMarketTradeService.settlementMarketPayOrder(settlementMarketPayOrderRequestDTO);
        SettlementMarketPayOrderResponseDTO settlementMarketPayOrderResponseDTO = settlementMarketPayOrderResponseDTOResponse.getData();
        if(settlementMarketPayOrderResponseDTO == null) {
            throw new RuntimeException("结算失败");
        }


        //封装订单项
        OrderItem item = new OrderItem();
        item.setOrderId(orderId);
        item.setProductId(productId);
        item.setProductName(product.getName());
        item.setProductImage(product.getImageUrl());
        item.setPrice(lockMarketPayOrderResponseDTO.getPayPrice());
        item.setQuantity(buyNum);
        item.setProductType("GROUP_BUY");

        // 3.5 计算订单项小计并累加订单总金额
        BigDecimal itemTotal = lockMarketPayOrderResponseDTO.getPayPrice().multiply(new BigDecimal(buyNum));
        totalAmount = totalAmount.add(itemTotal);


        // 4. 封装主订单信息
        OrderInfo order = new OrderInfo();
        order.setOrderId(orderId);
        order.setUserId(request.getUserId());
        order.setTotalPrice(totalAmount);
        order.setStatus("1"); // 已支付
        order.setAddress(request.getAddress());
        order.setContactName(request.getContactName());
        order.setContactPhone(request.getContactPhone());
        order.setCreateTime(new Date());
        order.setUpdateTime(new Date());

        // 5. 插入订单和订单项到数据库（先落库，再扣库存）
        orderDao.insertOrder(order);
        orderDao.insertOrderItem(item);

        // 订单创建成功后，批量扣减商品库存（关键步骤）
        // 调用productDao的库存扣减方法，扣减对应数量
        int updateRow = productDao.updateStockReduce(productId, buyNum);
        // 校验扣减结果（防止扣减时库存被其他订单抢光，行锁保护）
        if (updateRow == 0) {
            throw new RuntimeException("商品【" + item.getProductName() + "】库存扣减失败，可能已被抢购一空");
        }

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
