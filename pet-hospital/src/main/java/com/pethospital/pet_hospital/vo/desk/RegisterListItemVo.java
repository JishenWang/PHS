package com.pethospital.pet_hospital.vo.desk;

import lombok.Data;

@Data
public class RegisterListItemVo {
    private String registerNo;
    private String customerName;
    private String petName;
    private String doctorName;
    private String status;
    private String visitTime;
}
