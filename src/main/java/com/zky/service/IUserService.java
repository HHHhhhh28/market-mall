package com.zky.service;

import com.zky.domain.dto.UserInfoRequestDTO;
import com.zky.domain.vo.UserInfoVO;

public interface IUserService {
    String login(UserInfoRequestDTO request);
    void register(UserInfoRequestDTO request);
    UserInfoVO getUserInfo(String userId);
}
