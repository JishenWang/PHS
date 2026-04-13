package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Pet;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface PetMapper {
    List<Pet> listByOwnerId(Long ownerId);
}
