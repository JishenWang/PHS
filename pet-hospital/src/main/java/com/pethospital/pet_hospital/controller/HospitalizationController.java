package com.pethospital.pet_hospital.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.Hospitalization;
import com.pethospital.pet_hospital.entity.Prescription;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.service.IHospitalizationService;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hospitalization")
public class HospitalizationController {

    @Autowired
    private IHospitalizationService hospitalizationService;

    @Autowired
    private DoctorMapper doctorMapper;

    @PostMapping
    public ResultVo<Hospitalization> create(@RequestBody Hospitalization hospitalization) {
        Hospitalization result = hospitalizationService.createHospitalization(hospitalization);
        return ResultVo.success(result);
    }

    @GetMapping("/page")
    public ResultVo<Page<Hospitalization>> page(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long ownerId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer status) {
        Long resolvedDoctorId = resolveDoctorId(doctorId);
        Page<Hospitalization> result = hospitalizationService.pageQuery(current, size, ownerId, resolvedDoctorId, status);
        return ResultVo.success(result);
    }

    @GetMapping("/prescriptions/{id}")
    public ResultVo<List<Prescription>> listPrescriptions(@PathVariable Long id) {
        List<Prescription> result = hospitalizationService.listPrescriptionsByHospId(id);
        return ResultVo.success(result);
    }

    @PutMapping("/discharge/{id}")
    public ResultVo<Hospitalization> discharge(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal bedFeePerDay) {
        Hospitalization result = hospitalizationService.discharge(id, bedFeePerDay);
        return ResultVo.success(result);
    }

    @PostMapping("/charge/{id}")
    public ResultVo<Map<String, Object>> interimCharge(@PathVariable Long id) {
        Map<String, Object> result = hospitalizationService.interimCharge(id);
        return ResultVo.success(result);
    }

    @GetMapping("/{id}")
    public ResultVo<Hospitalization> getById(@PathVariable Long id) {
        Hospitalization hosp = hospitalizationService.getById(id);
        return ResultVo.success(hosp);
    }

    private Long resolveDoctorId(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor != null) {
            return doctorId;
        }
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getUserId, doctorId);
        List<Doctor> doctors = doctorMapper.selectList(wrapper);
        return !doctors.isEmpty() ? doctors.get(0).getId() : doctorId;
    }
}
