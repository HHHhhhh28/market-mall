package com.zky.domain.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CartItemVO {
    private String productId;
    private String productName;
    private String imageUrl;
    private BigDecimal price;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String category;
}
