// 提示词：按领域模块聚合实体；字段严格来源于 pet_hospital.sql，不得猜测。
package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class OrderInfo {
    private Long id;
    private String orderNo;
    private Long registerId;
    private Long prescriptionId;
    private Long appointmentId;
    private Long ownerUserId;
    private Long petId;
    private Long doctorId;
    private Long deskUserId;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal reductionAmount;
    private BigDecimal adjustAmount;
    private String adjustReason;
    private BigDecimal payableAmount;
    private BigDecimal paidAmount;
    private Integer payStatus;
    private String payStatusText;
    private String payMethod;
    private LocalDateTime payTime;
    private Integer receiptPrinted;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
}
