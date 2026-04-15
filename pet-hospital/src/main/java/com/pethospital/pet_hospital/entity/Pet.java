package com.pethospital.pet_hospital.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

@TableName("pet")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    // 主人相关字段
    private Long ownerUserId;
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    private String ownerAddress;
    private Integer ownerIsTemp;

    // 宠物基础信息
    private String name;
    private String avatar;
    private String type;
    private String species;
    private String breed;
    private String gender;
    private Integer genderCode;
    private LocalDate birthday;
    private Integer age;
    private Double weight;
    private String color;
    private String chipNumber;
    private Integer neutered;

    // 健康/状态信息
    private String healthStatus;
    private Integer status;
    private String vaccines;
    private String allergies;
    private String allergy;
    private String allergyHistory;
    private String description;
    private String remark;
    private LocalDateTime lastVisit;

    // 时间字段
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime createdTime;
    private LocalDateTime updatedTime;


    private Integer isDeleted;

    @TableField(exist = false) 
    private String healthRecords;

    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getOwnerUserId() { return ownerUserId; }
    public void setOwnerUserId(Long ownerUserId) { this.ownerUserId = ownerUserId; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }

    public String getOwnerPhone() { return ownerPhone; }
    public void setOwnerPhone(String ownerPhone) { this.ownerPhone = ownerPhone; }

    public String getOwnerAddress() { return ownerAddress; }
    public void setOwnerAddress(String ownerAddress) { this.ownerAddress = ownerAddress; }

    public Integer getOwnerIsTemp() { return ownerIsTemp; }
    public void setOwnerIsTemp(Integer ownerIsTemp) { this.ownerIsTemp = ownerIsTemp; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public Integer getGenderCode() { return genderCode; }
    public void setGenderCode(Integer genderCode) { this.genderCode = genderCode; }

    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getChipNumber() { return chipNumber; }
    public void setChipNumber(String chipNumber) { this.chipNumber = chipNumber; }

    public Integer getNeutered() { return neutered; }
    public void setNeutered(Integer neutered) { this.neutered = neutered; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public String getVaccines() { return vaccines; }
    public void setVaccines(String vaccines) { this.vaccines = vaccines; }

    public String getAllergies() { return allergies; }
    public void setAllergies(String allergies) { this.allergies = allergies; }

    public String getAllergy() { return allergy; }
    public void setAllergy(String allergy) { this.allergy = allergy; }

    public String getAllergyHistory() { return allergyHistory; }
    public void setAllergyHistory(String allergyHistory) { this.allergyHistory = allergyHistory; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public LocalDateTime getLastVisit() { return lastVisit; }
    public void setLastVisit(LocalDateTime lastVisit) { this.lastVisit = lastVisit; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public LocalDateTime getCreatedTime() { return createdTime; }
    public void setCreatedTime(LocalDateTime createdTime) { this.createdTime = createdTime; }

    public LocalDateTime getUpdatedTime() { return updatedTime; }
    public void setUpdatedTime(LocalDateTime updatedTime) { this.updatedTime = updatedTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getHealthRecords() { return healthRecords; }
    public void setHealthRecords(String healthRecords) { this.healthRecords = healthRecords; }
}