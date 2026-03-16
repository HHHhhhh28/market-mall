package com.zky.domain.dto;

import lombok.Data;

/**
 * 参团/开团请求DTO
 * 用于：用户发起开团 或 加入已有团队
 */
@Data
public class GroupBuyJoinRequestDTO {

    /** 用户ID */
    private String userId;

    /** 活动ID */
    private String activityId;

    /** 商品ID */
    private String productId;

    /**
     * 团队ID（可选）
     * 为空时：发起新团（开团）
     * 不为空时：加入已有团队
     */
    private String teamId;

    /** 收货地址 */
    private String address;

    /** 联系人姓名 */
    private String contactName;

    /** 联系电话 */
    private String contactPhone;
}
