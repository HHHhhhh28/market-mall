package com.zky.dao;

import com.zky.domain.po.OrderInfo;
import com.zky.domain.po.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface OrderDao {
    int insertOrder(OrderInfo orderInfo);
    int insertOrderItem(OrderItem orderItem);
    OrderInfo selectOrderById(@Param("orderId") String orderId);
    List<OrderItem> selectOrderItemsByOrderId(@Param("orderId") String orderId);
    List<OrderInfo> selectOrdersByUserId(@Param("userId") String userId);
}
