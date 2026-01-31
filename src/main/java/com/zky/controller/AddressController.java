package com.zky.controller;

import com.zky.common.response.Response;
import com.zky.domain.dto.ShippingAddressRequestDTO;
import com.zky.domain.vo.ShippingAddressVO;
import com.zky.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/mall/address")
@Slf4j
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/{userId}")
    public Response<List<ShippingAddressVO>> getUserAddresses(@PathVariable String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/address/" + userId);
        return Response.<List<ShippingAddressVO>>builder()
                .code("0000")
                .info("Success")
                .data(addressService.getAddressesByUserId(userId))
                .build();
    }

    @PostMapping
    public Response<ShippingAddressVO> addAddress(@RequestBody ShippingAddressRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/address (POST)");
        return Response.<ShippingAddressVO>builder()
                .code("0000")
                .info("Success")
                .data(addressService.addAddress(request))
                .build();
    }

    @PutMapping
    public Response<ShippingAddressVO> updateAddress(@RequestBody ShippingAddressRequestDTO request) {
        log.info("接口 {} 被调用了", "/api/v1/mall/address (PUT)");
        return Response.<ShippingAddressVO>builder()
                .code("0000")
                .info("Success")
                .data(addressService.updateAddress(request))
                .build();
    }

    @DeleteMapping("/{id}")
    public Response<Void> deleteAddress(@PathVariable Long id) {
        log.info("接口 {} 被调用了", "/api/v1/mall/address/" + id + " (DELETE)");
        addressService.deleteAddress(id);
        return Response.<Void>builder()
                .code("0000")
                .info("Success")
                .build();
    }

    @PutMapping("/default/{id}")
    public Response<Void> setDefaultAddress(@PathVariable Long id, @RequestParam String userId) {
        log.info("接口 {} 被调用了", "/api/v1/mall/address/default/" + id);
        addressService.setDefaultAddress(id, userId);
        return Response.<Void>builder()
                .code("0000")
                .info("Success")
                .build();
    }
}
