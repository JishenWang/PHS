package com.pethospital.pet_hospital.dto.admin;

import lombok.Data;

@Data
public class UserQueryDto {
    private String keyword;
    private Integer page = 1;
    private Integer size = 10;
}