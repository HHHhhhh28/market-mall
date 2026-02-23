package com.zky.domain.vo;

import com.zky.common.enums.RecommendationType;
import lombok.Data;

import java.math.BigDecimal;

/**
 * @author ：zky
 * @description：
 * @date ：2026/2/14 09:57
 */
@Data
public class LotteryProductVO {
    private String productId;
    private String name;
    private BigDecimal price;
    private String imageUrl;
    private String category;
}
