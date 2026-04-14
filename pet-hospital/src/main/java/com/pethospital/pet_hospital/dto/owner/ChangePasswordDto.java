package com.pethospital.pet_hospital.dto.owner;

import lombok.Data;

/**
 * 修改密码数据传输对象
 */
@Data
public class ChangePasswordDto {

    private String oldPassword;

    private String newPassword;
}