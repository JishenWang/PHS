// 提示词：全字段映射必须严格来源于 pet_hospital.sql，字段名不得猜测。
package com.pethospital.pet_hospital.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class User {
    // 数据库字段：id
    private Long id;
    // 数据库字段：username
    private String username;
    // 数据库字段：password
    private String password;
    // 数据库字段：real_name
    private String realName;
    // 数据库字段：phone
    private String phone;
    // 数据库字段：email
    private String email;
    // 数据库字段：role
    private String role;
    // 数据库字段：role_code
    private String roleCode;
    // 数据库字段：primary_role_code
    private String primaryRoleCode;
    // 数据库字段：avatar
    private String avatar;
    // 数据库字段：avatar_url
    private String avatarUrl;
    // 数据库字段：gender
    private Integer gender;
    // 数据库字段：birthday
    private LocalDate birthday;
    // 数据库字段：status
    private Integer status;
    // 数据库字段：remark
    private String remark;
    // 数据库字段：last_login_time
    private LocalDateTime lastLoginTime;
    // 数据库字段：create_time
    private LocalDateTime createTime;
    // 数据库字段：update_time
    private LocalDateTime updateTime;
    // 数据库字段：created_time
    private LocalDateTime createdTime;
    // 数据库字段：updated_time
    private LocalDateTime updatedTime;
    // 数据库字段：is_deleted
    private Integer isDeleted;
}
