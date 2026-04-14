// 提示词：全字段映射必须严格来源于 pet_hospital.sql，字段名不得猜测。
package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Pet {
    // 数据库字段：id
    private Long id;
    // 数据库字段：owner_user_id
    private Long ownerUserId;
    // 数据库字段：owner_id
    private Long ownerId;
    // 数据库字段：owner_name
    private String ownerName;
    // 数据库字段：owner_phone
    private String ownerPhone;
    // 数据库字段：owner_address
    private String ownerAddress;
    // 数据库字段：owner_is_temp
    private Integer ownerIsTemp;
    // 数据库字段：name
    private String name;
    // 数据库字段：avatar
    private String avatar;
    // 数据库字段：type
    private String type;
    // 数据库字段：species
    private String species;
    // 数据库字段：breed
    private String breed;
    // 数据库字段：gender
    private String gender;
    // 数据库字段：gender_code
    private Integer genderCode;
    // 数据库字段：birthday
    private LocalDate birthday;
    // 数据库字段：age
    private Integer age;
    // 数据库字段：weight
    private BigDecimal weight;
    // 数据库字段：color
    private String color;
    // 数据库字段：chip_number
    private String chipNumber;
    // 数据库字段：neutered
    private Integer neutered;
    // 数据库字段：health_status
    private String healthStatus;
    // 数据库字段：status
    private Integer status;
    // 数据库字段：vaccines
    private String vaccines;
    // 数据库字段：allergies
    private String allergies;
    // 数据库字段：allergy
    private String allergy;
    // 数据库字段：allergy_history
    private String allergyHistory;
    // 数据库字段：description
    private String description;
    // 数据库字段：remark
    private String remark;
    // 数据库字段：last_visit
    private LocalDateTime lastVisit;
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
    // 兼容旧前端占位字段
    private String healthRecords;
}
