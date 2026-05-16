package com.pethospital.pet_hospital.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 咨询回复DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
public class ConsultReplyDto {
    
    private Long consultId;
    private Long doctorId;
    private String replyContent;
    private String replyImages;
    
    public ConsultReplyDto() {}
    
    @NotNull(message = "Consultation ID cannot be empty")
    public Long getConsultId() {
        return consultId;
    }
    
    public void setConsultId(Long consultId) {
        this.consultId = consultId;
    }
    
    @NotNull(message = "Doctor ID cannot be empty")
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    @NotBlank(message = "Reply content cannot be empty")
    public String getReplyContent() {
        return replyContent;
    }
    
    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
    
    public String getReplyImages() {
        return replyImages;
    }
    
    public void setReplyImages(String replyImages) {
        this.replyImages = replyImages;
    }
}