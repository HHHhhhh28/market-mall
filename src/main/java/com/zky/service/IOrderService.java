package com.zky.service;

import com.zky.domain.dto.OrderInfoRequestDTO;
import com.zky.domain.vo.OrderVO;

import java.util.List;

public interface IOrderService {
    String createOrder(OrderInfoRequestDTO request);
    List<OrderVO> getOrderList(String userId);
    OrderVO getOrderDetail(String orderId);
}
