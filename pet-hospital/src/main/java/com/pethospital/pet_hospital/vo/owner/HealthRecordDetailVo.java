package com.pethospital.pet_hospital.vo.owner;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.Prescription;
import com.pethospital.pet_hospital.entity.PrescriptionItem;

/**
 * 客户端健康记录详情VO（含病历+处方）
 */
public class HealthRecordDetailVo {

    // 病历信息
    private MedicalRecord medicalRecord;

    // 关联处方列表（含明细）
    private List<PrescriptionWithItemsVo> prescriptions;

    // ========== Getter & Setter ==========

    public MedicalRecord getMedicalRecord() {
        return medicalRecord;
    }

    public void setMedicalRecord(MedicalRecord medicalRecord) {
        this.medicalRecord = medicalRecord;
    }

    public List<PrescriptionWithItemsVo> getPrescriptions() {
        return prescriptions;
    }

    public void setPrescriptions(List<PrescriptionWithItemsVo> prescriptions) {
        this.prescriptions = prescriptions;
    }

    /**
     * 处方+明细VO
     */
    public static class PrescriptionWithItemsVo {
        private Long id;
        private String prescriptionNo;
        private String diagnosis;
        private BigDecimal totalAmount;
        private Integer status;
        private String remark;
        private LocalDateTime createTime;
        private List<PrescriptionItem> items;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }

        public String getPrescriptionNo() { return prescriptionNo; }
        public void setPrescriptionNo(String prescriptionNo) { this.prescriptionNo = prescriptionNo; }

        public String getDiagnosis() { return diagnosis; }
        public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }

        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }

        public LocalDateTime getCreateTime() { return createTime; }
        public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

        public List<PrescriptionItem> getItems() { return items; }
        public void setItems(List<PrescriptionItem> items) { this.items = items; }
    }
}
