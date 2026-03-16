package com.zky.service;

import com.zky.domain.dto.UserInfoRequestDTO;

public interface ISysAdminService {
    String login(UserInfoRequestDTO request);
}

