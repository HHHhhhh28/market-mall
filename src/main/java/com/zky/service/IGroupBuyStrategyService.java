package com.zky.service;

import com.zky.domain.vo.GroupBuyStrategyVO;

/**
 * 拼团商品策略算法服务接口
 */
public interface IGroupBuyStrategyService {

    /**
     * 对指定商品执行策略分析：
     * 统计用户行为数据，计算热度分，推荐拼团价格和目标用户
     * @param productId 商品ID
     * @param activityId 当前全局活动ID
     * @return 策略分析结果VO
     */
    GroupBuyStrategyVO analyzeProduct(String productId, String activityId);

    /**
     * 上架商品（将策略分析结果写入 group_buy_product，状态设为已上架）
     * @param productId 商品ID
     * @param activityId 活动ID
     */
    void onlineProduct(String productId, String activityId);

    /**
     * 下架商品
     * @param productId 商品ID
     * @param activityId 活动ID
     */
    void offlineProduct(String productId, String activityId);

    /**
     * 更新拼团（保持上架状态，重新计算价值评估并写入 group_buy_product）
     * @param productId 商品ID
     * @param activityId 活动ID
     */
    void refreshProduct(String productId, String activityId);
}
