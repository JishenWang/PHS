package com.pethospital.pet_hospital.common.constant;

/**
 * URL路径常量
 */
public class UrlConstant {

    // API基础路径
    public static final String API_PREFIX = "/api";
    
    // 认证相关
    public static final String AUTH = API_PREFIX + "/auth";
    public static final String LOGIN = API_PREFIX + "/login";
    public static final String LOGOUT = API_PREFIX + "/logout";
    public static final String REGISTER = API_PREFIX + "/register";
    
    // 管理端
    public static final String ADMIN = API_PREFIX + "/admin";
    public static final String ADMIN_USER = ADMIN + "/user";
    public static final String ADMIN_ROLE = ADMIN + "/role";
    public static final String ADMIN_STATISTICS = ADMIN + "/statistics";
    
    // 客户自助端
    public static final String OWNER = API_PREFIX + "/owner";
    public static final String OWNER_PET = OWNER + "/pet";
    public static final String OWNER_HEALTH = OWNER + "/health";
    public static final String OWNER_RESERVE = OWNER + "/reserve";
    public static final String OWNER_CONSULT = OWNER + "/consult";
    public static final String OWNER_ORDER = OWNER + "/order";
    
    // 前台收银端
    public static final String DESK = API_PREFIX + "/desk";
    public static final String DESK_REGISTER = DESK + "/register";
    public static final String DESK_CHARGE = DESK + "/charge";
    public static final String DESK_CUSTOMER = DESK + "/customer";
    
    // 医生端
    public static final String DOCTOR = API_PREFIX + "/doctor";
    public static final String DOCTOR_ACCEPT = DOCTOR + "/accept";
    public static final String DOCTOR_RECORD = DOCTOR + "/record";
    public static final String DOCTOR_PRESCRIPTION = DOCTOR + "/prescription";
    public static final String DOCTOR_CONSULT = DOCTOR + "/consult";
    
    // 公共
    public static final String COMMON = API_PREFIX + "/common";
    public static final String COMMON_UPLOAD = COMMON + "/upload";
    public static final String COMMON_DOWNLOAD = COMMON + "/download";
}
