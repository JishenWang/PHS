package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.User;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserMapper {
    List<User> listByNameOrPhone(String keyword);
}
