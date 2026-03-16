package com.zky.dao;

import com.zky.domain.po.ProductInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface ProductDao {
    ProductInfo selectByProductId(@Param("productId") String productId);
    List<ProductInfo> selectList(@Param("keyword") String keyword, @Param("category") String category);
    List<ProductInfo> selectAll();
    void updateUserTagsByProductId(@Param("productId") String productId, @Param("userTags") String userTags);
    int updateStockReduce(@Param("productId") String productId, @Param("reduceNum") Integer reduceNum);
    int insert(ProductInfo productInfo);
    int update(ProductInfo productInfo);
    int deleteByProductId(@Param("productId") String productId);
    Integer selectTotalProductCount();
    List<Map<String, Object>> selectTopProducts();
    List<Map<String, Object>> selectLowStockProducts(@Param("threshold") Integer threshold);
}
