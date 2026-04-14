// 订单+支付+退款（前台收费）
package com.pethospital.pet_hospital.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderInfoMapper {
    Map<String, Object> getById(@Param("id") Long id);

    List<Map<String, Object>> listAll();

    List<Map<String, Object>> listOrderBillingView();

    List<Map<String, Object>> listDeskCharges(@Param("status") String status,
                                              @Param("keyword") String keyword,
                                              @Param("offset") Integer offset,
                                              @Param("limit") Integer limit);

    Long countDeskCharges(@Param("status") String status,
                          @Param("keyword") String keyword);

    int confirmChargeById(@Param("id") Long id,
                          @Param("payMethod") String payMethod);
}
