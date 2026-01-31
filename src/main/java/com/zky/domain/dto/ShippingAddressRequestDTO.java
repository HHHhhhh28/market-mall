package com.zky.domain.dto;

import lombok.Data;

@Data
public class ShippingAddressRequestDTO {
    private Long id;
    private String userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String detailAddress;
    private Boolean isDefault;
}
