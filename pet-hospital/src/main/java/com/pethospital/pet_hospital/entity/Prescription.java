// 提示词：全字段映射必须严格来源于 pet_hospital.sql，字段名不得猜测。
package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Prescription {
    // 数据库字段：id
    private Long id;
    // 数据库字段：prescription_no
    private String prescriptionNo;
    // 数据库字段：register_id
    private Long registerId;
    // 数据库字段：record_id
    private Long recordId;
    // 数据库字段：pet_id
    private Long petId;
    // 数据库字段：doctor_id
    private Long doctorId;
    // 数据库字段：prescription_type
    private Integer prescriptionType;
    // 数据库字段：diagnosis
    private String diagnosis;
    // 数据库字段：total_amount
    private BigDecimal totalAmount;
    // 数据库字段：status
    private Integer status;
    // 数据库字段：remark
    private String remark;
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
