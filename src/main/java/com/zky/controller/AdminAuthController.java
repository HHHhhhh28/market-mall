package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.UserInfoRequestDTO;
import com.zky.service.ISysAdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/admin")
public class AdminAuthController {

    @Resource
    private ISysAdminService sysAdminService;

    @PostMapping("/login")
    public Response<String> login(@RequestBody UserInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/login");
        String adminId = sysAdminService.login(request);
        return Response.<String>builder().code("0000").info("Success").data(adminId).build();
    }
}

