// 字典、配置等杂项
package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface CommonMapper {
    List<Map<String, Object>> listDept();

    List<Map<String, Object>> listServiceItem();

    List<Map<String, Object>> listMedicineItem();

    List<Map<String, Object>> listSysDict();

    List<Map<String, Object>> listSysConfig();
}
