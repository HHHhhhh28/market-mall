package com.zky.domain.dto;

import lombok.Data;
import java.util.List;

@Data
public class UserBehaviorRequestDTO {
    /**
     * 用户ID
     */
    private String userId;

    /**
     * 行为类型 (CLICK:点击, COLLECT:收藏, BUY:购买)
     */
    private String behaviorType;

    /**
     * 商品ID (用于单商品操作，如点击、收藏)
     */
    private String productId;

    /**
     * 商品ID列表 (用于多商品操作，如批量购买)
     */
    private List<String> productIds;
}
