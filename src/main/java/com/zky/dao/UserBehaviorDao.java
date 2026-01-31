package com.zky.dao;

import com.zky.domain.po.UserBehavior;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface UserBehaviorDao {
    List<UserBehavior> selectAll();

    int insert(UserBehavior behavior);
}
