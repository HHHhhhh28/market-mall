package com.zky.domain.dto;

import lombok.Data;

@Data
public class UserInfoRequestDTO {
    private String username;
    private String password;
    private String phone;
    private Integer age;
    private Integer gender; // 0: female, 1: male
    private String city;
}
