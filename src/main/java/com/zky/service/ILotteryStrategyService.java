package com.zky.service;

import com.zky.domain.vo.LotteryStrategyVO;
import java.util.List;

/**
 * 抽奖优惠券策略算法服务
 */
public interface ILotteryStrategyService {

    /**
     * 对指定优惠券执行策略分析
     * 计算该优惠券上架到抽奖池后的商家ROI、用户转化预期等
     */
    LotteryStrategyVO analyzeCoupon(String couponId);

    /**
     * 上架优惠券到抽奖池（保持分析结果写入DB，status=1）
     */
    void onlineCoupon(String couponId);

    /**
     * 下架优惠券（status=2）
     */
    void offlineCoupon(String couponId);

    /**
     * 更新已上架优惠券的策略数据（重新计算）
     */
    LotteryStrategyVO refreshCoupon(String couponId);

    /**
     * 查询所有优惠券策略列表（管理端）
     */
    List<LotteryStrategyVO> listAllStrategies();
}
