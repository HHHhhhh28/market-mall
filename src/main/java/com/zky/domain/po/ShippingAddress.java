package com.zky.domain.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShippingAddress {
    private Long id;
    private String userId;
    private String receiverName;
    private String receiverPhone;
    private String province;
    private String city;
    private String detailAddress;
    private Integer isDefault;
    private Date createTime;
    private Date updateTime;
}
