// sys_user（四端共用）
package com.pethospital.pet_hospital.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.pethospital.pet_hospital.entity.User;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    List<User> listByNameOrPhone(@Param("keyword") String keyword);
}
