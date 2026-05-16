package com.pethospital.pet_hospital.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("hospitalization")
public class Hospitalization {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String hospNo;
    private Long ownerId;
    private Long petId;
    private Long doctorId;
    private Long cageId;
    private LocalDateTime admissionTime;
    private Integer expectedDays;
    private Integer actualDays;
    private Integer status;
    private BigDecimal bedFeePerDay;
    private BigDecimal totalBedFee;
    private BigDecimal prescriptionAmount;
    private BigDecimal totalAmount;
    private Integer payStatus;
    private Long orderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;

    @TableField(exist = false)
    private String petName;
    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String ownerName;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getHospNo() { return hospNo; }
    public void setHospNo(String hospNo) { this.hospNo = hospNo; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Long getPetId() { return petId; }
    public void setPetId(Long petId) { this.petId = petId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getCageId() { return cageId; }
    public void setCageId(Long cageId) { this.cageId = cageId; }

    public LocalDateTime getAdmissionTime() { return admissionTime; }
    public void setAdmissionTime(LocalDateTime admissionTime) { this.admissionTime = admissionTime; }

    public Integer getExpectedDays() { return expectedDays; }
    public void setExpectedDays(Integer expectedDays) { this.expectedDays = expectedDays; }

    public Integer getActualDays() { return actualDays; }
    public void setActualDays(Integer actualDays) { this.actualDays = actualDays; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    public BigDecimal getBedFeePerDay() { return bedFeePerDay; }
    public void setBedFeePerDay(BigDecimal bedFeePerDay) { this.bedFeePerDay = bedFeePerDay; }

    public BigDecimal getTotalBedFee() { return totalBedFee; }
    public void setTotalBedFee(BigDecimal totalBedFee) { this.totalBedFee = totalBedFee; }

    public BigDecimal getPrescriptionAmount() { return prescriptionAmount; }
    public void setPrescriptionAmount(BigDecimal prescriptionAmount) { this.prescriptionAmount = prescriptionAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public Integer getPayStatus() { return payStatus; }
    public void setPayStatus(Integer payStatus) { this.payStatus = payStatus; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public LocalDateTime getCreateTime() { return createTime; }
    public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }

    public LocalDateTime getUpdateTime() { return updateTime; }
    public void setUpdateTime(LocalDateTime updateTime) { this.updateTime = updateTime; }

    public Integer getIsDeleted() { return isDeleted; }
    public void setIsDeleted(Integer isDeleted) { this.isDeleted = isDeleted; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getOwnerName() { return ownerName; }
    public void setOwnerName(String ownerName) { this.ownerName = ownerName; }
}
