package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 拼团商品策略实体（策略算法分析结果 + 上下架状态）
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupBuyProduct {

    /** 主键ID */
    private Long id;

    /** 关联活动ID */
    private String activityId;

    /** 关联商品ID */
    private String productId;

    /** 策略算法计算出的拼团优惠价 */
    private BigDecimal groupBuyPrice;

    /** 商品原价快照 */
    private BigDecimal originalPrice;

    /** 折扣率（0~1），算法计算结果 */
    private BigDecimal discountRate;

    /** 对该商品感兴趣的用户数（算法统计） */
    private Integer interestedUsers;

    /** 推送目标用户标签（逗号分隔，如：男,18-25,26-35） */
    private String targetUserTags;

    /** 商品行为热度分（浏览+收藏+加购综合分） */
    private BigDecimal behaviorScore;

    /** 近30天购买次数 */
    private Integer purchaseCount;

    /** 近30天浏览次数 */
    private Integer viewCount;

    /** 近30天收藏次数 */
    private Integer favoriteCount;

    /** 上架状态：0-未上架，1-已上架（推送中），2-已下架 */
    private Integer status;

    /** 上架时间 */
    private Date onlineTime;

    /** 下架时间 */
    private Date offlineTime;

    /** 创建时间（策略分析时间） */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
