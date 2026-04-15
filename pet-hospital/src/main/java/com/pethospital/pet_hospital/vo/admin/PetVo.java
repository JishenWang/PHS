package com.pethospital.pet_hospital.vo.admin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class PetVo {
    private Long id;
    private String name;
    private String type;
    private String breed;
    private String gender;
    private Integer age;
    private BigDecimal weight;
    private String ownerName;
    private String ownerPhone;
    private String ownerAddress;
    private String healthStatus;
    private List<String> vaccines;
    private String allergies;
    private String remark;
    private LocalDateTime lastVisit; 
    private LocalDateTime createTime;
}