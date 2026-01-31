package com.zky.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddressVO {
    private Long id;
    private String userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String detailAddress;
    private Boolean isDefault;
    private Date createTime;
    private Date updateTime;
}
