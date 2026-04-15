package com.pethospital.pet_hospital;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.pethospital.pet_hospital.mapper")  // 确保这行存在
public class PetHospitalApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetHospitalApplication.class, args);
    }
}