// 挂号（前台核心）
package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface RegisterRecordMapper {
    Map<String, Object> getById(@Param("id") Long id);

    List<Map<String, Object>> listAll();

    List<Map<String, Object>> listRegisterView();

    List<Map<String, Object>> listDeskRegisters(@Param("status") String status,
                                                @Param("keyword") String keyword,
                                                @Param("doctorId") Long doctorId,
                                                @Param("offset") Integer offset,
                                                @Param("limit") Integer limit);

    Long countDeskRegisters(@Param("status") String status,
                            @Param("keyword") String keyword,
                            @Param("doctorId") Long doctorId);

    int updateStatusById(@Param("id") Long id,
                         @Param("status") Integer status);
}
