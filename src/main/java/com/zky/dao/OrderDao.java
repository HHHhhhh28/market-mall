package com.zky.dao;

import com.zky.domain.po.OrderInfo;
import com.zky.domain.po.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderDao {
    int insertOrder(OrderInfo orderInfo);
    int insertOrderItem(OrderItem orderItem);
    OrderInfo selectOrderById(@Param("orderId") String orderId);
    List<OrderItem> selectOrderItemsByOrderId(@Param("orderId") String orderId);
    List<OrderInfo> selectOrdersByUserId(@Param("userId") String userId);
    List<String> selectProductIdsByUserId(@Param("userId") String userId);
    Double selectTodayTotalAmount();
    Integer selectTodayOrderCount();
    List<Map<String, Object>> selectOrderTrend();

    List<OrderItem> selectOrderItemsByUserId(@Param("userId") String userId);

    /** 查询近N天指定品类的订单数和活跃买家数 */
    List<java.util.Map<String, Object>> selectCategoryStats(
            @Param("category") String category,
            @Param("days") int days);

    /** 查询近N天指定品类有多次购买的用户数（用于计算复购率） */
    int selectRepeatBuyerCount(
            @Param("category") String category,
            @Param("days") int days);

    /** 统计某商品在所有订单中的总销售数量 */
    Integer countSalesByProductId(@Param("productId") String productId);
}
