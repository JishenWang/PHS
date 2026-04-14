package com.pethospital.pet_hospital.dto.doctor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 病历创建DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
public class MedicalRecordCreateDto {
    
    private Long registerId;
    private Long petId;
    private Long doctorId;
    private String chiefComplaint;
    private String symptoms;
    private String presentIllness;
    private String pastHistory;
    private String physicalExam;
    private String auxiliaryExam;
    private String diagnosis;
    private String treatmentPlan;
    private String doctorAdvice;
    private String remark;
    
    public MedicalRecordCreateDto() {}
    
    @NotNull(message = "挂号ID不能为空")
    public Long getRegisterId() {
        return registerId;
    }
    
    public void setRegisterId(Long registerId) {
        this.registerId = registerId;
    }
    
    @NotNull(message = "宠物ID不能为空")
    public Long getPetId() {
        return petId;
    }
    
    public void setPetId(Long petId) {
        this.petId = petId;
    }
    
    @NotNull(message = "医生ID不能为空")
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    @NotBlank(message = "主诉不能为空")
    public String getChiefComplaint() {
        return chiefComplaint;
    }
    
    public void setChiefComplaint(String chiefComplaint) {
        this.chiefComplaint = chiefComplaint;
    }
    
    public String getSymptoms() {
        return symptoms;
    }
    
    public void setSymptoms(String symptoms) {
        this.symptoms = symptoms;
    }
    
    public String getPresentIllness() {
        return presentIllness;
    }
    
    public void setPresentIllness(String presentIllness) {
        this.presentIllness = presentIllness;
    }
    
    public String getPastHistory() {
        return pastHistory;
    }
    
    public void setPastHistory(String pastHistory) {
        this.pastHistory = pastHistory;
    }
    
    public String getPhysicalExam() {
        return physicalExam;
    }
    
    public void setPhysicalExam(String physicalExam) {
        this.physicalExam = physicalExam;
    }
    
    public String getAuxiliaryExam() {
        return auxiliaryExam;
    }
    
    public void setAuxiliaryExam(String auxiliaryExam) {
        this.auxiliaryExam = auxiliaryExam;
    }
    
    @NotBlank(message = "诊断结果不能为空")
    public String getDiagnosis() {
        return diagnosis;
    }
    
    public void setDiagnosis(String diagnosis) {
        this.diagnosis = diagnosis;
    }
    
    public String getTreatmentPlan() {
        return treatmentPlan;
    }
    
    public void setTreatmentPlan(String treatmentPlan) {
        this.treatmentPlan = treatmentPlan;
    }
    
    public String getDoctorAdvice() {
        return doctorAdvice;
    }
    
    public void setDoctorAdvice(String doctorAdvice) {
        this.doctorAdvice = doctorAdvice;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark;
    }
}