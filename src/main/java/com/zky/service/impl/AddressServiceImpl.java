package com.zky.service.impl;

import com.zky.dao.ShippingAddressDao;
import com.zky.domain.dto.ShippingAddressRequestDTO;
import com.zky.domain.po.ShippingAddress;
import com.zky.domain.vo.ShippingAddressVO;
import com.zky.service.AddressService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AddressServiceImpl implements AddressService {

    @Autowired
    private ShippingAddressDao shippingAddressDao;

    @Override
    public List<ShippingAddressVO> getAddressesByUserId(String userId) {
        List<ShippingAddress> addresses = shippingAddressDao.selectByUserId(userId);
        return addresses.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ShippingAddressVO addAddress(ShippingAddressRequestDTO request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            shippingAddressDao.clearDefault(request.getUserId());
        }
        
        ShippingAddress address = new ShippingAddress();
        BeanUtils.copyProperties(request, address);
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0);
        
        shippingAddressDao.insert(address);
        
        return convertToVO(address);
    }

    @Override
    public void deleteAddress(Long id) {
        shippingAddressDao.deleteById(id);
    }

    @Override
    @Transactional
    public ShippingAddressVO updateAddress(ShippingAddressRequestDTO request) {
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            shippingAddressDao.clearDefault(request.getUserId());
        }

        ShippingAddress address = new ShippingAddress();
        BeanUtils.copyProperties(request, address);
        address.setIsDefault(Boolean.TRUE.equals(request.getIsDefault()) ? 1 : 0);

        shippingAddressDao.update(address);
        
        ShippingAddress updated = shippingAddressDao.selectById(address.getId());
        return convertToVO(updated);
    }

    @Override
    @Transactional
    public void setDefaultAddress(Long id, String userId) {
        shippingAddressDao.clearDefault(userId);
        
        ShippingAddress address = new ShippingAddress();
        address.setId(id);
        address.setIsDefault(1);
        shippingAddressDao.update(address);
    }
    
    private ShippingAddressVO convertToVO(ShippingAddress po) {
        if (po == null) return null;
        ShippingAddressVO vo = new ShippingAddressVO();
        BeanUtils.copyProperties(po, vo);
        vo.setIsDefault(po.getIsDefault() != null && po.getIsDefault() == 1);
        return vo;
    }
}
