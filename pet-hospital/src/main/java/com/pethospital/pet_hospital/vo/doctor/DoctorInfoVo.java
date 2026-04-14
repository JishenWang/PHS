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
    
    private Long doctorId;
    private Long userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String avatar;
    private String title;
    private String department;
    private String specialty;
    private String introduction;
    private Integer status;
    private String statusDesc;
    private Integer authStatus;
    private String authRemark;
    private Integer consultCount;
    private Integer acceptCount;
    private Double rating;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}