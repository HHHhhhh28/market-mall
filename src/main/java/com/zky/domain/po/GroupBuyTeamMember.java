package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼团团队成员实体
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyTeamMember {

    /** 主键ID */
    private Long id;

    /** 成员记录ID（业务唯一标识） */
    private String memberId;

    /** 关联团队ID */
    private String teamId;

    /** 关联活动ID */
    private String activityId;

    /** 关联商品ID */
    private String productId;

    /** 用户ID */
    private String userId;

    /** 关联订单ID */
    private String orderId;

    /** 实际支付价格（拼团价快照） */
    private BigDecimal payPrice;

    /** 是否为团长：1-是，0-否 */
    private Integer isLeader;

    /** 收货地址快照 */
    private String address;

    /** 联系人姓名快照 */
    private String contactName;

    /** 联系电话快照 */
    private String contactPhone;

    /** 成员状态：0-拼团中，1-拼团成功，2-拼团失败退款 */
    private Integer status;

    /** 参团时间 */
    private Date joinTime;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
