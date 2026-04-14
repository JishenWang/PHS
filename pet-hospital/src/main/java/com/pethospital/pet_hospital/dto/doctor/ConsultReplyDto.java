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
    private String images;
    
    public ConsultReplyDto() {}
    
    @NotNull(message = "咨询ID不能为空")
    public Long getConsultId() {
        return consultId;
    }
    
    public void setConsultId(Long consultId) {
        this.consultId = consultId;
    }
    
    @NotNull(message = "医生ID不能为空")
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    @NotBlank(message = "回复内容不能为空")
    public String getReplyContent() {
        return replyContent;
    }
    
    public void setReplyContent(String replyContent) {
        this.replyContent = replyContent;
    }
    
    public String getImages() {
        return images;
    }
    
    public void setImages(String images) {
        this.images = images;
    }
}