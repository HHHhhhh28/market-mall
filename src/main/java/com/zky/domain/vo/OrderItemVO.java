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
}
