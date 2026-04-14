package com.pethospital.pet_hospital.vo.admin;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class UserVo {
    private Long id;
    private String username;
    private String password;      // 新增：用于新增用户时接收密码
    private String realName;
    private String role;
    private String phone;
    private String email;
    private Integer status;
    private String remark;        // 新增：备注字段
    private String avatar;
    private LocalDateTime createTime;
}