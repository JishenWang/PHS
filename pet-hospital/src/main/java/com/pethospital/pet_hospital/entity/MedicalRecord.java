package com.pethospital.pet_hospital.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("medical_record")
public class MedicalRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String recordNo;           // 病历编号
    private Long registerId;           // 挂号ID
    private Long petId;
    private Long ownerId;
    private Long doctorId;
    private String doctorName;
    private String type;
    private String title;
    private String content;
    
    // 新增字段
    private String chiefComplaint;     // 主诉
    private String symptoms;           // 症状
    private String presentIllness;     // 现病史
    private String pastHistory;        // 既往史
    private String physicalExam;       // 体格检查
    private String auxiliaryExam;      // 辅助检查
    private String diagnosis;
    private String prescription;
    private String treatmentPlan;      // 治疗方案
    private String doctorAdvice;       // 医嘱
    
    private LocalDate recordDate;
    private String hospital;
    private String attachments;
    private Integer status;            // 状态：0草稿 1已完成
    private Integer isDeleted;         // 逻辑删除：0未删除 1已删除
    private String remark;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;  // 更新时间

    // ========== Getter & Setter ==========
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getRecordNo() { return recordNo; }
    public void setRecordNo(String recordNo) { this.recordNo = recordNo; }

    public Long getRegisterId() { return registerId; }
    public void setRegisterId(Long registerId) { this.registerId = registerId; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getChiefComplaint() { return chiefComplaint; }
    public void setChiefComplaint(String chiefComplaint) { this.chiefComplaint = chiefComplaint; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public String getPresentIllness() { return presentIllness; }
    public void setPresentIllness(String presentIllness) { this.presentIllness = presentIllness; }

    public String getPastHistory() { return pastHistory; }
    public void setPastHistory(String pastHistory) { this.pastHistory = pastHistory; }

    public String getPhysicalExam() { return physicalExam; }
    public void setPhysicalExam(String physicalExam) { this.physicalExam = physicalExam; }

    public String getAuxiliaryExam() { return auxiliaryExam; }
    public void setAuxiliaryExam(String auxiliaryExam) { this.auxiliaryExam = auxiliaryExam; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getTreatmentPlan() { return treatmentPlan; }
    public void setTreatmentPlan(String treatmentPlan) { this.treatmentPlan = treatmentPlan; }

    public String getDoctorAdvice() { return doctorAdvice; }
    public void setDoctorAdvice(String doctorAdvice) { this.doctorAdvice = doctorAdvice; }

    public LocalDate getRecordDate() { return recordDate; }
    public void setRecordDate(LocalDate recordDate) { this.recordDate = recordDate; }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    public String getAttachments() { return attachments; }
    public void setAttachments(String attachments) { this.attachments = attachments; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }
}