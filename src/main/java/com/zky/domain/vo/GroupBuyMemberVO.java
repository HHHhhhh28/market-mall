package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼团成员展示VO
 */
@Data
public class GroupBuyMemberVO {

    /** 用户ID */
    private String userId;

    /** 是否为团长：1-是，0-否 */
    private Integer isLeader;

    /** 实际支付价格 */
    private BigDecimal payPrice;

    /** 成员状态：0-拼团中，1-拼团成功，2-拼团失败 */
    private Integer status;

    /** 参团时间 */
    private Date joinTime;
}
