package com.pethospital.pet_hospital.vo.owner;

public class OrderItemVo {

    private String itemName;
    private Integer quantity;
    private Double unitPrice;
    private Double amount;

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Double unitPrice) { this.unitPrice = unitPrice; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
}