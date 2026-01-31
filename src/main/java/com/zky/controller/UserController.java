package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.UserInfoRequestDTO;
import com.zky.service.IUserService;
import com.zky.domain.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/user")
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public Response<String> login(@RequestBody UserInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/user/login");
        String userId = userService.login(request);
        return Response.<String>builder().code("0000").info("Success").data(userId).build();
    }

    @PostMapping("/register")
    public Response<Void> register(@RequestBody UserInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/user/register");
        userService.register(request);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @GetMapping("/info")
    public Response<UserInfoVO> getUserInfo(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/user/info");
        UserInfoVO vo = userService.getUserInfo(userId);
        return Response.<UserInfoVO>builder().code("0000").info("Success").data(vo).build();
    }
}
