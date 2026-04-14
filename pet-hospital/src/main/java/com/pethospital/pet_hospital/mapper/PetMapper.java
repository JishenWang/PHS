// pet + 疫苗/驱虫/检查记录（聚合）
package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.Pet;

import java.util.List;

@Mapper
public interface PetMapper extends BaseMapper<Pet> {
    /**
     * 根据宠物 ID 查询宠物详情（XML 内自定义 SQL）
     * @param id 宠物主键
     * @return 宠物实体
     */
    Pet getById(@Param("id") Long id);

    /**
     * 查询全部宠物（全字段，用于后台管理等场景）
     * @return 宠物列表
     */
    List<Pet> listAllFull();

    /**
     * 根据主人 ID 查询其名下宠物列表（前台 desk 端用）
     * 说明：XML 中同时兼容 owner_user_id 与 owner_id 两种字段
     * @param ownerId 主人 ID（sys_user.id）
     * @return 宠物列表
     */
    List<Pet> listByOwnerId(@Param("ownerId") Long ownerId);
}
