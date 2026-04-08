package com.zky.dao;

import com.zky.domain.po.FavoriteInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FavoriteDao {
    int insert(FavoriteInfo favoriteInfo);
    int delete(@Param("userId") String userId, @Param("productId") String productId);
    FavoriteInfo selectOne(@Param("userId") String userId, @Param("productId") String productId);
    List<FavoriteInfo> selectByUserId(@Param("userId") String userId);
    
    /**
     * 查询指定商品的收藏数
     */
    int countByProductId(@Param("productId") String productId);
}
