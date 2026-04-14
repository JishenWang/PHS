// pet + 疫苗/驱虫/检查记录（聚合）
package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.Pet;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
}
