package com.pethospital.pet_hospital.dto.owner;

import lombok.Data;

/**
 * 绑定手机数据传输对象
 */
@Data
public class BindPhoneDto {

    private String phone;

    private String code;
}
