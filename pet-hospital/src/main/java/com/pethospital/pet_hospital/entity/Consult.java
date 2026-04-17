package com.pethospital.pet_hospital.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("consult")
public class Consult {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String consultNo;
    
    private Long ownerUserId;
    
    private Long petId;
    
    private Long doctorId;
    
    private String title;
    
    private String content;
    
    private String images;
    
    private String replyContent;
    
    private String replyImages;
    
    private Integer replyStatus;
    
    private Integer rating;
    
    private String comment;
    
    private LocalDateTime consultTime;
    
    private LocalDateTime replyTime;
    
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer isDeleted;
}