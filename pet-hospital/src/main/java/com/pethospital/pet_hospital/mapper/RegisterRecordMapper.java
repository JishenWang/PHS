package com.pethospital.pet_hospital.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RegisterRecordMapper {
    
    Map<String, Object> getById(@Param("id") Long id);
    
    List<Map<String, Object>> listAll();
    
    List<Map<String, Object>> listRegisterView();
    
    // 医生端接诊列表查询 - 将 status 参数类型从 String 改为 Integer
    List<Map<String, Object>> listDeskRegisters(@Param("status") Integer status,
                                                @Param("keyword") String keyword,
                                                @Param("doctorId") Long doctorId,
                                                @Param("offset") Integer offset,
                                                @Param("limit") Integer limit);
    
    Long countDeskRegisters(@Param("status") Integer status,
                            @Param("keyword") String keyword,
                            @Param("doctorId") Long doctorId);
    
    int updateStatusById(@Param("id") Long id,
                         @Param("status") Integer status);
}