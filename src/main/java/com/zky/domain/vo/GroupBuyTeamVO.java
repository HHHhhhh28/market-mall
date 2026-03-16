package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 拼团团队详情VO（用于拼团详情页 / 进度页）
 */
@Data
public class GroupBuyTeamVO {

    /** 团队ID */
    private String teamId;

    /** 活动ID */
    private String activityId;

    /** 商品ID */
    private String productId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String imageUrl;

    /** 商品原价 */
    private BigDecimal originalPrice;

    /** 拼团价格 */
    private BigDecimal groupBuyPrice;

    /** 团长用户ID */
    private String leaderUserId;

    /** 拼团所需人数 */
    private Integer requiredPeople;

    /** 当前已参团人数 */
    private Integer currentPeople;

    /** 还差几人成团（requiredPeople - currentPeople） */
    private Integer remainingPeople;

    /**
     * 团队状态：0-拼团中，1-拼团成功，2-拼团失败
     */
    private Integer status;

    /** 开团时间 */
    private Date startTime;

    /** 拼团截止时间 */
    private Date endTime;

    /** 倒计时描述（如：23小时59分） */
    private String countdownDesc;

    /** 团队成员列表 */
    private List<GroupBuyMemberVO> members;
}
