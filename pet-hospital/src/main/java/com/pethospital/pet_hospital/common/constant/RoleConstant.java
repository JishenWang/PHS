package com.pethospital.pet_hospital.common.constant;

/**
 * 角色常量
 */
public class RoleConstant {

    // 角色标识
    public static final String ROLE_ADMIN = "admin";      // 超级管理员
    public static final String ROLE_OWNER = "owner";      // 宠物主人
    public static final String ROLE_DESK = "desk";        // 前台工作人员
    public static final String ROLE_DOCTOR = "doctor";    // 宠物医生

    // 角色权限前缀
    public static final String ROLE_PREFIX = "ROLE_";

    // 完整角色名
    public static final String ROLE_ADMIN_FULL = ROLE_PREFIX + ROLE_ADMIN;
    public static final String ROLE_OWNER_FULL = ROLE_PREFIX + ROLE_OWNER;
    public static final String ROLE_DESK_FULL = ROLE_PREFIX + ROLE_DESK;
    public static final String ROLE_DOCTOR_FULL = ROLE_PREFIX + ROLE_DOCTOR;

    /**
     * 判断是否为有效角色
     */
    public static boolean isValidRole(String role) {
        return ROLE_ADMIN.equals(role) || ROLE_OWNER.equals(role) || 
               ROLE_DESK.equals(role) || ROLE_DOCTOR.equals(role);
    }
}