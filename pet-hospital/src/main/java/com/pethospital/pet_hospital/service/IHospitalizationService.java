package com.pethospital.pet_hospital.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.pethospital.pet_hospital.entity.Hospitalization;
import com.pethospital.pet_hospital.entity.Prescription;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface IHospitalizationService extends IService<Hospitalization> {

    Hospitalization createHospitalization(Hospitalization hospitalization);

    Page<Hospitalization> pageQuery(Integer current, Integer size, Long ownerId, Long doctorId, Integer status);

    Hospitalization discharge(Long hospId, BigDecimal bedFeePerDay);

    Map<String, Object> interimCharge(Long hospId);

    List<Prescription> listPrescriptionsByHospId(Long hospId);
}
