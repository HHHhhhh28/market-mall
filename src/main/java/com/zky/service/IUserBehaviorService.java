package com.zky.service;

import com.zky.domain.dto.UserBehaviorRequestDTO;

public interface IUserBehaviorService {
    /**
     * 记录用户行为
     * @param request 行为数据
     */
    void recordBehavior(UserBehaviorRequestDTO request);
}
