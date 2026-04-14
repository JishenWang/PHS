// 提示词：全字段映射必须严格来源于 pet_hospital.sql，字段名不得猜测。
package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("doctor")
public class Doctor {
    // 数据库字段：id
    @TableId(type = IdType.AUTO)
    private Long id;
    // 数据库字段：user_id
    private Long userId;
    // 数据库字段：doctor_no
    private String doctorNo;
    // 数据库字段：dept_id
    private Long deptId;
    // 数据库字段：name
    private String name;
    // 数据库字段：department
    @TableField("department")
    private String department;
    // 数据库字段：title
    private String title;
    // 数据库字段：phone
    private String phone;
    // 数据库字段：specialty
    private String specialty;
    // 数据库字段：introduction
    private String introduction;
    // 数据库字段：avatar_url
    private String avatarUrl;
    // 数据库字段：patient_count
    private Integer patientCount;
    // 数据库字段：consultation_fee
    private BigDecimal consultationFee;
    // 数据库字段：work_status
    @TableField("work_status")
    private Integer workStatusCode;
    // 兼容前端状态字符串字段
    private String workStatus;
    // 数据库字段：auth_status
    private Integer authStatus;
    // 数据库字段：auth_remark
    private String authRemark;
    // 数据库字段：rating
    private BigDecimal rating;
    // 数据库字段：status
    private Integer status;
    // 数据库字段：create_time
    private LocalDateTime createTime;
    // 数据库字段：update_time
    private LocalDateTime updateTime;
    // 数据库字段：created_time
    private LocalDateTime createdTime;
    // 数据库字段：updated_time
    private LocalDateTime updatedTime;
    // 数据库字段：is_deleted
    @TableField("is_deleted")
    private Integer isDeleted;
}
