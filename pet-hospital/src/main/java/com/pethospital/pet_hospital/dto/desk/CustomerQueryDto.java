package com.pethospital.pet_hospital.dto.desk;

import lombok.Data;

@Data
public class CustomerQueryDto {
    private String phone;
    private String name;
    private String petName;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
}
