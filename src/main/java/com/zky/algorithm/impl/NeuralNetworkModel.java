package com.zky.algorithm.impl;

import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 纯神经网络推荐模型（5→4→1结构，移除归一化，日志打印完整小数）
 */
@Component
@Slf4j
public class NeuralNetworkModel implements RecommendationStrategy {



    @Override
    public RecommendationType getType() {
        return RecommendationType.GROUP_BUY;
    }

    @Override
    public List<ProductInfo> recommend(UserInfo user, List<ProductInfo> candidates) {
        log.info("神经网络模型开始为用户 {} 进行推荐，候选商品数量：{}", user.getUserId(), candidates.size());
        return  new ArrayList<>();
    }


}
