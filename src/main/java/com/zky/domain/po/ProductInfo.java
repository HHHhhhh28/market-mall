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
public class ProductInfo {
    private Long id;
    private String productId;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private Integer stock;
    private String category;
    private String brand;
    private String keywords;
    private Date createTime;
    private Date updateTime;
    private String userTags;
}
