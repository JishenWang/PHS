package com.pethospital.pet_hospital.vo.owner;

import java.util.List;

public class HealthRecordVo {

    private Long id;
    private String type;
    private String typeName;
    private String petName;
    private String doctorName;
    private String title;
    private String content;
    private String prescription;
    private String createTime;
    private List<OrderItemVo> orderItems;  // 使用独立的 OrderItemVo
    private Double totalAmount;
    private String payStatus;
    private String payStatusText;

    // Getters and Setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getPrescription() { return prescription; }
    public void setPrescription(String prescription) { this.prescription = prescription; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public List<OrderItemVo> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemVo> orderItems) { this.orderItems = orderItems; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }

    public String getPayStatusText() { return payStatusText; }
    public void setPayStatusText(String payStatusText) { this.payStatusText = payStatusText; }
}