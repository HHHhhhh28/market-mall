package com.zky.dao;

import com.zky.domain.po.ProductInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ProductDao {
    ProductInfo selectByProductId(@Param("productId") String productId);
    List<ProductInfo> selectList(@Param("keyword") String keyword, @Param("category") String category);
    List<ProductInfo> selectAll();
    /**
     * 更新商品user_tags标签
     * @param productId 商品ID
     * @param userTags 新标签（如：男,18-25,26-35）
     */
    void updateUserTagsByProductId(@Param("productId") String productId, @Param("userTags") String userTags);

    int updateStockReduce(@Param("productId") String productId, @Param("reduceNum") Integer reduceNum);



}
