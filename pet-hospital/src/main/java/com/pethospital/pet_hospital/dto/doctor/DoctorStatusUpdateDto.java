package com.pethospital.pet_hospital.dto.doctor;

import jakarta.validation.constraints.NotNull;

/**
 * 医生状态更新DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
public class DoctorStatusUpdateDto {
    
    private Long doctorId;
    private Integer status;
    
    public DoctorStatusUpdateDto() {}
    
    public DoctorStatusUpdateDto(Long doctorId, Integer status) {
        this.doctorId = doctorId;
        this.status = status;
    }
    
    @NotNull(message = "医生ID不能为空")
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    @NotNull(message = "接诊状态不能为空")
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
}