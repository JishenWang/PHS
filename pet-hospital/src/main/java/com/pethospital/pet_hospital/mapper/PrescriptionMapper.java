// 处方+处方明细（医生开方）
package com.pethospital.pet_hospital.mapper;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.Prescription;

@Mapper
public interface PrescriptionMapper extends BaseMapper<Prescription> {
    Prescription getById(@Param("id") Long id);

    List<Prescription> listAllFull();

    // 按挂号单查询处方，供医生端/收费端读取
    List<Prescription> listByRegisterId(@Param("registerId") Long registerId);

    // 联表查询处方列表（支持宠物名称搜索）
    List<Map<String, Object>> listWithPetName(Map<String, Object> params);

    // 统计数量
    Long countWithPetName(Map<String, Object> params);
}
