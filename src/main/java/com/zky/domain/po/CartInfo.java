package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartInfo {
    private Long id;
    private String userId;
    private String productId;
    private Integer quantity;
    private Date createTime;
    private Date updateTime;
}
