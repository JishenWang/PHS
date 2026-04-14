// 提示词：全表覆盖；数据库字段名不得猜测，避免漏洞；字段严格来源于 pet_hospital.sql。
package com.pethospital.pet_hospital.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class MedicalRecord {
    // 数据库字段：id
    private Long id;
    // 数据库字段：record_no
    private String recordNo;
    // 数据库字段：register_id
    private Long registerId;
    // 数据库字段：pet_id
    private Long petId;
    // 数据库字段：owner_id
    private Long ownerId;
    // 数据库字段：doctor_id
    private Long doctorId;
    // 数据库字段：doctor_name
    private String doctorName;
    // 数据库字段：type
    private String type;
    // 数据库字段：title
    private String title;
    // 数据库字段：content
    private String content;
    // 数据库字段：chief_complaint
    private String chiefComplaint;
    // 数据库字段：symptoms
    private String symptoms;
    // 数据库字段：present_illness
    private String presentIllness;
    // 数据库字段：past_history
    private String pastHistory;
    // 数据库字段：physical_exam
    private String physicalExam;
    // 数据库字段：auxiliary_exam
    private String auxiliaryExam;
    // 数据库字段：diagnosis
    private String diagnosis;
    // 数据库字段：prescription
    private String prescription;
    // 数据库字段：treatment_plan
    private String treatmentPlan;
    // 数据库字段：doctor_advice
    private String doctorAdvice;
    // 数据库字段：hospital
    private String hospital;
    // 数据库字段：attachments
    private String attachments;
    // 数据库字段：record_date
    private LocalDate recordDate;
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
