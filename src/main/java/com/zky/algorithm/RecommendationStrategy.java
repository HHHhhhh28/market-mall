package com.zky.algorithm;

import com.zky.common.enums.RecommendationType;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;

import java.util.List;

public interface RecommendationStrategy {
    RecommendationType getType();
    List<ProductInfo> recommend(UserInfo user, List<ProductInfo> candidates);
}
