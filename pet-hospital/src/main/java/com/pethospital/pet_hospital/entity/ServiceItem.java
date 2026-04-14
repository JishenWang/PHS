// 提示词：全字段映射必须严格来源于 pet_hospital.sql，字段名不得猜测。
package com.pethospital.pet_hospital.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ServiceItem {
    // 数据库字段：id
    private Long id;
    // 数据库字段：service_code
    private String serviceCode;
    // 数据库字段：service_name
    private String serviceName;
    // 数据库字段：category
    private String category;
    // 数据库字段：dept_id
    private Long deptId;
    // 数据库字段：price
    private BigDecimal price;
    // 数据库字段：duration_minutes
    private Integer durationMinutes;
    // 数据库字段：description
    private String description;
    // 数据库字段：status
    private Integer status;
    // 数据库字段：sort_no
    private Integer sortNo;
    // 数据库字段：create_time
    private LocalDateTime createTime;
    // 数据库字段：update_time
    private LocalDateTime updateTime;
    // 数据库字段：created_time
    private LocalDateTime createdTime;
    // 数据库字段：updated_time
    private LocalDateTime updatedTime;
    // 数据库字段：is_deleted
    private Integer isDeleted;
}
