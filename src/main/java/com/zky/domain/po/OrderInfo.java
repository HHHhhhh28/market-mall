package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderInfo {
    private Long id;
    private String orderId;
    private String userId;
    private BigDecimal totalPrice;
    private String status; // PENDING, PAID, SHIPPED, COMPLETED, CANCELLED
    private String address;
    private String contactName;
    private String contactPhone;
    private String couponId;
    private Date createTime;
    private Date updateTime;
}
