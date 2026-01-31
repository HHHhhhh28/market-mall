package com.zky.domain.dto;

import lombok.Data;

@Data
public class ProductInfoRequestDTO {
    private int pageNum;
    private int pageSize;
    private String keyword;
    private String category;
}
