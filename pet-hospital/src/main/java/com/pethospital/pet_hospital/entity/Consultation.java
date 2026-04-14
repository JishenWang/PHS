// 提示词：全表覆盖；数据库字段名不得猜测，避免漏洞；字段严格来源于 pet_hospital.sql。
package com.pethospital.pet_hospital.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Consultation {
    // 数据库字段：id
    private Long id;
    // 数据库字段：consultation_no
    private String consultationNo;
    // 数据库字段：owner_id
    private Long ownerId;
    // 数据库字段：pet_id
    private Long petId;
    // 数据库字段：doctor_id
    private Long doctorId;
    // 数据库字段：title
    private String title;
    // 数据库字段：content
    private String content;
    // 数据库字段：images
    private String images;
    // 数据库字段：status
    private String status;
    // 数据库字段：rating
    private Integer rating;
    // 数据库字段：rating_comment
    private String ratingComment;
    // 数据库字段：create_time
    private LocalDateTime createTime;
    // 数据库字段：update_time
    private LocalDateTime updateTime;
}
