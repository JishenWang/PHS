// 病历（医生诊疗）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.MedicalRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MedicalRecordMapper {
    MedicalRecord getById(@Param("id") Long id);

    List<MedicalRecord> listAll();
}
