package com.pethospital.pet_hospital.vo.desk;

import lombok.Data;

@Data
public class ChargeListItemVo {
    private String orderNo;
    private String registerNo;
    private String customerName;
    private String petName;
    private String doctorName;
    private String payMethod;
    private String status;
    private Double total;
}
