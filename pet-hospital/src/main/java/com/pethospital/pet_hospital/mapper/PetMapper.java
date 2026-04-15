package com.pethospital.pet_hospital.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.Pet;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
    
    /**
     * 根据宠物 ID 查询宠物详情
     */
    Pet getById(@Param("id") Long id);

    /**
     * 查询全部宠物（后台管理用）
     */
    List<Pet> listAllFull();

    /**
     * 根据主人 ID 查询其名下宠物列表
     */
    List<Pet> listByOwnerId(@Param("ownerId") Long ownerId);
}