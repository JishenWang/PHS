package com.pethospital.pet_hospital.dto.doctor;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

/**
 * 处方创建DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionCreateDto {
    
    /**
     * 挂号ID
     */
    @NotNull(message = "挂号ID不能为空")
    private Long registerId;
    
    /**
     * 病历ID
     */
    private Long recordId;
    
    /**
     * 宠物ID
     */
    @NotNull(message = "宠物ID不能为空")
    private Long petId;
    
    /**
     * 医生ID
     */
    @NotNull(message = "医生ID不能为空")
    private Long doctorId;
    
    /**
     * 处方类型：0-西药，1-中药，2-手术，3-检查
     */
    @NotNull(message = "处方类型不能为空")
    private Integer prescriptionType;
    
    /**
     * 诊断结果
     */
    private String diagnosis;
    
    /**
     * 药品列表
     */
    private List<PrescriptionDrugDto> drugs;
    
    /**
     * 服务项目列表
     */
    private List<PrescriptionServiceDto> services;
    
    /**
     * 总金额
     */
    private BigDecimal totalAmount;
    
    /**
     * 备注
     */
    private String remark;

    
    
    /**
     * 处方药品项DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionDrugDto {
        private Long drugId;        // 药品ID
        private String drugName;    // 药品名称
        private Integer quantity;   // 数量
        private String dosage;      // 剂量
        private String usage;       // 用法
        private String frequency;   // 频次
        private Integer days;       // 天数
        private BigDecimal price;   // 单价
        private BigDecimal amount;  // 小计
    }
    
    /**
     * 处方服务项DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionServiceDto {
        private Long serviceId;     // 服务ID
        private String serviceName; // 服务名称
        private Integer quantity;   // 数量
        private BigDecimal price;   // 单价
        private BigDecimal amount;  // 小计
    }
}