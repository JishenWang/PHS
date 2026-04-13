package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DoctorMapper {
    List<Doctor> listAll();
}
