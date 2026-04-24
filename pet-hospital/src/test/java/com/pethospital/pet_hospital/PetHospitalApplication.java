package com.pethospital.pet_hospital;

import org.mybatis.spring.annotation.MapperScan;  // ← 改成这个
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pethospital.pet_hospital.mapper")
public class PetHospitalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetHospitalApplication.class, args);
    }
}