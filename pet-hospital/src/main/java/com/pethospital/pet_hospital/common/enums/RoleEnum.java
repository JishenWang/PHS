package com.pethospital.pet_hospital.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 角色枚举
 */
public enum RoleEnum {

    ADMIN(1, "admin", "超级管理员"),
    OWNER(2, "owner", "宠物主人"),
    DESK(3, "desk", "前台工作人员"),
    DOCTOR(4, "doctor", "宠物医生");

    @EnumValue  // MyBatis-Plus存储到数据库的值
    private final Integer code;
    
    @JsonValue  // JSON序列化输出的值
    private final String value;
    
    private final String description;

    RoleEnum(Integer code, String value, String description) {
        this.code = code;
        this.value = value;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据value获取枚举
     */
    public static RoleEnum fromValue(String value) {
        for (RoleEnum role : values()) {
            if (role.value.equals(value)) {
                return role;
            }
        }
        return null;
    }

    /**
     * 根据code获取枚举
     */
    public static RoleEnum fromCode(Integer code) {
        for (RoleEnum role : values()) {
            if (role.code.equals(code)) {
                return role;
            }
        }
        return null;
    }
}
