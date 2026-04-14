package com.pethospital.pet_hospital.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 宠物性别枚举
 */
public enum PetGenderEnum {

    MALE(1, "male", "公"),
    FEMALE(2, "female", "母"),
    UNKNOWN(3, "unknown", "未知");

    @EnumValue
    private final Integer code;
    
    @JsonValue
    private final String value;
    
    private final String description;

    PetGenderEnum(Integer code, String value, String description) {
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

    public static PetGenderEnum fromValue(String value) {
        for (PetGenderEnum gender : values()) {
            if (gender.value.equals(value)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
    
    public static PetGenderEnum fromCode(Integer code) {
        for (PetGenderEnum gender : values()) {
            if (gender.code.equals(code)) {
                return gender;
            }
        }
        return UNKNOWN;
    }
}