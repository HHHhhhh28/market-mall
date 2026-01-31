package com.zky.domain.dto;

import lombok.Data;

@Data
public class CartInfoRequestDTO {
    private String userId;
    private String productId;
    private Integer quantity;
}
