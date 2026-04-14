package com.pethospital.pet_hospital.service;

import com.pethospital.pet_hospital.dto.desk.ChargeQueryDto;
import com.pethospital.pet_hospital.dto.desk.CustomerQueryDto;
import com.pethospital.pet_hospital.dto.desk.RegisterQueryDto;

import java.util.Map;

public interface IDeskService {

    /**
     * 前台端服务连通性检查
     */
    String ping();

    Map<String, Object> queryCustomers(CustomerQueryDto queryDto);

    Map<String, Object> queryRegisters(RegisterQueryDto queryDto);

    Map<String, Object> queryCharges(ChargeQueryDto queryDto);

    Map<String, Object> debugTableStats();

    Map<String, Object> updateRegisterStatus(Long id, String status);

    Map<String, Object> confirmCharge(Long id, String payMethod);

    Map<String, Object> createTempCustomer(Map<String, Object> payload);

    Map<String, Object> getReserveList(Map<String, Object> params);

    Map<String, Object> verifyReserve(Long reserveId);

    Map<String, Object> createRegister(Map<String, Object> payload);

    Map<String, Object> updateChargePricing(Long id, Map<String, Object> payload);

    Map<String, Object> applyRefund(Long id, String reason);

    Map<String, Object> getOrderList(Map<String, Object> params);

    Map<String, Object> getDoctorStatusList();
}
