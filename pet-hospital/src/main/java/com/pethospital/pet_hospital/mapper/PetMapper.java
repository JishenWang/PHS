// pet + 疫苗/驱虫/检查记录（聚合）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Pet;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PetMapper {
    Pet getById(@Param("id") Long id);

    List<Pet> listAllFull();

    List<Pet> listByOwnerId(Long ownerId);
}
