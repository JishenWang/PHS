package com.pethospital.pet_hospital.controller;

import com.pethospital.pet_hospital.dto.common.PageQueryDto;
import com.pethospital.pet_hospital.dto.doctor.ConsultReplyDto;
import com.pethospital.pet_hospital.dto.doctor.DoctorStatusUpdateDto;
import com.pethospital.pet_hospital.dto.doctor.MedicalRecordCreateDto;
import com.pethospital.pet_hospital.dto.doctor.PrescriptionCreateDto;
import com.pethospital.pet_hospital.service.IDoctorService;
import com.pethospital.pet_hospital.vo.common.PageResultVo;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import com.pethospital.pet_hospital.vo.doctor.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/doctor")
public class DoctorController {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);
    private final IDoctorService doctorService;
    
    public DoctorController(IDoctorService doctorService) {
        this.doctorService = doctorService;
    }
    
    // ==================== 医生账号基础操作 ====================
    
    @GetMapping("/info")
    public ResultVo<DoctorInfoVo> getDoctorInfo(@RequestParam Long doctorId) {
        return ResultVo.success(doctorService.getDoctorInfo(doctorId));
    }
    
    @PutMapping("/info")
    public ResultVo<String> updateDoctorInfo(@RequestBody DoctorInfoVo doctorInfoVo) {
        doctorService.updateDoctorInfo(doctorInfoVo);
        return ResultVo.success("更新成功");
    }
    
    @PutMapping("/status")
    public ResultVo<String> updateDoctorStatus(@Valid @RequestBody DoctorStatusUpdateDto dto) {
        doctorService.updateDoctorStatus(dto);
        return ResultVo.success("状态更新成功");
    }
    
    @GetMapping("/authStatus")
    public ResultVo<Map<String, Object>> getAuthStatus(@RequestParam Long doctorId) {
        return ResultVo.success(doctorService.getAuthStatus(doctorId));
    }
    
    // ==================== 接诊管理 ====================
    
    @GetMapping("/accept/list")
    public ResultVo<PageResultVo<WaitAcceptRegisterVo>> getWaitAcceptList(@Valid PageQueryDto pageQuery) {
        return ResultVo.success(doctorService.getWaitAcceptList(pageQuery));
    }
    
    @GetMapping("/pet/detail")
    public ResultVo<Map<String, Object>> getPetDetail(@RequestParam Long petId) {
        return ResultVo.success(doctorService.getPetDetail(petId));
    }
    
    @PutMapping("/accept/status")
    public ResultVo<String> updateAcceptStatus(@RequestParam Long registerId, @RequestParam Integer status) {
        doctorService.updateAcceptStatus(registerId, status);
        return ResultVo.success("状态更新成功");
    }
    
    // ==================== 病历管理 ====================
    
    @PostMapping("/record")
    public ResultVo<MedicalRecordVo> createMedicalRecord(@Valid @RequestBody MedicalRecordCreateDto dto) {
        return ResultVo.success(doctorService.createMedicalRecord(dto));
    }
    
    @PutMapping("/record")
    public ResultVo<MedicalRecordVo> updateMedicalRecord(@RequestBody MedicalRecordVo recordVo) {
        return ResultVo.success(doctorService.updateMedicalRecord(recordVo));
    }
    
    @GetMapping("/record/list")
    public ResultVo<PageResultVo<MedicalRecordVo>> getMedicalRecordList(@Valid PageQueryDto pageQuery,
                                                                         @RequestParam(required = false) Long petId) {
        return ResultVo.success(doctorService.getMedicalRecordList(pageQuery, petId));
    }
    
    @GetMapping("/record/detail")
    public ResultVo<MedicalRecordVo> getMedicalRecordDetail(@RequestParam Long recordId) {
        return ResultVo.success(doctorService.getMedicalRecordDetail(recordId));
    }
    
    @GetMapping("/record/export")
    public void exportMedicalRecord(@RequestParam String recordIds, 
                                    @RequestParam String format,
                                    HttpServletResponse response) {
        doctorService.exportMedicalRecord(recordIds, format, response);
    }
    
    // ==================== 处方管理 ====================
    
    @GetMapping("/drug/list")
    public ResultVo<List<Map<String, Object>>> getDrugList(@RequestParam(required = false) String keyword) {
        return ResultVo.success(doctorService.getDrugList(keyword));
    }
    
    @GetMapping("/service/list")
    public ResultVo<List<Map<String, Object>>> getServiceList(@RequestParam(required = false) String keyword) {
        return ResultVo.success(doctorService.getServiceList(keyword));
    }
    
    @PostMapping("/prescription")
    public ResultVo<PrescriptionVo> createPrescription(@Valid @RequestBody PrescriptionCreateDto dto) {
        return ResultVo.success(doctorService.createPrescription(dto));
    }
    
    @PostMapping("/prescription/submit")
    public ResultVo<PrescriptionVo> submitPrescription(@RequestParam Long prescriptionId) {
        return ResultVo.success(doctorService.submitPrescription(prescriptionId));
    }
    
    @GetMapping("/prescription/list")
    public ResultVo<PageResultVo<PrescriptionVo>> getPrescriptionList(@Valid PageQueryDto pageQuery,
                                                                       @RequestParam(required = false) Long petId) {
        return ResultVo.success(doctorService.getPrescriptionList(pageQuery, petId));
    }
    
    @GetMapping("/prescription/detail")
    public ResultVo<PrescriptionVo> getPrescriptionDetail(@RequestParam Long prescriptionId) {
        return ResultVo.success(doctorService.getPrescriptionDetail(prescriptionId));
    }
    
    @GetMapping("/prescription/print")
    public void printPrescription(@RequestParam Long prescriptionId, HttpServletResponse response) {
        doctorService.printPrescription(prescriptionId, response);
    }
    
    // ==================== 在线咨询 ====================
    
    @GetMapping("/consult/unreadCount")
    public ResultVo<Integer> getUnreadConsultCount(@RequestParam Long doctorId) {
        return ResultVo.success(doctorService.getUnreadConsultCount(doctorId));
    }
    
    @GetMapping("/consult/list")
    public ResultVo<PageResultVo<ConsultVo>> getConsultList(@Valid PageQueryDto pageQuery,
                                                             @RequestParam(required = false) Integer replyStatus) {
        return ResultVo.success(doctorService.getConsultList(pageQuery, replyStatus));
    }
    
    @PostMapping("/consult/reply")
    public ResultVo<ConsultVo> replyConsult(@Valid @RequestBody ConsultReplyDto dto) {
        return ResultVo.success(doctorService.replyConsult(dto));
    }
    
    @GetMapping("/consult/detail")
    public ResultVo<ConsultVo> getConsultDetail(@RequestParam Long consultId) {
        return ResultVo.success(doctorService.getConsultDetail(consultId));
    }
    
    // ==================== 医生统计 ====================
    
    @GetMapping("/statistics")
    public ResultVo<DoctorStatisticsVo> getDoctorStatistics(@RequestParam Long doctorId,
                                                             @RequestParam(required = false) String startDate,
                                                             @RequestParam(required = false) String endDate) {
        return ResultVo.success(doctorService.getDoctorStatistics(doctorId, startDate, endDate));
    }
    
    @GetMapping("/statistics/accept")
    public ResultVo<Map<String, Object>> getAcceptStatistics(@RequestParam Long doctorId,
                                                              @RequestParam String statType) {
        return ResultVo.success(doctorService.getAcceptStatistics(doctorId, statType));
    }
}