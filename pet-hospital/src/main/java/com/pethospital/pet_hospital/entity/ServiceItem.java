package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;

@Data
@TableName("service_item")  // ← 关键：添加这行！
public class ServiceItem {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String serviceCode;     // 对应 service_code
    private String serviceName;     // 对应 service_name
    private String category;
    private Long deptId;
    private BigDecimal price;
    private Integer durationMinutes;
    private String description;
    private Integer status;
    private Integer sortNo;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;
    private Integer isDeleted;
}