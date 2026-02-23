package com.zky.domain.vo;

import com.zky.common.enums.RecommendationType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zky
 * @description：
 * @date ：2026/2/14 09:58
 */
@Data
public class HomeProductVO {
    private String productId;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal payPrice;
    // 活动ID
    private Long activityId;
    private String description;
    private String category;
    private String type; // "NORMAL", "GROUP_BUY", "AWARD"

}