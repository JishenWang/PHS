package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper {
    List<Map<String, Object>> listDeskCharges(@Param("status") String status,
                                              @Param("keyword") String keyword,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

    Long countDeskCharges(@Param("status") String status,
                          @Param("keyword") String keyword);

    int confirmChargeById(@Param("id") Long id,
                          @Param("payMethod") String payMethod);
}
