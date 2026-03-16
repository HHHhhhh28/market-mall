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
public class SysAdmin {
    private Long id;
    private String adminId;
    private String username;
    private String password;
    private Integer status;
    private Date createTime;
    private Date updateTime;
}
