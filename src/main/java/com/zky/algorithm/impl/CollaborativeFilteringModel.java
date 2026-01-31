package com.zky.algorithm.impl;

import com.zky.algorithm.RecommendationStrategy;
import com.zky.common.enums.RecommendationType;
import com.zky.dao.OrderDao;
import com.zky.domain.po.OrderInfo;
import com.zky.domain.po.OrderItem;
import com.zky.domain.po.ProductInfo;
import com.zky.domain.po.UserInfo;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 协同过滤推荐算法 - 针对抽奖商品
 * Item-Based CF: 推荐与用户过去喜欢的物品相似的物品
 */
@Component
public class CollaborativeFilteringModel implements RecommendationStrategy {

    @Resource
    private OrderDao orderDao;

    @Override
    public RecommendationType getType() {
        return RecommendationType.LOTTERY;
    }

    @Override
    public List<ProductInfo> recommend(UserInfo user, List<ProductInfo> candidates) {
        // Build User History from Orders
        Map<String, Integer> userHistory = new HashMap<>();
        if (user.getUserId() != null) {
            List<OrderInfo> orders = orderDao.selectOrdersByUserId(user.getUserId());
            for (OrderInfo order : orders) {
                List<OrderItem> items = orderDao.selectOrderItemsByOrderId(order.getOrderId());
                for (OrderItem item : items) {
                    userHistory.merge(item.getProductId(), item.getQuantity(), Integer::sum);
                }
            }
        }

        if (userHistory.isEmpty()) {
            return candidates; // No history, return default order
        }

        Map<ProductInfo, Double> scores = new HashMap<>();

        for (ProductInfo candidate : candidates) {
            double totalScore = 0.0;
            
            for (Map.Entry<String, Integer> entry : userHistory.entrySet()) {
                String historyItemId = entry.getKey();
                Integer interactionCount = entry.getValue();
                
                // Similarity between Candidate Item and History Item
                double similarity = calculateItemSimilarity(candidate.getProductId(), historyItemId);
                
                // Weighted Sum
                totalScore += similarity * interactionCount;
            }
            scores.put(candidate, totalScore);
        }

        return candidates.stream()
                .sorted((p1, p2) -> Double.compare(scores.get(p2), scores.get(p1)))
                .collect(Collectors.toList());
    }

    // Mock Item Similarity Matrix calculation
    // In real world, this is pre-calculated from user-item interaction matrix
    private double calculateItemSimilarity(String itemA, String itemB) {
        if (itemA == null || itemB == null) return 0.0;
        if (itemA.equals(itemB)) return 1.0;
        // Mock similarity based on hashcode proximity for demo
        int diff = Math.abs(itemA.hashCode() - itemB.hashCode());
        return 1.0 / (1.0 + Math.log(diff + 1)); 
    }
}
