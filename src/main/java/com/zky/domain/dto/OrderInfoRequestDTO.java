package com.zky.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class OrderInfoRequestDTO {
    private String userId;
    private List<String> productIds; 
    private String address;
    private String contactName;
    private String contactPhone;
    private String productId; // 拼团需要的单个商品ID
    private String teamId;
    private String activityId;
    private String productType; //'商品类型：NORMAL-普通商品、GROUP_BUY-拼团商品、AWARD-抽奖商品'
    private String couponId;
}
