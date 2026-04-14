package com.pethospital.pet_hospital.dto.owner;

import lombok.Data;

/**
 * 咨询数据传输对象
 */
@Data
public class ConsultDto {

    private Long petId;

    private Long doctorId;

    private String title;

    private String content;

    private String images;
}
