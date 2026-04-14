// sys_user（四端共用）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {
    User getById(@Param("id") Long id);

    List<User> listAllFull();

    List<User> listByNameOrPhone(String keyword);
}
