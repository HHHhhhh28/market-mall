package com.zky.service.impl;

import com.zky.dao.SysAdminDao;
import com.zky.domain.dto.UserInfoRequestDTO;
import com.zky.domain.po.SysAdmin;
import com.zky.service.ISysAdminService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
public class SysAdminServiceImpl implements ISysAdminService {

    @Resource
    private SysAdminDao sysAdminDao;

    @Override
    public String login(UserInfoRequestDTO request) {
        SysAdmin admin = sysAdminDao.selectByUsername(request.getUsername());
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        if (admin.getStatus() == null || admin.getStatus() != 1) {
            throw new RuntimeException("管理员账号已禁用");
        }
        String hashedInput = hashPassword(request.getPassword());
        if (!admin.getPassword().equals(hashedInput)) {
            throw new RuntimeException("用户名或密码错误");
        }
        return admin.getAdminId();
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

