package com.pethospital.pet_hospital.entity;

import lombok.Data;

@Data
public class Doctor {
    private Long id;
    private String doctorNo;
    private String name;
    private String dept;
    /**
     * FREE/BUSY/REST
     */
    private String workStatus;
    private String createdAt;
    private String updatedAt;
}
