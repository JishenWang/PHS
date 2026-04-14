package com.pethospital.pet_hospital.dto.owner;

import java.time.LocalDate;

import lombok.Data;

/**
 * 自填记录数据传输对象
 */
@Data
public class OwnerHealthRecordDto {

    private Long id;

    private Long petId;

    private String type;  // weight, deworming, daily, other

    private String title;

    private String content;

    private LocalDate recordDate;
}