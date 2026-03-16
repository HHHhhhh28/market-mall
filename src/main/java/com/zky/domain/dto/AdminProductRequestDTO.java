package com.zky.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdminProductRequestDTO {
    private String productId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private String category;
    private String brand;
    private String keywords;
    private String userTags;
    private Integer status;
}

