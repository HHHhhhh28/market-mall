package com.zky.dao;

import com.zky.domain.po.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDao {
    int insert(UserInfo userInfo);
    UserInfo selectByUserId(@Param("userId") String userId);
    UserInfo selectByUsername(@Param("username") String username);
    java.util.List<UserInfo> selectAll();
    int update(UserInfo userInfo);
}
