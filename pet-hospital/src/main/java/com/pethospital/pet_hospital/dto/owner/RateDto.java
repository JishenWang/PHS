package com.pethospital.pet_hospital.dto.owner;

import lombok.Data;

/**
 * 评价数据传输对象
 */
@Data
public class RateDto {

    private Integer rating;  // 1-5

    private String comment;
}
