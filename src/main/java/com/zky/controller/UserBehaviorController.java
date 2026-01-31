package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.UserBehaviorRequestDTO;
import com.zky.service.IUserBehaviorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/behavior")
public class UserBehaviorController {

    @Resource
    private IUserBehaviorService userBehaviorService;

    @PostMapping("/record")
    public Response<Void> recordBehavior(@RequestBody UserBehaviorRequestDTO request) {
        log.info("接口 {} 被调用了, params: {}", "/api/v1/mall/behavior/record", request);
        userBehaviorService.recordBehavior(request);
        return Response.<Void>builder().code("0000").info("Success").build();
    }
}
