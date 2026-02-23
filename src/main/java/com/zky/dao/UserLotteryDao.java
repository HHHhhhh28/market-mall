package com.zky.dao;

import com.zky.domain.po.UserLottery;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户抽奖次数与签到 DAO
 */
@Mapper
public interface UserLotteryDao {

    /**
     * 按用户ID查询抽奖记录
     */
    UserLottery selectByUserId(@Param("userId") String userId);

    /**
     * 新增用户抽奖记录
     */
    int insert(UserLottery record);

    /**
     * 更新用户抽奖记录（根据 user_id）
     */
    int update(UserLottery record);
}

