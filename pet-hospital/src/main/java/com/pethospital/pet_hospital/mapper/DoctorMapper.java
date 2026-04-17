// doctor_profile + dept（医生+科室）
package com.pethospital.pet_hospital.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.Doctor;


@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {
    Doctor getById(@Param("id") Long id);

    List<Doctor> listAllFull();

    List<Doctor> listAll();
}

