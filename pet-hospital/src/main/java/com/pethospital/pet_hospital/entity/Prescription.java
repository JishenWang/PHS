package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("prescription")
public class Prescription {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String prescriptionNo;
    
    private Long registerId;
    
    private Long hospitalizationId;
    
    private Long recordId;
    
    private Long petId;
    
    private Long doctorId;
    
    private Integer prescriptionType;
    
    private String diagnosis;
    
    private BigDecimal totalAmount;
    
    private Integer status;
    
    private String remark;

    @TableField(exist = false)
    private String serviceNames;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer isDeleted;
}