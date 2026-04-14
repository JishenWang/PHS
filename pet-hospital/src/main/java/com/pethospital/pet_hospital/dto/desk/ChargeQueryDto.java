package com.pethospital.pet_hospital.dto.desk;

import lombok.Data;

@Data
public class ChargeQueryDto {
    private String status;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
}
