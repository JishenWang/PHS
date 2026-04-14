package com.pethospital.pet_hospital.dto.owner;

import lombok.Data;

/**
 * 绑定邮箱数据传输对象
 */
@Data
public class BindEmailDto {

    private String email;

    private String code;
}
