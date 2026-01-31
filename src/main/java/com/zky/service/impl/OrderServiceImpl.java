package com.zky.service.impl;

import com.zky.dao.CartDao;
import com.zky.dao.OrderDao;
import com.zky.dao.ProductDao;
import com.zky.domain.dto.OrderInfoRequestDTO;
import com.zky.domain.po.CartInfo;
import com.zky.domain.po.OrderInfo;
import com.zky.domain.po.OrderItem;
import com.zky.domain.po.ProductInfo;
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String createOrder(OrderInfoRequestDTO request) {
        // 逻辑：从购物车中选中的商品创建订单
        // 1. 获取购物车商品
        List<CartInfo> carts = cartDao.selectByUserId(request.getUserId()).stream()
                .filter(c -> request.getProductIds().contains(c.getProductId()))
                .collect(Collectors.toList());

        if (carts.isEmpty()) {
            throw new RuntimeException("没有选择有效的商品进行下单");
        }

        // 2. 创建订单
        String orderId = UUID.randomUUID().toString();
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (CartInfo cart : carts) {
            ProductInfo product = productDao.selectByProductId(cart.getProductId());
            if (product == null) continue;

            OrderItem item = new OrderItem();
            item.setOrderId(orderId);
            item.setProductId(product.getProductId());
            item.setProductName(product.getName());
            item.setProductImage(product.getImageUrl());
            item.setPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            
            BigDecimal itemTotal = product.getPrice().multiply(new BigDecimal(cart.getQuantity()));
            // item.setTotalPrice(itemTotal); // PO对象中没有totalPrice字段
            
            orderItems.add(item);
            totalAmount = totalAmount.add(itemTotal);
            
            // 从购物车中移除
            cartDao.delete(request.getUserId(), cart.getProductId());
        }

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

        orderDao.insertOrder(order);
        for (OrderItem item : orderItems) {
            orderDao.insertOrderItem(item);
        }

        return orderId;
    }

    @Override
    public List<OrderVO> getOrderList(String userId) {
        List<OrderInfo> orders = orderDao.selectOrdersByUserId(userId);
        return orders.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    public OrderVO getOrderDetail(String orderId) {
        OrderInfo order = orderDao.selectOrderById(orderId);
        if (order == null) {
            throw new RuntimeException("Order not found");
        }
        return convertToVO(order);
    }

    private OrderVO convertToVO(OrderInfo order) {
        OrderVO vo = new OrderVO();
        vo.setOrderId(order.getOrderId());
        vo.setStatus(order.getStatus());
        vo.setTotalPrice(order.getTotalPrice());
        vo.setCreateTime(order.getCreateTime());
        vo.setAddress(order.getAddress());
        vo.setContactName(order.getContactName());
        vo.setContactPhone(order.getContactPhone());
        
        List<OrderItem> items = orderDao.selectOrderItemsByOrderId(order.getOrderId());
        List<OrderItemVO> itemVOs = items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            itemVO.setProductId(item.getProductId());
            itemVO.setProductName(item.getProductName());
            itemVO.setProductImage(item.getProductImage());
            itemVO.setPrice(item.getPrice());
            itemVO.setQuantity(item.getQuantity());
            return itemVO;
        }).collect(Collectors.toList());
        
        vo.setItems(itemVOs);
        return vo;
    }
}
