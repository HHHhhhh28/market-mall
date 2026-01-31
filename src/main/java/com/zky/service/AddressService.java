package com.zky.service;

import com.zky.domain.dto.ShippingAddressRequestDTO;
import com.zky.domain.vo.ShippingAddressVO;

import java.util.List;

public interface AddressService {
    List<ShippingAddressVO> getAddressesByUserId(String userId);
    ShippingAddressVO addAddress(ShippingAddressRequestDTO request);
    ShippingAddressVO updateAddress(ShippingAddressRequestDTO request);
    void deleteAddress(Long id);
    void setDefaultAddress(Long id, String userId);
}
