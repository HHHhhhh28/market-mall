package com.zky.domain.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zky
 * @description：
 * @date ：2026/2/15 12:05
 */
@Data
public class GroupBuyProductInfoRequestDTO {
    // 用户ID
    private String userId;
    // 商品ID
    private String productId;

}
