package com.pethospital.pet_hospital.dto.owner;

import java.time.LocalDate;

import lombok.Data;

/**
 * 健康记录数据传输对象
 */
@Data
public class HealthRecordDto {

    private Long id;

    private Long petId;

    private String type;  // vaccine, deworming, exam, treatment

    private String title;

    private String content;

    private LocalDate recordDate;

    private String hospital;
}
