package com.pethospital.pet_hospital.vo.owner;

import lombok.Data;

/**
 * 用户信息视图对象
 */
@Data
public class UserInfoVo {

    private Long id;

    private String username;

    private String phone;

    private String email;

    private String role;

    private String roleName;

    private String avatar;

    private String createTime;
}
