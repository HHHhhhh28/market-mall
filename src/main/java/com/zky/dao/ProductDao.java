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
}
