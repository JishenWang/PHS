package com.pethospital.pet_hospital.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Reserve {
    // 数据库字段：id
    private Long id;
    // 数据库字段：appointment_no
    private String appointmentNo;
    // 数据库字段：owner_user_id
    private Long ownerUserId;
    // 数据库字段：owner_id
    private Long ownerId;
    // 数据库字段：pet_id
    private Long petId;
    // 数据库字段：doctor_id
    private Long doctorId;
    // 数据库字段：service_item_id
    private Long serviceItemId;
    // 数据库字段：service_type
    private String serviceType;
    // 数据库字段：appointment_time
    private LocalDateTime appointmentTime;
    // 数据库字段：symptom_desc
    private String symptomDesc;
    // 数据库字段：remark
    private String remark;
    // 数据库字段：source_type
    private String sourceType;
    // 数据库字段：status
    private Integer status;
    // 数据库字段：status_text
    private String statusText;
    // 数据库字段：cancel_reason
    private String cancelReason;
    // 数据库字段：verified_by
    private Long verifiedBy;
    // 数据库字段：verified_time
    private LocalDateTime verifiedTime;
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
