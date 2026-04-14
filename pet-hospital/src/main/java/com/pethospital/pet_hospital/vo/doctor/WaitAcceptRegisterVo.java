package com.pethospital.pet_hospital.vo.doctor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 待接诊挂号VO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WaitAcceptRegisterVo {
    
    private Long registerId;        // 挂号ID
    private String registerNo;      // 挂号单号
    
    // 宠物信息
    private Long petId;
    private String petName;
    private String petType;         // 宠物类型
    private String breed;           // 品种
    private Integer age;            // 年龄
    private String gender;          // 性别
    
    // 主人信息
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    
    // 挂号信息
    private String serviceType;     // 服务类型
    private String symptom;         // 症状描述
    private Integer status;         // 状态：0-待接诊，1-接诊中，2-已完成，3-已取消
    private String statusDesc;
    private BigDecimal amount;      // 金额
    private LocalDateTime registerTime;
    private LocalDateTime acceptTime;
}