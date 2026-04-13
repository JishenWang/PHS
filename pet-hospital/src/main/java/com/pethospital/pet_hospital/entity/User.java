package com.pethospital.pet_hospital.entity;

import lombok.Data;

@Data
public class User {
    private Long id;
    private String username;
    private String password;
    private String realName;
    private String phone;
    /**
     * OWNER/DOCTOR/DESK/ADMIN
     */
    private String role;
    private Integer status;
    private String createdAt;
    private String updatedAt;
}
