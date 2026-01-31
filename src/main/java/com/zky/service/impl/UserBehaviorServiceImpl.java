package com.zky.service.impl;

import com.zky.dao.UserBehaviorDao;
import com.zky.domain.dto.UserBehaviorRequestDTO;
import com.zky.domain.po.UserBehavior;
import com.zky.service.IUserBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.Date;

@Slf4j
@Service
public class UserBehaviorServiceImpl implements IUserBehaviorService {

    @Resource
    private UserBehaviorDao userBehaviorDao;

    @Override
    public void recordBehavior(UserBehaviorRequestDTO request) {
        if (request == null || StringUtils.isBlank(request.getUserId()) || StringUtils.isBlank(request.getBehaviorType())) {
            return;
        }

        // 处理单商品行为 (CLICK, COLLECT)
        if (StringUtils.isNotBlank(request.getProductId())) {
            insertBehavior(request.getUserId(), request.getProductId(), request.getBehaviorType());
        }

        // 处理多商品行为 (BUY)
        if (!CollectionUtils.isEmpty(request.getProductIds())) {
            for (String productId : request.getProductIds()) {
                // 避免重复插入（如果 productId 已经在上面被处理过）
                if (StringUtils.equals(productId, request.getProductId())) {
                    continue;
                }
                insertBehavior(request.getUserId(), productId, request.getBehaviorType());
            }
        }
    }

    private void insertBehavior(String userId, String productId, String behaviorType) {
        UserBehavior behavior = new UserBehavior();
        behavior.setUserId(userId);
        behavior.setProductId(productId);
        behavior.setBehaviorType(behaviorType);
        behavior.setCreateTime(new Date());
        
        try {
            userBehaviorDao.insert(behavior);
        } catch (Exception e) {
            log.error("记录用户行为失败: userId={}, productId={}, type={}", userId, productId, behaviorType, e);
        }
    }
}
