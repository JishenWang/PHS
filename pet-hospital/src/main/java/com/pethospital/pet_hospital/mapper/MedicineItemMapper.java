package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.MedicineItem;

@Mapper
public interface MedicineItemMapper extends BaseMapper<MedicineItem> {
}