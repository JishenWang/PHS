package com.pethospital.pet_hospital.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.Hospitalization;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface HospitalizationMapper extends BaseMapper<Hospitalization> {
}
