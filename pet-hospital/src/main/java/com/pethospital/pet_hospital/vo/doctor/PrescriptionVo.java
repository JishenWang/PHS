package com.pethospital.pet_hospital.vo.doctor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionVo {
    
    private Long prescriptionId;
    private String prescriptionNo;
    private Long registerId;
    private Long recordId;
    private Long petId;
    private Long doctorId;
    private String petName;
    private String doctorName;
    private Integer prescriptionType;
    private String prescriptionTypeDesc;
    private String diagnosis;
    private List<PrescriptionDrugVo> drugs;
    private List<PrescriptionServiceVo> services;
    private BigDecimal totalAmount;
    private Integer payStatus;
    private Integer status;
    private String statusDesc;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime submitTime;
    private LocalDateTime writeOffTime;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionDrugVo {
        private Long drugId;
        private String drugName;
        private String specification;
        private Integer quantity;
        private String dosage;
        private String usage;
        private String frequency;
        private Integer days;
        private BigDecimal price;
        private BigDecimal amount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionServiceVo {
        private Long serviceId;
        private String serviceName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal amount;
    }
}