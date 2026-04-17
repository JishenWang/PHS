package com.pethospital.pet_hospital.vo.doctor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorInfoVo {
    
    private Long doctorId;      // 对应 Doctor 的 id
    private Long userId;        // 对应 Doctor 的 userId
    
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    
    private String title;       // 职称
    private String department;  // 科室
    private String specialty;   // 专长
    private String introduction; // 简介
    
    private Integer status;      // 工作状态
    private String statusDesc;
    private Integer authStatus;  // 认证状态
    private String authRemark;   // 认证备注
    
    private Integer consultCount; // 咨询数
    private Integer acceptCount;  // 接诊数
    private Double rating;        // 评分
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}