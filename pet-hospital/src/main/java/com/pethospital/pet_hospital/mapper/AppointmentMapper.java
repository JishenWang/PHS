// 预约（前台+客户）
package com.pethospital.pet_hospital.mapper;

import com.pethospital.pet_hospital.entity.Reserve;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface AppointmentMapper {
    Reserve getById(@Param("id") Long id);

    List<Reserve> listAll();

    List<Reserve> listByStatus(@Param("status") Integer status);

    // 预约列表聚合宠物/主人/医生信息
    List<Map<String, Object>> listAppointmentView();
}
