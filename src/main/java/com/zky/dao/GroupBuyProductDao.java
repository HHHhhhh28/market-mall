package com.zky.dao;

import com.zky.domain.po.GroupBuyProduct;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 拼团商品策略DAO
 */
@Mapper
public interface GroupBuyProductDao {

    /** 根据活动ID和商品ID查询策略记录 */
    GroupBuyProduct selectByActivityAndProduct(
            @Param("activityId") String activityId,
            @Param("productId") String productId);

    /** 根据商品ID查询最新策略记录 */
    GroupBuyProduct selectLatestByProductId(@Param("productId") String productId);

    /** 查询所有已上架的拼团商品策略（status=1） */
    List<GroupBuyProduct> selectOnlineList();

    /** 查询指定活动下所有已上架商品 */
    List<GroupBuyProduct> selectOnlineByActivityId(@Param("activityId") String activityId);

    /** 插入策略记录 */
    int insert(GroupBuyProduct product);

    /** 更新策略记录 */
    int update(GroupBuyProduct product);

    /** 更新上架状态 */
    int updateStatus(@Param("activityId") String activityId,
                     @Param("productId") String productId,
                     @Param("status") int status);

    /** 批量下架超过 offlineTime 的商品 */
    int batchOfflineExpired();
}
