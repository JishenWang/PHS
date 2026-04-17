package com.pethospital.pet_hospital.service;

import java.util.List;
import java.util.Map;

import com.pethospital.pet_hospital.dto.common.PageQueryDto;
import com.pethospital.pet_hospital.dto.doctor.ConsultReplyDto;
import com.pethospital.pet_hospital.dto.doctor.DoctorStatusUpdateDto;
import com.pethospital.pet_hospital.dto.doctor.MedicalRecordCreateDto;
import com.pethospital.pet_hospital.dto.doctor.PrescriptionCreateDto;
import com.pethospital.pet_hospital.vo.common.PageResultVo;
import com.pethospital.pet_hospital.vo.doctor.ConsultVo;
import com.pethospital.pet_hospital.vo.doctor.DoctorInfoVo;
import com.pethospital.pet_hospital.vo.doctor.DoctorStatisticsVo;
import com.pethospital.pet_hospital.vo.doctor.MedicalRecordVo;
import com.pethospital.pet_hospital.vo.doctor.PrescriptionVo;
import com.pethospital.pet_hospital.vo.doctor.WaitAcceptRegisterVo;

import jakarta.servlet.http.HttpServletResponse;

/**
 * 医生端服务接口
 */
public interface IDoctorService {
    
    // ==================== 医生账号基础操作 ====================
    
    /**
     * 获取医生信息
     */
    DoctorInfoVo getDoctorInfo(Long doctorId);
    
    /**
     * 更新医生信息
     */
    void updateDoctorInfo(DoctorInfoVo doctorInfoVo);
    
    /**
     * 更新医生接诊状态
     */
    void updateDoctorStatus(DoctorStatusUpdateDto dto);
    
    /**
     * 获取医生认证状态
     */
    Map<String, Object> getAuthStatus(Long doctorId);
    
    // ==================== 接诊管理 ====================
    
    /**
     * 获取待接诊列表
     */
    PageResultVo<WaitAcceptRegisterVo> getWaitAcceptList(PageQueryDto pageQuery);
    
    /**
     * 获取宠物详情
     */
    Map<String, Object> getPetDetail(Long petId);
    
    /**
     * 更新接诊状态
     */
    void updateAcceptStatus(Long registerId, Integer status);
    
    // ==================== 病历管理 ====================
    
    /**
     * 创建病历
     */
    MedicalRecordVo createMedicalRecord(MedicalRecordCreateDto dto);
    
    /**
     * 更新病历
     */
    MedicalRecordVo updateMedicalRecord(MedicalRecordVo recordVo);
    
    /**
     * 获取病历列表
     */
    PageResultVo<MedicalRecordVo> getMedicalRecordList(PageQueryDto pageQuery, Long petId);
    
    /**
     * 获取病历详情
     */
    MedicalRecordVo getMedicalRecordDetail(Long recordId);
    
    /**
     * 导出处历
     */
    void exportMedicalRecord(String recordIds, String format, HttpServletResponse response);
    
    // ==================== 处方管理 ====================
    
    /**
     * 获取药品列表
     */
    List<Map<String, Object>> getDrugList(String keyword);
    
    /**
     * 获取服务项目列表
     */
    List<Map<String, Object>> getServiceList(String keyword);
    
    /**
     * 创建处方
     */
    PrescriptionVo createPrescription(PrescriptionCreateDto dto);
    
    /**
     * 提交处方
     */
    PrescriptionVo submitPrescription(Long prescriptionId);
    
    /**
     * 获取处方列表
     */
    PageResultVo<PrescriptionVo> getPrescriptionList(PageQueryDto pageQuery, Long petId);
    
    /**
     * 获取处方详情
     */
    PrescriptionVo getPrescriptionDetail(Long prescriptionId);
    
    /**
     * 打印处方
     */
    void printPrescription(Long prescriptionId, HttpServletResponse response);
    
    // ==================== 在线咨询 ====================
    
    /**
     * 获取未读咨询数量
     */
    Integer getUnreadConsultCount(Long doctorId);
    
    /**
     * 获取咨询列表
     */
    PageResultVo<ConsultVo> getConsultList(PageQueryDto pageQuery, Integer replyStatus);
    
    /**
     * 回复咨询
     */
    ConsultVo replyConsult(ConsultReplyDto dto);
    
    /**
     * 获取咨询详情
     */
    ConsultVo getConsultDetail(Long consultId);
    
    // ==================== 医生统计 ====================
    
    /**
     * 获取医生统计
     */
    DoctorStatisticsVo getDoctorStatistics(Long doctorId, String startDate, String endDate);
    
    /**
     * 获取接诊统计
     */
    Map<String, Object> getAcceptStatistics(Long doctorId, String statType);

    /**
     * 获取挂号单详情
     */
    Map<String, Object> getRegisterDetail(Long registerId);
}