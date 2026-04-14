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
public class MedicalRecordVo {
    
    private Long recordId;
    private String recordNo;
    private Long registerId;
    private Long petId;
    private Long doctorId;
    private String petName;
    private String petType;
    private String doctorName;
    private String doctorTitle;
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
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}