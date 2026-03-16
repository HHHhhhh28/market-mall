package com.zky.dao;

import com.zky.domain.po.SysAdmin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SysAdminDao {
    SysAdmin selectByUsername(@Param("username") String username);
}

