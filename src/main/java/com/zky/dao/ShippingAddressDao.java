package com.zky.dao;

import com.zky.domain.po.ShippingAddress;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ShippingAddressDao {
    int insert(ShippingAddress address);

    int deleteById(Long id);

    int update(ShippingAddress address);

    int clearDefault(String userId);

    ShippingAddress selectById(Long id);

    List<ShippingAddress> selectByUserId(String userId);
}
