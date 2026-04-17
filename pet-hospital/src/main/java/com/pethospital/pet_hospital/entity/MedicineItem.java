package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("medicine_item")
public class MedicineItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    @TableField(value = "medicine_code")
    private String medicineCode;
    
    @TableField(value = "medicine_name")
    private String medicineName;
    
    private String category;
    private String specification;
    private String unit;
    private BigDecimal price;
    
    @TableField(value = "stock_qty")
    private Integer stockQty;
    
    @TableField(value = "warning_stock_qty")
    private Integer warningStockQty;
    
    private String producer;
    
    @TableField(value = "allergy_risk")
    private String caution;
    
    @TableField(value = "usage_desc")
    private String instruction;
    
    private Integer status;
    
    @TableField(value = "create_time")
    private LocalDateTime createTime;
    
    @TableField(value = "update_time")
    private LocalDateTime updateTime;
    
    // 删除这两个字段！数据库中没有 created_time 和 updated_time
    // private LocalDateTime createdTime;
    // private LocalDateTime updatedTime;
    
    @TableField(value = "is_deleted")
    private Integer isDeleted;
}