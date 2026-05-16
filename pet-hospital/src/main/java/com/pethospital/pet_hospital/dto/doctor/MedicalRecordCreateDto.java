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
    
    private Long hospitalizationId;
    
    @NotNull(message = "Pet ID cannot be empty")
    private Long petId;
    
    @NotNull(message = "Doctor ID cannot be empty")
    private Long doctorId;
    
    private String doctorName;
    
    @NotBlank(message = "Chief complaint cannot be empty")
    private String chiefComplaint;
    
    private String symptoms;
    private String presentIllness;
    private String pastHistory;
    private String physicalExam;
    private String auxiliaryExam;
    
    @NotBlank(message = "Diagnosis cannot be empty")
    private String diagnosis;
    
    private String treatmentPlan;
    private String doctorAdvice;
    private String remark;
    
    // ========== Getter & Setter ==========
    
    public Long getRegisterId() {
        return registerId;
    }
    
    public void setRegisterId(Long registerId) {
        this.registerId = registerId;
    }
    
    public Long getHospitalizationId() {
        return hospitalizationId;
    }
    
    public void setHospitalizationId(Long hospitalizationId) {
        this.hospitalizationId = hospitalizationId;
    }
    
    public Long getPetId() {
        return petId;
    }
    
    public void setPetId(Long petId) {
        this.petId = petId;
    }
    
    public Long getDoctorId() {
        return doctorId;
    }
    
    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }
    
    public String getDoctorName() {
        return doctorName;
    }
    
    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }
    
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
    
    @Override
    public String toString() {
        return "MedicalRecordCreateDto{" +
                "registerId=" + registerId +
                ", petId=" + petId +
                ", doctorId=" + doctorId +
                ", doctorName='" + doctorName + '\'' +
                ", chiefComplaint='" + chiefComplaint + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                '}';
    }
}