package com.pethospital.pet_hospital.service.impl;

import com.pethospital.pet_hospital.dto.common.PageQueryDto;
import com.pethospital.pet_hospital.dto.doctor.ConsultReplyDto;
import com.pethospital.pet_hospital.dto.doctor.DoctorStatusUpdateDto;
import com.pethospital.pet_hospital.dto.doctor.MedicalRecordCreateDto;
import com.pethospital.pet_hospital.dto.doctor.PrescriptionCreateDto;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IDoctorService;
import com.pethospital.pet_hospital.vo.common.PageResultVo;
import com.pethospital.pet_hospital.vo.doctor.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class DoctorServiceImpl implements IDoctorService {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorServiceImpl.class);
    
    private final DoctorMapper doctorMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    
    public DoctorServiceImpl(DoctorMapper doctorMapper, PetMapper petMapper, UserMapper userMapper) {
        this.doctorMapper = doctorMapper;
        this.petMapper = petMapper;
        this.userMapper = userMapper;
    }
    
    // ==================== 医生账号基础操作 ====================
    
    @Override
    public DoctorInfoVo getDoctorInfo(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        DoctorInfoVo vo = new DoctorInfoVo();
        if (doctor != null) {
            vo.setDoctorId(doctor.getId());
            vo.setUserId(doctor.getUserId());
            vo.setTitle(doctor.getTitle());
            vo.setDepartment(doctor.getDepartment());
            vo.setSpecialty(doctor.getSpecialty());
            vo.setIntroduction(doctor.getIntroduction());
            vo.setStatus(doctor.getStatus());
            vo.setAuthStatus(doctor.getAuthStatus());
            vo.setAuthRemark(doctor.getAuthRemark());
            
            if (doctor.getUserId() != null) {
                User user = userMapper.selectById(doctor.getUserId());
                if (user != null) {
                    vo.setUsername(user.getUsername());
                    vo.setRealName(user.getRealName());
                    vo.setPhone(user.getPhone());
                    vo.setEmail(user.getEmail());
                }
            }
        }
        return vo;
    }
    
    @Override
    public void updateDoctorInfo(DoctorInfoVo doctorInfoVo) {
        Doctor doctor = new Doctor();
        doctor.setId(doctorInfoVo.getDoctorId());
        doctor.setTitle(doctorInfoVo.getTitle());
        doctor.setDepartment(doctorInfoVo.getDepartment());
        doctor.setSpecialty(doctorInfoVo.getSpecialty());
        doctor.setIntroduction(doctorInfoVo.getIntroduction());
        doctorMapper.updateById(doctor);
        log.info("更新医生信息成功, 医生ID: {}", doctorInfoVo.getDoctorId());
    }
    
    @Override
    public void updateDoctorStatus(DoctorStatusUpdateDto dto) {
        Doctor doctor = new Doctor();
        doctor.setId(dto.getDoctorId());
        doctor.setStatus(dto.getStatus());
        doctorMapper.updateById(doctor);
        log.info("更新医生状态成功, 医生ID: {}, 状态: {}", dto.getDoctorId(), dto.getStatus());
    }
    
    @Override
    public Map<String, Object> getAuthStatus(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        Map<String, Object> result = new HashMap<>();
        if (doctor != null) {
            result.put("authStatus", doctor.getAuthStatus());
            result.put("authRemark", doctor.getAuthRemark());
        } else {
            result.put("authStatus", 0);
            result.put("authRemark", "医生不存在");
        }
        return result;
    }
    
    // ==================== 接诊管理 ====================
    
    @Override
    public PageResultVo<WaitAcceptRegisterVo> getWaitAcceptList(PageQueryDto pageQuery) {
        // 返回空的分页结果（后续对接数据库）
        List<WaitAcceptRegisterVo> list = new ArrayList<>();
        return PageResultVo.success(list, 0L, 1L, 10L);
    }
    
    @Override
    public Map<String, Object> getPetDetail(Long petId) {
        Pet pet = petMapper.selectById(petId);
        Map<String, Object> result = new HashMap<>();
        if (pet != null) {
            result.put("petId", pet.getId());
            result.put("petName", pet.getName());
            result.put("type", pet.getType());
            result.put("breed", pet.getBreed());
            result.put("age", pet.getAge());
            result.put("gender", pet.getGender());
        }
        return result;
    }
    
    @Override
    public void updateAcceptStatus(Long registerId, Integer status) {
        log.info("更新接诊状态, 挂号ID: {}, 状态: {}", registerId, status);
        // TODO: 对接 RegisterMapper
    }
    
    // ==================== 病历管理 ====================
    
    @Override
    public MedicalRecordVo createMedicalRecord(MedicalRecordCreateDto dto) {
        MedicalRecordVo vo = new MedicalRecordVo();
        vo.setRecordId(System.currentTimeMillis());
        vo.setRegisterId(dto.getRegisterId());
        vo.setPetId(dto.getPetId());
        vo.setDoctorId(dto.getDoctorId());
        vo.setChiefComplaint(dto.getChiefComplaint());
        vo.setSymptoms(dto.getSymptoms());
        vo.setDiagnosis(dto.getDiagnosis());
        vo.setStatus(0);
        log.info("创建病历成功, 挂号ID: {}", dto.getRegisterId());
        return vo;
    }
    
    @Override
    public MedicalRecordVo updateMedicalRecord(MedicalRecordVo recordVo) {
        log.info("更新病历成功, 病历ID: {}", recordVo.getRecordId());
        return recordVo;
    }
    
    @Override
    public PageResultVo<MedicalRecordVo> getMedicalRecordList(PageQueryDto pageQuery, Long petId) {
        List<MedicalRecordVo> list = new ArrayList<>();
        return PageResultVo.success(list, 0L, (long) pageQuery.getPageNum(), (long) pageQuery.getPageSize());
    }
    
    @Override
    public MedicalRecordVo getMedicalRecordDetail(Long recordId) {
        MedicalRecordVo vo = new MedicalRecordVo();
        vo.setRecordId(recordId);
        return vo;
    }
    
    @Override
    public void exportMedicalRecord(String recordIds, String format, HttpServletResponse response) {
        log.info("导出病历，病历ID: {}, 格式: {}", recordIds, format);
    }
    
    // ==================== 处方管理 ====================
    
    @Override
    public List<Map<String, Object>> getDrugList(String keyword) {
        List<Map<String, Object>> drugs = new ArrayList<>();
        Map<String, Object> drug = new HashMap<>();
        drug.put("drugId", 1L);
        drug.put("drugName", "阿莫西林");
        drug.put("specification", "0.5g*20粒");
        drug.put("price", 25.00);
        drugs.add(drug);
        
        Map<String, Object> drug2 = new HashMap<>();
        drug2.put("drugId", 2L);
        drug2.put("drugName", "头孢克肟");
        drug2.put("specification", "0.1g*10粒");
        drug2.put("price", 35.00);
        drugs.add(drug2);
        return drugs;
    }
    
    @Override
    public List<Map<String, Object>> getServiceList(String keyword) {
        List<Map<String, Object>> services = new ArrayList<>();
        Map<String, Object> service = new HashMap<>();
        service.put("serviceId", 1L);
        service.put("serviceName", "基础体检");
        service.put("price", 100.00);
        services.add(service);
        
        Map<String, Object> service2 = new HashMap<>();
        service2.put("serviceId", 2L);
        service2.put("serviceName", "血液检查");
        service2.put("price", 200.00);
        services.add(service2);
        return services;
    }
    
    @Override
    public PrescriptionVo createPrescription(PrescriptionCreateDto dto) {
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(System.currentTimeMillis());
        vo.setRegisterId(dto.getRegisterId());
        vo.setPetId(dto.getPetId());
        vo.setDoctorId(dto.getDoctorId());
        vo.setTotalAmount(dto.getTotalAmount());
        vo.setStatus(0);
        vo.setStatusDesc("草稿");
        log.info("创建处方成功，挂号ID: {}", dto.getRegisterId());
        return vo;
    }
    
    @Override
    public PrescriptionVo submitPrescription(Long prescriptionId) {
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescriptionId);
        vo.setStatus(1);
        vo.setStatusDesc("已提交");
        log.info("提交处方成功，处方ID: {}", prescriptionId);
        return vo;
    }
    
    @Override
    public PageResultVo<PrescriptionVo> getPrescriptionList(PageQueryDto pageQuery, Long petId) {
        List<PrescriptionVo> list = new ArrayList<>();
        return PageResultVo.success(list, 0L, (long) pageQuery.getPageNum(), (long) pageQuery.getPageSize());
    }
    
    @Override
    public PrescriptionVo getPrescriptionDetail(Long prescriptionId) {
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescriptionId);
        return vo;
    }
    
    @Override
    public void printPrescription(Long prescriptionId, HttpServletResponse response) {
        log.info("打印处方，处方ID: {}", prescriptionId);
    }
    
    // ==================== 在线咨询 ====================
    
    @Override
    public Integer getUnreadConsultCount(Long doctorId) {
        return 0;
    }
    
    @Override
    public PageResultVo<ConsultVo> getConsultList(PageQueryDto pageQuery, Integer replyStatus) {
        List<ConsultVo> list = new ArrayList<>();
        return PageResultVo.success(list, 0L, (long) pageQuery.getPageNum(), (long) pageQuery.getPageSize());
    }
    
    @Override
    public ConsultVo replyConsult(ConsultReplyDto dto) {
        ConsultVo vo = new ConsultVo();
        vo.setConsultId(dto.getConsultId());
        vo.setReplyContent(dto.getReplyContent());
        vo.setReplyStatus(1);
        vo.setReplyStatusDesc("已回复");
        log.info("回复咨询成功，咨询ID: {}", dto.getConsultId());
        return vo;
    }
    
    @Override
    public ConsultVo getConsultDetail(Long consultId) {
        ConsultVo vo = new ConsultVo();
        vo.setConsultId(consultId);
        return vo;
    }
    
    // ==================== 医生统计 ====================
    
    @Override
    public DoctorStatisticsVo getDoctorStatistics(Long doctorId, String startDate, String endDate) {
        DoctorStatisticsVo vo = new DoctorStatisticsVo();
        vo.setTotalConsult(0);
        vo.setTotalAccept(0);
        vo.setTotalPrescription(0);
        vo.setAvgRating(5.0);
        vo.setDailyStats(new ArrayList<>());
        vo.setMonthlyStats(new ArrayList<>());
        vo.setYearlyStats(new ArrayList<>());
        return vo;
    }
    
    @Override
    public Map<String, Object> getAcceptStatistics(Long doctorId, String statType) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> data = new ArrayList<>();
        
        // 模拟图表数据
        Map<String, Object> item = new HashMap<>();
        item.put("date", "2026-04-01");
        item.put("count", 5);
        data.add(item);
        
        result.put("data", data);
        result.put("statType", statType);
        return result;
    }
}