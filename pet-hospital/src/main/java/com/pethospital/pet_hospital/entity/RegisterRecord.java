package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class RegisterRecord {
    private Long id;
    private String registerNo;
    private Long appointmentId;
    private Long ownerUserId;
    private Long petId;
    private Long doctorId;
    private Long serviceItemId;
    private Long deskUserId;
    private LocalDateTime registerTime;
    private LocalDateTime acceptTime;
    private String queueNo;
    private String symptom;
    private String triageNote;
    private BigDecimal amount;
    private Integer status;
    private String cancelReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer isDeleted;
}
