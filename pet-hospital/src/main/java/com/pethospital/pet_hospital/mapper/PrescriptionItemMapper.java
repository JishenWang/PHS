package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.PrescriptionItem;

@Mapper
public interface PrescriptionItemMapper extends BaseMapper<PrescriptionItem> {
}