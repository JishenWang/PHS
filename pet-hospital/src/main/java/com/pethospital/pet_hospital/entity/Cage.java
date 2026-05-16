package com.pethospital.pet_hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("cage")
public class Cage {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String cageNo;
    private String area;
    private Integer status;
    private Long currentPetId;
    @TableField(exist = false)
    private String currentPetName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCageNo() { return cageNo; }
    public void setCageNo(String cageNo) { this.cageNo = cageNo; }

    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public Long getCurrentPetId() { return currentPetId; }
    public void setCurrentPetId(Long currentPetId) { this.currentPetId = currentPetId; }

    public String getCurrentPetName() { return currentPetName; }
    public void setCurrentPetName(String currentPetName) { this.currentPetName = currentPetName; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }
}
