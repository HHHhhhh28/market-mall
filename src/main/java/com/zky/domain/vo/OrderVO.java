package com.zky.domain.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
public class OrderVO {
    private String orderId;
    private String status;
    private BigDecimal totalPrice;
    private String address;
    private String contactName;
    private String contactPhone;
    private Date createTime;
    private List<OrderItemVO> items;
    private String couponId;
    private String couponName;
    private java.math.BigDecimal originalPrice;
    private java.math.BigDecimal discountAmount;
}
