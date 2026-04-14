package com.pethospital.pet_hospital.dto.desk;

import lombok.Data;

@Data
public class RegisterQueryDto {
    private String status;
    private String doctorId;
    private String keyword;
    private Integer page = 1;
    private Integer pageSize = 10;
}
