package com.zky.dao;

import com.zky.domain.po.CartInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface CartDao {
    int insert(CartInfo cartInfo);
    int update(CartInfo cartInfo);
    int delete(@Param("userId") String userId, @Param("productId") String productId);
    CartInfo selectOne(@Param("userId") String userId, @Param("productId") String productId);
    List<CartInfo> selectByUserId(@Param("userId") String userId);
    int deleteByUserId(@Param("userId") String userId);
}
