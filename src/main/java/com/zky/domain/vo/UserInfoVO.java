package com.zky.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoVO {
    private String userId;
    private String username;
    private String phone;
    private Integer age;
    private Integer gender;
    private String city;
}
