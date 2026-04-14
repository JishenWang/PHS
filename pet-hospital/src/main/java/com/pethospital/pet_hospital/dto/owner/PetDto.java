package com.pethospital.pet_hospital.dto.owner;

import java.time.LocalDate;

import lombok.Data;

/**
 * 宠物数据传输对象
 */
@Data
public class PetDto {

    private Long id;

    private String name;

    private String breed;

    private Integer gender;  // 1-公，2-母

    private LocalDate birthday;

    private Double weight;

    private String chipNumber;

    private Integer neutered;  // 0-未绝育，1-已绝育

    private String avatar;

    private String description;
}
