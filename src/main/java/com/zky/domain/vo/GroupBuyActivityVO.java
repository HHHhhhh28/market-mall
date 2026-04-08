package com.zky.domain.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 拼团活动展示VO（用于拼团列表/商品卡片）
 */
@Data
public class GroupBuyActivityVO {

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

    /** 拼团所需人数 */
    private Integer requiredPeople;

    /** 拼团有效时长（小时） */
    private Integer validDuration;

    /** 活动状态：1-进行中，0-已结束，2-未开始 */
    private Integer status;

    /** 活动开始时间 */
    private Date startTime;

    /** 活动结束时间 */
    private Date endTime;

    /** 当前进行中的团队数量（可凑团） */
    private Integer openTeamCount;

    /** 进行中的团队列表（含进度和倒计时，用于详情页展示） */
    private List<OpenTeamInfo> openTeams;

    /** 可加入的团队列表（排除用户已参团的，用于列表页展示） */
    private List<OpenTeamInfo> joinableTeams;

    /** 进行中团队简要信息内部类 */
    @Data
    public static class OpenTeamInfo {
        private String teamId;
        private Integer currentPeople;
        private Integer requiredPeople;
        private Long endTimeMs; // 毫秒时间戳，前端计算倒计时
        private String leaderUserId;
        private String leaderUsername; // 团长用户名
        private Boolean isJoined; // 当前用户是否已参团
    }
}
