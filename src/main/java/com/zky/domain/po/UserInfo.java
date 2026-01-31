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
public class UserInfo {
    private Long id;
    private String userId;
    private String username;
    private String password;
    private String phone;
    private Integer age;
    private Integer gender; // 0: female, 1: male
    private String city;
    private Date createTime;
    private Date updateTime;
}
