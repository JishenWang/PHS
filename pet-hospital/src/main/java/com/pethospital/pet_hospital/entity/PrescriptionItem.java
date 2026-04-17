package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("prescription_item")
public class PrescriptionItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private Long prescriptionId;
    private String itemType;  // MEDICINE 或 SERVICE
    private Long refItemId;   // 药品ID或服务ID
    private String itemName;
    private String specification;
    private String dosage;
    private String usageMethod;
    private String frequency;
    private Integer useDays;
    private BigDecimal unitPrice;
    private BigDecimal quantity;
    private BigDecimal lineAmount;
    private Integer sortNo;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}