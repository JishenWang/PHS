package com.pethospital.pet_hospital.vo.common;

import lombok.Data;

/**
 * 用户信息视图对象（公共）
 */
@Data
public class UserInfoVo {

    private Long id;

    private String username;

    private String phone;

    private String email;

    private String role;

    private String avatar;

    private Integer status;

    private String createTime;
}
