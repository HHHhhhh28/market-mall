package com.zky.service.impl;

import com.zky.common.constans.Constants;
import com.zky.dao.UserDao;
import com.zky.domain.dto.UserInfoRequestDTO;
import com.zky.domain.po.UserInfo;
import com.zky.service.IUserService;
import com.zky.domain.vo.UserInfoVO;
import com.zky.service.IRedisService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserDao userDao;

    @Resource
    private IRedisService redisService;

    @Override
    public String login(UserInfoRequestDTO request) {
        UserInfo user = userDao.selectByUsername(request.getUsername());
        if (user == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        String hashedInput = hashPassword(request.getPassword());
        if (!user.getPassword().equals(hashedInput)) {
            throw new RuntimeException("用户名或密码错误");
        }
        return user.getUserId();
    }

    @Override
    public void register(UserInfoRequestDTO request) {
        if (userDao.selectByUsername(request.getUsername()) != null) {
            throw new RuntimeException("用户名已存在");
        }
        UserInfo user = new UserInfo();
        user.setUserId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(hashPassword(request.getPassword()));
        user.setPhone(request.getPhone());
        user.setAge(request.getAge());
        user.setGender(request.getGender());
        user.setCity(request.getCity());
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        
        userDao.insert(user);
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

    @Override
    public UserInfoVO getUserInfo(String userId) {
        String cacheKey = Constants.RedisKey.MALL_USER_KEY + userId;
        UserInfo user = redisService.getValue(cacheKey);
        
        if (user == null) {
            user = userDao.selectByUserId(userId);
            if (user == null) {
                throw new RuntimeException("用户不存在");
            }
            // 缓存1小时
            redisService.setValue(cacheKey, user, TimeUnit.HOURS.toMillis(1));
        }
        
        UserInfoVO vo = new UserInfoVO();
        vo.setUserId(user.getUserId());
        vo.setUsername(user.getUsername());
        vo.setPhone(user.getPhone());
        vo.setAge(user.getAge());
        vo.setGender(user.getGender());
        vo.setCity(user.getCity());
        return vo;
    }
}
