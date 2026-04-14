// doctor_profile + dept（医生+科室）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DoctorMapper {
    Doctor getById(@Param("id") Long id);

    List<Doctor> listAllFull();

    List<Doctor> listAll();
}
