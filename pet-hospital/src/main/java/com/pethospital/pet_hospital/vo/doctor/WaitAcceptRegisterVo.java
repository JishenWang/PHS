package com.pethospital.pet_hospital.vo.doctor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class WaitAcceptRegisterVo {
    
    private Long registerId;
    private String registerNo;
    
    private Long petId;
    private String petName;
    private String petType;
    private String breed;
    private Integer age;
    private String gender;
    
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    
    private String serviceType;
    private String symptom;
    private Integer status;
    private String statusDesc;
    private BigDecimal amount;
    private LocalDateTime registerTime;
    private LocalDateTime acceptTime;
}