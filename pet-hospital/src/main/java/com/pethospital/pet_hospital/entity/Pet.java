package com.pethospital.pet_hospital.entity;

import lombok.Data;

@Data
public class Pet {
    private Long id;
    private Long ownerId;
    private String name;
    private String species;
    private String gender;
    private Integer age;
    private String allergyHistory;
    private String healthRecords;
    private String createdAt;
    private String updatedAt;
}
