package com.zky.domain.dto;

import lombok.Data;

@Data
public class AdminUserUpdateRequestDTO {
    private String userId;
    private String username;
    private String password;
    private String phone;
    private Integer age;
    private Integer gender;
    private String city;
}

