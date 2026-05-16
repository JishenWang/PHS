package com.pethospital.pet_hospital.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pethospital.pet_hospital.dto.common.PageQueryDto;
import com.pethospital.pet_hospital.dto.doctor.ConsultReplyDto;
import com.pethospital.pet_hospital.dto.doctor.DoctorStatusUpdateDto;
import com.pethospital.pet_hospital.dto.doctor.MedicalRecordCreateDto;
import com.pethospital.pet_hospital.dto.doctor.PrescriptionCreateDto;
import com.pethospital.pet_hospital.service.IDoctorService;
import com.pethospital.pet_hospital.vo.common.PageResultVo;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import com.pethospital.pet_hospital.vo.doctor.ConsultVo;
import com.pethospital.pet_hospital.vo.doctor.DoctorInfoVo;
import com.pethospital.pet_hospital.vo.doctor.DoctorStatisticsVo;
import com.pethospital.pet_hospital.vo.doctor.MedicalRecordVo;
import com.pethospital.pet_hospital.vo.doctor.PrescriptionVo;
import com.pethospital.pet_hospital.vo.doctor.WaitAcceptRegisterVo;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.Feedback;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.FeedbackMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.utils.EncryptUtil;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/doctor")
public class DoctorController {
    
    private static final Logger log = LoggerFactory.getLogger(DoctorController.class);
    private final IDoctorService doctorService;
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final FeedbackMapper feedbackMapper;
    
    public DoctorController(IDoctorService doctorService, UserMapper userMapper, DoctorMapper doctorMapper, FeedbackMapper feedbackMapper) {
        this.doctorService = doctorService;
        this.userMapper = userMapper;
        this.doctorMapper = doctorMapper;
        this.feedbackMapper = feedbackMapper;
    }
    
    private Long getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = 
            org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not logged in");
        }
        String username = authentication.getName();
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.last("LIMIT 1");
        List<User> users = userMapper.selectList(wrapper);
        if (users.isEmpty()) {
            throw new RuntimeException("User does not exist");
        }
        return users.get(0).getId();
    }
    
    /**
     * 兼容处理：如果传入的 doctorId 实际上是 userId，则转换为真正的 doctorId
     */
    private Long resolveDoctorId(Long doctorId) {
        if (doctorId == null) {
            return null;
        }
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor != null) {
            return doctorId;
        }
        // 尝试按 userId 查找（使用 selectList + LIMIT 1 避免重复数据抛异常）
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getUserId, doctorId);
        wrapper.last("LIMIT 1");
        List<Doctor> doctors = doctorMapper.selectList(wrapper);
        return !doctors.isEmpty() ? doctors.get(0).getId() : doctorId;
    }
    
    // ==================== 个人中心接口 ====================
    
    @GetMapping("/profile/detail")
    public ResultVo<DoctorInfoVo> getDoctorProfile() {
        Long userId = getCurrentUserId();
        // 通过 userId 找到 doctorId（使用 selectList + LIMIT 1 避免重复数据抛异常）
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getUserId, userId);
        wrapper.last("LIMIT 1");
        List<Doctor> doctors = doctorMapper.selectList(wrapper);
        Long doctorId = !doctors.isEmpty() ? doctors.get(0).getId() : userId;
        DoctorInfoVo vo = doctorService.getDoctorInfo(doctorId);
        return ResultVo.success(vo);
    }
    
    @PutMapping("/profile/update")
    public ResultVo<String> updateDoctorProfile(@RequestBody DoctorInfoVo doctorInfoVo) {
        Long userId = getCurrentUserId();
        // 确保更新的是当前医生的信息
        if (doctorInfoVo.getDoctorId() == null) {
            doctorInfoVo.setDoctorId(userId);
        }
        doctorService.updateDoctorInfo(doctorInfoVo);
        return ResultVo.success("Updated successfully");
    }
    
    @PutMapping("/profile/change-password")
    public ResultVo<String> changeDoctorPassword(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        
        if (oldPassword == null || newPassword == null) {
            return ResultVo.error(400, "Incomplete parameters");
        }
        
        User user = userMapper.selectById(userId);
        if (user == null) {
            return ResultVo.error(404, "User does not exist");
        }
        
        if (!EncryptUtil.matchesPassword(oldPassword, user.getPassword())) {
            return ResultVo.error(400, "Incorrect original password");
        }
        
        user.setPassword(EncryptUtil.encodePassword(newPassword));
        userMapper.updateById(user);
        return ResultVo.success("Password changed successfully");
    }
    
    // ==================== 医生账号基础操作 ====================
    
    @GetMapping("/info")
    public ResultVo<DoctorInfoVo> getDoctorInfo(@RequestParam Long doctorId) {
        // 如果前端传的 doctorId 实际上是 userId，尝试查找真正的 doctorId
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, doctorId);
            wrapper.last("LIMIT 1");
            List<Doctor> doctors = doctorMapper.selectList(wrapper);
            if (!doctors.isEmpty()) {
                doctorId = doctors.get(0).getId();
            }
        }
        return ResultVo.success(doctorService.getDoctorInfo(doctorId));
    }
    
    @PutMapping("/info")
    public ResultVo<String> updateDoctorInfo(@RequestBody DoctorInfoVo doctorInfoVo) {
        doctorService.updateDoctorInfo(doctorInfoVo);
        return ResultVo.success("Updated successfully");
    }
    
    @PutMapping("/status")
    public ResultVo<String> updateDoctorStatus(@Valid @RequestBody DoctorStatusUpdateDto dto) {
        Long doctorId = dto.getDoctorId();
        
        // 如果前端传的 doctorId 实际上是 userId，尝试查找真正的 doctorId
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, doctorId);
            wrapper.last("LIMIT 1");
            List<Doctor> doctors = doctorMapper.selectList(wrapper);
            if (!doctors.isEmpty()) {
                doctorId = doctors.get(0).getId();
            }
        }
        
        if (doctorId == null) {
            return ResultVo.error(404, "Doctor profile does not exist");
        }
        
        DoctorStatusUpdateDto realDto = new DoctorStatusUpdateDto();
        realDto.setDoctorId(doctorId);
        realDto.setStatus(dto.getStatus());
        doctorService.updateDoctorStatus(realDto);
        return ResultVo.success("Status updated successfully");
    }
    
    @GetMapping("/authStatus")
    public ResultVo<Map<String, Object>> getAuthStatus(@RequestParam Long doctorId) {
        return ResultVo.success(doctorService.getAuthStatus(doctorId));
    }
    
    // ==================== 接诊管理 ====================
    
    /**
     * 获取待接诊数量（新增接口）
     */
    @GetMapping("/accept/count")
    public ResultVo<Integer> getWaitAcceptCount(@RequestParam Long doctorId) {
        log.info("获取待接诊数量, 医生ID: {}", doctorId);
        // 查询待接诊数量
        PageQueryDto query = new PageQueryDto();
        query.setPageNum(1);
        query.setPageSize(1);
        query.setStatus(0); // 待接诊状态
        
        PageResultVo<WaitAcceptRegisterVo> result = doctorService.getWaitAcceptList(query);
        int count = result.getTotal() != null ? result.getTotal().intValue() : 0;
        return ResultVo.success(count);
    }
    
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
        return ResultVo.success("Status updated successfully");
    }
    
    /**
     * 获取挂号单详情（用于病历创建页面）
     */
    @GetMapping("/accept/registerDetail")
    public ResultVo<Map<String, Object>> getRegisterDetail(@RequestParam Long registerId) {
        return ResultVo.success(doctorService.getRegisterDetail(registerId));
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
        return ResultVo.success(doctorService.getUnreadConsultCount(resolveDoctorId(doctorId)));
    }
    
    @GetMapping("/consult/list")
    public ResultVo<PageResultVo<ConsultVo>> getConsultList(@Valid PageQueryDto pageQuery,
                                                             @RequestParam(required = false) Integer replyStatus) {
        // 兼容：如果前端传的 doctorId 实际上是 userId，转换为真正的 doctorId
        if (pageQuery.getDoctorId() != null) {
            pageQuery.setDoctorId(resolveDoctorId(pageQuery.getDoctorId()));
        }
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
        // 兼容：如果传入的 doctorId 实际上是 userId，转换为真正的 doctor_profile.id
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, doctorId);
            wrapper.last("LIMIT 1");
            List<Doctor> doctors = doctorMapper.selectList(wrapper);
            if (!doctors.isEmpty()) {
                doctorId = doctors.get(0).getId();
            }
        }
        return ResultVo.success(doctorService.getDoctorStatistics(doctorId, startDate, endDate));
    }
    
    @GetMapping("/statistics/accept")
    public ResultVo<Map<String, Object>> getAcceptStatistics(@RequestParam Long doctorId,
                                                              @RequestParam String statType) {
        return ResultVo.success(doctorService.getAcceptStatistics(doctorId, statType));
    }
    
    // ==================== 系统设置 ====================
    
    @PostMapping("/feedback")
    public ResultVo<String> submitFeedback(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        User user = userMapper.selectById(userId);
        
        Feedback feedback = new Feedback();
        feedback.setUserId(userId);
        feedback.setUserName(user != null ? user.getRealName() : "");
        feedback.setType(body.get("type"));
        feedback.setContent(body.get("content"));
        feedback.setContact(body.get("contact"));
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);
        
        return ResultVo.success("Feedback submitted");
    }
    
    @GetMapping("/version")
    public ResultVo<Map<String, Object>> checkVersion() {
        Map<String, Object> data = Map.of(
            "version", "v1.0.0",
            "buildDate", "2026-04-12",
            "forceUpdate", false,
            "updateUrl", "",
            "updateLog", "Already up to date"
        );
        return ResultVo.success(data);
    }
}