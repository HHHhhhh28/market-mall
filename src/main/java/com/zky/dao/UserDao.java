package com.zky.dao;

import com.zky.domain.po.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface UserDao {
    int insert(UserInfo userInfo);
    UserInfo selectByUserId(@Param("userId") String userId);
    UserInfo selectByUsername(@Param("username") String username);
    List<UserInfo> selectAll();
    List<UserInfo> selectList(@Param("keyword") String keyword);
    int update(UserInfo userInfo);
    int deleteByUserId(@Param("userId") String userId);
    Integer selectTotalUserCount();
    Integer selectTodayNewUserCount();
    List<Map<String, Object>> selectCityDistribution();
}
