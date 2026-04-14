package com.pethospital.pet_hospital.vo.admin;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class DoctorVo {
    private Long id;
    private String name;
    private String department;
    private String title;
    private String phone;
    private String specialty;
    private String introduction;   // 新增：医生简介
    private String avatar;       // 新增：头像
    private Integer status;
    private Integer workStatus;  // 新增：工作状态
    private Integer patientCount;  // 新增：接诊数
    private LocalDateTime createTime;
}