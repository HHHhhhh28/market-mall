package com.zky.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrderItemVO {
    private String productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private String productType; //'商品类型：NORMAL-普通商品、GROUP_BUY-拼团商品、AWARD-抽奖商品'
}
