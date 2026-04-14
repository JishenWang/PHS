package com.pethospital.pet_hospital.vo.desk;

import lombok.Data;

@Data
public class CustomerListItemVo {
    private String customerId;
    private String customerName;
    private String phone;
    private String petName;
    private String petSpecies;
}
