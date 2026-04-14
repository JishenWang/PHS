package com.pethospital.pet_hospital.vo.owner;

import java.util.List;

public class OrderVo {

    private Long id;
    private String orderNo;
    private String petName;
    private Double totalAmount;
    private String payStatus;
    private String payStatusText;
    private String payMethod;
    private String payTime;
    private String createTime;
    private List<OrderItemVo> orderItems;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }

    public String getPetName() { return petName; }
    public void setPetName(String petName) { this.petName = petName; }

    public Double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(Double totalAmount) { this.totalAmount = totalAmount; }

    public String getPayStatus() { return payStatus; }
    public void setPayStatus(String payStatus) { this.payStatus = payStatus; }

    public String getPayStatusText() { return payStatusText; }
    public void setPayStatusText(String payStatusText) { this.payStatusText = payStatusText; }

    public String getPayMethod() { return payMethod; }
    public void setPayMethod(String payMethod) { this.payMethod = payMethod; }

    public String getPayTime() { return payTime; }
    public void setPayTime(String payTime) { this.payTime = payTime; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public List<OrderItemVo> getOrderItems() { return orderItems; }
    public void setOrderItems(List<OrderItemVo> orderItems) { this.orderItems = orderItems; }
}