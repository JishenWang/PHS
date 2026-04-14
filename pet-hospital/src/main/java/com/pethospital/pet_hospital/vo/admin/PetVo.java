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
    private BigDecimal weight;      // 新增：体重
    private String ownerName;
    private String ownerPhone;
    private String ownerAddress;    // 新增：主人地址
    private String healthStatus;    // 新增：健康状态
    private List<String> vaccines;  // 新增：疫苗记录（前端传数组）
    private String allergies;       // 新增：过敏史
    private String remark;          // 新增：备注
    private LocalDateTime createTime;
}