package com.pethospital.pet_hospital.dto.common;

import lombok.Data;

/**
 * 登录数据传输对象
 */
@Data
public class LoginDto {

    private String username;

    private String password;

    private Boolean remember;  // 记住我
}