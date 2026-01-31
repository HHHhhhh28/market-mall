package com.zky.domain.vo;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVO {
    private String productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private String description;
    private String category;
    private String activityId;
    private String type; // "NORMAL", "GROUP_BUY", "AWARD"
}
