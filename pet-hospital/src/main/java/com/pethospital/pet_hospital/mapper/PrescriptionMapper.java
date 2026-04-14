// 处方+处方明细（医生开方）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Prescription;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PrescriptionMapper {
    Prescription getById(@Param("id") Long id);

    List<Prescription> listAllFull();

    // 按挂号单查询处方，供医生端/收费端读取
    List<Prescription> listByRegisterId(@Param("registerId") Long registerId);
}
