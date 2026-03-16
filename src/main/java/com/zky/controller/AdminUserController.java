package com.zky.controller;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.zky.common.response.Response;
import com.zky.dao.UserDao;
import com.zky.domain.dto.AdminUserUpdateRequestDTO;
import com.zky.domain.dto.UserInfoRequestDTO;
import com.zky.domain.po.UserInfo;
import com.zky.domain.vo.UserInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1/mall/admin/user")
public class AdminUserController {

    @Resource
    private UserDao userDao;

    @PostMapping("/list")
    public Response<PageInfo<UserInfoVO>> list(@RequestBody(required = false) UserInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/user/list");
        int pageNum = 1;
        int pageSize = 10;
        String keyword = null;
        if (request != null) {
            if (request.getPageNum() != null && request.getPageNum() > 0) {
                pageNum = request.getPageNum();
            }
            if (request.getPageSize() != null && request.getPageSize() > 0) {
                pageSize = request.getPageSize();
            }
            keyword = request.getKeyword();
        }
        PageHelper.startPage(pageNum, pageSize);
        List<UserInfo> users = userDao.selectList(keyword);
        PageInfo<UserInfo> pageInfo = new PageInfo<>(users);
        List<UserInfoVO> vos = users.stream().map(u -> {
            UserInfoVO vo = new UserInfoVO();
            vo.setUserId(u.getUserId());
            vo.setUsername(u.getUsername());
            vo.setPhone(u.getPhone());
            vo.setAge(u.getAge());
            vo.setGender(u.getGender());
            vo.setCity(u.getCity());
            return vo;
        }).collect(Collectors.toList());
        PageInfo<UserInfoVO> pageInfoVO = new PageInfo<>();
        BeanUtils.copyProperties(pageInfo, pageInfoVO);
        pageInfoVO.setList(vos);
        return Response.<PageInfo<UserInfoVO>>builder().code("0000").info("Success").data(pageInfoVO).build();
    }

    @PostMapping("/create")
    public Response<Void> create(@RequestBody UserInfoRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/user/create");
        
        // 校验必填字段
        if (request.getUsername() == null || request.getUsername().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("用户名不能为空").build();
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("密码不能为空").build();
        }
        if (request.getPhone() == null || request.getPhone().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("手机号不能为空").build();
        }
        if (request.getAge() == null || request.getAge() < 0) {
            return Response.<Void>builder().code("0001").info("年龄不能为空且不能为负数").build();
        }
        if (request.getGender() == null || (request.getGender() != 0 && request.getGender() != 1)) {
            return Response.<Void>builder().code("0001").info("性别不能为空且只能是0或1").build();
        }
        if (request.getCity() == null || request.getCity().trim().isEmpty()) {
            return Response.<Void>builder().code("0001").info("城市不能为空").build();
        }
        
        if (userDao.selectByUsername(request.getUsername()) != null) {
            return Response.<Void>builder().code("0001").info("用户名已存在").build();
        }
        
        UserInfo user = new UserInfo();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(hashPassword(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setCity(request.getCity());
        userDao.insert(user);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @PostMapping("/update")
    public Response<Void> update(@RequestBody AdminUserUpdateRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/user/update");
        UserInfo existing = userDao.selectByUserId(request.getUserId());
        if (existing == null) {
            throw new RuntimeException("用户不存在");
        }
        if (request.getUsername() != null) {
            existing.setUsername(request.getUsername());
        }
        if (request.getPassword() != null) {
            existing.setPassword(hashPassword(request.getPassword()));
        }
        if (request.getPhone() != null) {
            existing.setPhone(request.getPhone());
        }
        if (request.getAge() != null) {
            existing.setAge(request.getAge());
        }
        if (request.getGender() != null) {
            existing.setGender(request.getGender());
        }
        if (request.getCity() != null) {
            existing.setCity(request.getCity());
        }
        userDao.update(existing);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    @DeleteMapping("/delete")
    public Response<Void> delete(@RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/admin/user/delete");
        userDao.deleteByUserId(userId);
        return Response.<Void>builder().code("0000").info("Success").build();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes());
            byte[] bytes = md.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("密码加密错误", e);
        }
    }
}
