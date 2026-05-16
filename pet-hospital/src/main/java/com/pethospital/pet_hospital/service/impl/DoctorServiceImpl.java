package com.pethospital.pet_hospital.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.dto.common.PageQueryDto;
import com.pethospital.pet_hospital.dto.doctor.ConsultReplyDto;
import com.pethospital.pet_hospital.dto.doctor.DoctorStatusUpdateDto;
import com.pethospital.pet_hospital.dto.doctor.MedicalRecordCreateDto;
import com.pethospital.pet_hospital.dto.doctor.PrescriptionCreateDto;
import com.pethospital.pet_hospital.entity.Consult;
import com.pethospital.pet_hospital.entity.Consultation;
import com.pethospital.pet_hospital.entity.ConsultationReply;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.MedicineItem;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.Prescription;
import com.pethospital.pet_hospital.entity.PrescriptionItem;
import com.pethospital.pet_hospital.entity.ServiceItem;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.ConsultMapper;
import com.pethospital.pet_hospital.mapper.ConsultationMapper;
import com.pethospital.pet_hospital.mapper.ConsultationReplyMapper;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.MedicalRecordMapper;
import com.pethospital.pet_hospital.mapper.MedicineItemMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.PrescriptionItemMapper;
import com.pethospital.pet_hospital.mapper.PrescriptionMapper;
import com.pethospital.pet_hospital.mapper.RegisterRecordMapper;
import com.pethospital.pet_hospital.mapper.ServiceItemMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IDoctorService;
import com.pethospital.pet_hospital.vo.common.PageResultVo;
import com.pethospital.pet_hospital.vo.doctor.ConsultVo;
import com.pethospital.pet_hospital.vo.doctor.DoctorInfoVo;
import com.pethospital.pet_hospital.vo.doctor.DoctorStatisticsVo;
import com.pethospital.pet_hospital.vo.doctor.MedicalRecordVo;
import com.pethospital.pet_hospital.vo.doctor.PrescriptionVo;
import com.pethospital.pet_hospital.vo.doctor.WaitAcceptRegisterVo;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class DoctorServiceImpl implements IDoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorServiceImpl.class);

    private final DoctorMapper doctorMapper;
    private final PetMapper petMapper;
    private final UserMapper userMapper;
    private final RegisterRecordMapper registerRecordMapper;
    private final MedicalRecordMapper medicalRecordMapper;  // 注入病历Mapper
    private final PrescriptionMapper prescriptionMapper;
    private final PrescriptionItemMapper prescriptionItemMapper;
    private final ConsultMapper consultMapper;
    private final ConsultationMapper consultationMapper;
    private final ConsultationReplyMapper consultationReplyMapper;
    private final MedicineItemMapper medicineItemMapper;
    private final ServiceItemMapper serviceItemMapper;
    private final JdbcTemplate jdbcTemplate;

    // 修改构造函数
    public DoctorServiceImpl(DoctorMapper doctorMapper, 
                            PetMapper petMapper, 
                            UserMapper userMapper,
                            RegisterRecordMapper registerRecordMapper,
                            MedicalRecordMapper medicalRecordMapper,
                            PrescriptionMapper prescriptionMapper,
                            PrescriptionItemMapper prescriptionItemMapper,
                            ConsultMapper consultMapper,
                            ConsultationMapper consultationMapper,
                            ConsultationReplyMapper consultationReplyMapper,
                            MedicineItemMapper medicineItemMapper,
                            ServiceItemMapper serviceItemMapper,
                            JdbcTemplate jdbcTemplate) {
        this.doctorMapper = doctorMapper;
        this.petMapper = petMapper;
        this.userMapper = userMapper;
        this.registerRecordMapper = registerRecordMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.prescriptionItemMapper = prescriptionItemMapper;
        this.consultMapper = consultMapper;
        this.consultationMapper = consultationMapper;
        this.consultationReplyMapper = consultationReplyMapper;
        this.medicineItemMapper = medicineItemMapper;
        this.serviceItemMapper = serviceItemMapper;
        this.jdbcTemplate = jdbcTemplate;
    }


    @PostConstruct
    public void initDoctorProfileColumn() {
        try {
            // 检查 doctor_profile 表是否存在 consult_visible 字段
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM doctor_profile LIKE 'consult_visible'"
            );
            if (columns.isEmpty()) {
                jdbcTemplate.execute(
                    "ALTER TABLE doctor_profile ADD COLUMN consult_visible TINYINT(1) DEFAULT 1 COMMENT '在线咨询可见：1-可见，0-不可见'"
                );
                log.info("成功为 doctor_profile 表添加 consult_visible 字段");
            }
        } catch (Exception e) {
            log.warn("检查或添加 consult_visible 字段失败: {}", e.getMessage());
        }
    }

    // ==================== 医生账号基础操作（保持不变）====================

    @Override
    public DoctorInfoVo getDoctorInfo(Long doctorId) {
        Doctor doctor = doctorMapper.selectById(doctorId);
        if (doctor == null) {
            return new DoctorInfoVo();
        }

        DoctorInfoVo vo = new DoctorInfoVo();
        vo.setDoctorId(doctor.getId());
        vo.setUserId(doctor.getUserId());
        vo.setTitle(doctor.getTitle());
        vo.setDepartment(doctor.getDepartment());
        vo.setSpecialty(doctor.getSpecialty());
        vo.setIntroduction(doctor.getIntroduction());
        vo.setStatus(doctor.getWorkStatus());
        vo.setAuthStatus(doctor.getAuthStatus());
        vo.setAuthRemark(doctor.getAuthRemark());
        vo.setConsultVisible(doctor.getConsultVisible());

        if (doctor.getUserId() != null) {
            User user = userMapper.selectById(doctor.getUserId());
            if (user != null) {
                vo.setUsername(user.getUsername());
                vo.setRealName(user.getRealName());
                vo.setPhone(user.getPhone());
                vo.setEmail(user.getEmail());
                vo.setAvatar(user.getAvatar() != null ? user.getAvatar() : user.getAvatarUrl());
            }
        }

        return vo;
    }

    @Override
    public void updateDoctorInfo(DoctorInfoVo doctorInfoVo) {
        if (doctorInfoVo == null || doctorInfoVo.getDoctorId() == null) {
            throw new RuntimeException("Doctor information cannot be empty");
        }

        Long doctorId = doctorInfoVo.getDoctorId();
        // 兼容：如果传入的 doctorId 实际上是 userId，转换为真正的 doctor_profile.id
        Doctor exist = doctorMapper.selectById(doctorId);
        if (exist == null) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, doctorId);
            wrapper.last("LIMIT 1");
            List<Doctor> doctors = doctorMapper.selectList(wrapper);
            if (!doctors.isEmpty()) {
                doctorId = doctors.get(0).getId();
            }
        }

        // 使用 UpdateWrapper 只更新非 null 字段，避免前端未返回的字段覆盖数据库原值
        LambdaUpdateWrapper<Doctor> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Doctor::getId, doctorId);
        boolean hasUpdate = false;
        if (doctorInfoVo.getTitle() != null) {
            updateWrapper.set(Doctor::getTitle, doctorInfoVo.getTitle());
            hasUpdate = true;
        }
        if (doctorInfoVo.getDepartment() != null) {
            updateWrapper.set(Doctor::getDepartment, doctorInfoVo.getDepartment());
            hasUpdate = true;
        }
        if (doctorInfoVo.getSpecialty() != null) {
            updateWrapper.set(Doctor::getSpecialty, doctorInfoVo.getSpecialty());
            hasUpdate = true;
        }
        if (doctorInfoVo.getIntroduction() != null) {
            updateWrapper.set(Doctor::getIntroduction, doctorInfoVo.getIntroduction());
            hasUpdate = true;
        }
        // 同步更新 doctor_profile.name（管理端展示的是这个字段）
        if (doctorInfoVo.getRealName() != null && !doctorInfoVo.getRealName().isEmpty()) {
            updateWrapper.set(Doctor::getName, doctorInfoVo.getRealName());
            hasUpdate = true;
        }
        if (doctorInfoVo.getConsultVisible() != null) {
            updateWrapper.set(Doctor::getConsultVisible, doctorInfoVo.getConsultVisible());
            hasUpdate = true;
        }
        if (hasUpdate) {
            updateWrapper.set(Doctor::getUpdateTime, LocalDateTime.now());
            int affected = doctorMapper.update(null, updateWrapper);
            if (affected == 0) {
                throw new RuntimeException("Doctor profile does not exist, update failed");
            }
        }
        
        // 同时更新 sys_user 表的 realName
        if (doctorInfoVo.getRealName() != null && !doctorInfoVo.getRealName().isEmpty()) {
            Doctor existDoctor = doctorMapper.selectById(doctorId);
            if (existDoctor != null && existDoctor.getUserId() != null) {
                User user = new User();
                user.setId(existDoctor.getUserId());
                user.setRealName(doctorInfoVo.getRealName());
                userMapper.updateById(user);
            }
        }
        
        log.info("更新医生信息成功, 医生ID: {}", doctorId);
    }

    @Override
    public void updateDoctorStatus(DoctorStatusUpdateDto dto) {
        Doctor doctor = new Doctor();
        doctor.setId(dto.getDoctorId());
        doctor.setWorkStatus(dto.getStatus());
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
            result.put("authRemark", "Doctor does not exist");
        }
        return result;
    }

    // ==================== 接诊管理（保持你之前的代码）====================

    @Override
    public PageResultVo<WaitAcceptRegisterVo> getWaitAcceptList(PageQueryDto pageQuery) {
        try {
            int offset = (pageQuery.getPageNum() - 1) * pageQuery.getPageSize();
            int limit = pageQuery.getPageSize();

            Long doctorId = null;

            // 关键修复：直接使用数字，不要转换成字符串
            Integer status = pageQuery.getStatus();  // 直接取 Integer，0,1,2,3

            log.info("调用 Mapper 参数: status={}, keyword={}, doctorId={}, offset={}, limit={}", 
                status, pageQuery.getKeyword(), doctorId, offset, limit);

            // 调用 Mapper - 注意这里传的是 Integer
            List<Map<String, Object>> registerList = registerRecordMapper.listDeskRegisters(
                status,  // 传 Integer，不是 String
                pageQuery.getKeyword(), 
                doctorId, 
                offset, 
                limit
            );

            Long total = registerRecordMapper.countDeskRegisters(
                status,  // 传 Integer，不是 String
                pageQuery.getKeyword(), 
                doctorId
            );

            log.info("查询结果: 列表{}条, 总计{}条", registerList.size(), total);

            List<WaitAcceptRegisterVo> voList = new ArrayList<>();
            for (Map<String, Object> record : registerList) {
                try {
                    WaitAcceptRegisterVo vo = convertToWaitAcceptRegisterVo(record);
                    voList.add(vo);
                } catch (Exception e) {
                    log.error("转换VO失败: {}", record, e);
                }
            }

            PageResultVo<WaitAcceptRegisterVo> result = new PageResultVo<>();
            result.setCode(200);
            result.setMsg("Query successful");
            result.setData(voList);
            result.setTotal(total);
            result.setCurrent((long) pageQuery.getPageNum());
            result.setSize((long) pageQuery.getPageSize());
            result.setPages((total + pageQuery.getPageSize() - 1) / pageQuery.getPageSize());

            return result;

        } catch (Exception e) {
            log.error("查询接诊列表失败", e);
            throw new RuntimeException("Query accept list failed: " + e.getMessage());
        }
    }

    private WaitAcceptRegisterVo convertToWaitAcceptRegisterVo(Map<String, Object> record) {
        WaitAcceptRegisterVo vo = new WaitAcceptRegisterVo();
        vo.setRegisterId(getLongValue(record, "id"));
        vo.setRegisterNo(getStringValue(record, "registerNo"));
        vo.setPetId(getLongValue(record, "petId"));
        vo.setPetName(getStringValue(record, "petName"));
        vo.setPetType(getStringValue(record, "petType"));
        vo.setBreed(getStringValue(record, "breed"));
        vo.setAge(getIntegerValue(record, "age"));
        vo.setGender(getStringValue(record, "gender"));
        vo.setOwnerId(getLongValue(record, "ownerId"));
        vo.setOwnerName(getStringValue(record, "ownerName"));
        vo.setOwnerPhone(getStringValue(record, "ownerPhone"));
        vo.setServiceType(getStringValue(record, "serviceType"));
        vo.setSymptom(getStringValue(record, "symptom"));
        vo.setAmount(getBigDecimalValue(record, "amount"));
        
        // 状态：SQL 直接返回数字 0/1/2/3，也兼容字符串
        Object rawStatus = record.get("status");
        Integer statusInt = 0;
        
        if (rawStatus != null) {
            if (rawStatus instanceof String) {
                String statusStr = (String) rawStatus;
                switch (statusStr) {
                    case "WAITING": statusInt = 0; break;
                    case "IN_PROGRESS": statusInt = 1; break;
                    case "DONE": statusInt = 2; break;
                    case "CANCELED": statusInt = 3; break;
                    default: 
                        try { statusInt = Integer.parseInt(statusStr); } catch (NumberFormatException e) { statusInt = 0; }
                }
            } else if (rawStatus instanceof Number) {
                statusInt = ((Number) rawStatus).intValue();
            }
        }
        
        vo.setStatus(statusInt);
        vo.setStatusDesc(getStatusDesc(statusInt));
        
        Object registerTime = record.get("registerTime");
        if (registerTime != null) {
            vo.setRegisterTime(parseDateTime(registerTime.toString()));
        }
        Object acceptTime = record.get("acceptTime");
        if (acceptTime != null) {
            vo.setAcceptTime(parseDateTime(acceptTime.toString()));
        }
        return vo;
    }

    // 添加这个辅助方法
    private String getStatusDesc(Integer status) {
        if (status == null) return "Unknown";
        switch (status) {
            case 0: return "Waiting";
            case 1: return "In Progress";
            case 2: return "Completed";
            case 3: return "Cancelled";
            default: return "Unknown";
        }
    }

    private Integer convertStatusToInt(String status) {
        if (status == null) return 0;
        switch (status) {
            case "WAITING": return 0;
            case "IN_PROGRESS": return 1;
            case "DONE": return 2;
            case "CANCELED": return 3;
            default: return 0;
        }
    }

    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer getIntegerValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    private BigDecimal getBigDecimalValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) return null;
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        try {
            return LocalDateTime.parse(dateTimeStr.replace(" ", "T"));
        } catch (Exception e) {
            try {
                return LocalDateTime.parse(dateTimeStr, 
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (Exception ex) {
                return null;
            }
        }
    }

    @Override
    public Map<String, Object> getPetDetail(Long petId) {
        Pet pet = petMapper.selectById(petId);
        if (pet == null) {
            log.warn("宠物不存在, petId: {}", petId);
            return new HashMap<>();
        }

        Map<String, Object> result = new HashMap<>();
        result.put("petId", pet.getId());
        result.put("name", pet.getName() != null ? pet.getName() : "Unnamed");

        String species = pet.getSpecies();
        String typeStr;
        if ("dog".equals(species) || "狗".equals(species)) {
            typeStr = "Dog";
        } else if ("cat".equals(species) || "猫".equals(species)) {
            typeStr = "Cat";
        } else if ("rabbit".equals(species) || "兔".equals(species)) {
            typeStr = "Rabbit";
        } else {
            typeStr = species != null ? species : "Unknown";
        }
        result.put("type", typeStr);

        result.put("breed", pet.getBreed() != null ? pet.getBreed() : "Unknown breed");
        Integer ageValue = pet.getAge();
        result.put("age", ageValue != null ? ageValue : Integer.valueOf(0));

// 关键修复：genderCode 直接用 Integer 接收
        Integer genderCode = pet.getGenderCode();  // ✅ 正确！直接获取 Integer
        if (genderCode == null) {
            String genderStr = pet.getGender();
            if ("male".equalsIgnoreCase(genderStr) || "公".equals(genderStr) || "1".equals(genderStr)) {
                genderCode = 1;
            } else if ("female".equalsIgnoreCase(genderStr) || "母".equals(genderStr) || "2".equals(genderStr)) {
                genderCode = 2;
            } else {
                genderCode = 1;
            }
        }
        
        if (genderCode == null) {
            String genderStr = pet.getGender();
            if ("male".equalsIgnoreCase(genderStr) || "公".equals(genderStr) || "1".equals(genderStr)) {
                genderCode = Integer.valueOf(1);
            } else if ("female".equalsIgnoreCase(genderStr) || "母".equals(genderStr) || "2".equals(genderStr)) {
                genderCode = Integer.valueOf(2);
            } else {
                genderCode = Integer.valueOf(1);
            }
        }
        result.put("gender", genderCode);

        if (pet.getBirthday() != null) {
            result.put("birthday", pet.getBirthday().toString());
        } else {
            result.put("birthday", "");
        }

        Double weightDouble = pet.getWeight();
        BigDecimal weight;
        if (weightDouble != null) {
            weight = BigDecimal.valueOf(weightDouble);
        } else {
            weight = BigDecimal.valueOf(0.0);
        }
        result.put("weight", weight);

        result.put("color", pet.getColor() != null ? pet.getColor() : "#D4A574");
        result.put("chipNumber", pet.getChipNumber() != null ? pet.getChipNumber() : "Not implanted");
        result.put("allergies", pet.getAllergies() != null ? pet.getAllergies() : "");
        result.put("allergy", pet.getAllergies() != null ? pet.getAllergies() : (pet.getAllergy() != null ? pet.getAllergy() : ""));
        result.put("healthStatus", pet.getHealthStatus() != null ? pet.getHealthStatus() : "healthy");
        result.put("description", pet.getDescription() != null ? pet.getDescription() : "");

        if (pet.getCreateTime() != null) {
            result.put("createTime", pet.getCreateTime().toString());
        } else {
            result.put("createTime", "");
        }

        String healthStatus = pet.getHealthStatus();
        result.put("status", "healthy".equals(healthStatus) ? Integer.valueOf(1) : Integer.valueOf(0));

        Integer neutered = pet.getNeutered();
        boolean neuteredBool = neutered != null && (neutered.intValue() == 1 || neutered.intValue() > 0);
        result.put("neutered", Boolean.valueOf(neuteredBool));

        String ownerName = "Unknown";
        String ownerPhone = "Not provided";
        String ownerAddress = pet.getOwnerAddress();

        if (pet.getOwnerUserId() != null) {
            User owner = userMapper.selectById(pet.getOwnerUserId());
            if (owner != null) {
                ownerName = owner.getRealName() != null ? owner.getRealName() : owner.getUsername();
                ownerPhone = owner.getPhone() != null ? owner.getPhone() : "Not provided";
            }
        }
        if ("Unknown".equals(ownerName) && pet.getOwnerName() != null) {
            ownerName = pet.getOwnerName();
        }
        if ("Not provided".equals(ownerPhone) && pet.getOwnerPhone() != null) {
            ownerPhone = pet.getOwnerPhone();
        }
        if (ownerAddress == null) {
            ownerAddress = "Address not provided";
        }

        result.put("ownerName", ownerName);
        result.put("ownerPhone", ownerPhone);
        result.put("ownerAddress", ownerAddress);
        result.put("vaccineStatus", "Rabies vaccine vaccinated");

        // 上次就诊时间（优先 lastVisit，无则实时查 register_record）
        if (pet.getLastVisit() != null) {
            result.put("lastCheckup", pet.getLastVisit().toString());
        } else {
            try {
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                        "SELECT MAX(register_time) as last_visit FROM register_record WHERE pet_id = ? AND is_deleted = 0",
                        petId);
                if (!rows.isEmpty() && rows.get(0).get("last_visit") != null) {
                    result.put("lastCheckup", rows.get(0).get("last_visit").toString());
                } else {
                    result.put("lastCheckup", "");
                }
            } catch (Exception e) {
                result.put("lastCheckup", "");
            }
        }

        log.info("宠物详情查询成功, petId: {}, name: {}", petId, pet.getName());
        return result;
    }

    @Override
    public void updateAcceptStatus(Long registerId, Integer status) {
        log.info("更新接诊状态, 挂号ID: {}, 状态: {}", registerId, status);
        int result = registerRecordMapper.updateStatusById(registerId, status);
        if (result > 0) {
            log.info("更新接诊状态成功, 挂号ID: {}", registerId);
        } else {
            log.warn("更新接诊状态失败, 挂号ID: {}", registerId);
            throw new RuntimeException("Update accept status failed, register record does not exist or has been deleted");
        }
    }

    // ==================== 病历管理（关键修复：真正查询数据库）====================

    @Override
    public MedicalRecordVo createMedicalRecord(MedicalRecordCreateDto dto) {
        try {
            // 0. 确保 doctorId 有效（自动创建缺失的医生档案）
            Long validDoctorId = ensureDoctorProfile(dto.getDoctorId(), dto.getDoctorName());
            
            // 0.1 通过宠物ID查询主人ID
            Long ownerId = null;
            Pet pet = petMapper.selectById(dto.getPetId());
            if (pet != null) {
                ownerId = pet.getOwnerUserId() != null ? pet.getOwnerUserId() : pet.getOwnerId();
            }
            
            // 1. 创建实体对象
            MedicalRecord record = new MedicalRecord();
            record.setRegisterId(dto.getRegisterId());
            record.setHospitalizationId(dto.getHospitalizationId());
            record.setPetId(dto.getPetId());
            record.setOwnerId(ownerId);
            record.setDoctorId(validDoctorId);
            record.setDoctorName(dto.getDoctorName());
            record.setChiefComplaint(dto.getChiefComplaint());
            record.setSymptoms(dto.getSymptoms());
            record.setPresentIllness(dto.getPresentIllness());
            record.setPastHistory(dto.getPastHistory());
            record.setPhysicalExam(dto.getPhysicalExam());
            record.setAuxiliaryExam(dto.getAuxiliaryExam());
            record.setDiagnosis(dto.getDiagnosis());
            record.setTreatmentPlan(dto.getTreatmentPlan());
            record.setDoctorAdvice(dto.getDoctorAdvice());
            record.setRemark(dto.getRemark());
            record.setStatus(0); // 草稿状态
            record.setIsDeleted(0);
            
            // 生成病历编号
            record.setRecordNo("MR" + System.currentTimeMillis());
            
            // 2. 插入数据库
            medicalRecordMapper.insert(record);
            
            log.info("创建病历成功, 病历ID: {}, 挂号ID: {}", record.getId(), dto.getRegisterId());
            
            // 3. 转换为VO返回
            return convertToMedicalRecordVo(record);
        } catch (Exception e) {
            log.error("创建病历失败", e);
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new RuntimeException("Create medical record failed: " + msg);
        }
    }

    @Override
    public MedicalRecordVo updateMedicalRecord(MedicalRecordVo recordVo) {
        if (recordVo == null || recordVo.getRecordId() == null) {
            throw new RuntimeException("Medical record information cannot be empty");
        }
        
        MedicalRecord record = medicalRecordMapper.selectById(recordVo.getRecordId());
        if (record == null) {
            throw new RuntimeException("Medical record does not exist");
        }
        
        // 自动修复：如果 ownerId 为空，通过 petId 查询补全
        if (record.getOwnerId() == null && record.getPetId() != null) {
            Pet pet = petMapper.selectById(record.getPetId());
            if (pet != null) {
                Long ownerId = pet.getOwnerUserId() != null ? pet.getOwnerUserId() : pet.getOwnerId();
                record.setOwnerId(ownerId);
                log.info("自动修复病历 ownerId, 病历ID: {}, petId: {}, ownerId: {}", 
                    record.getId(), record.getPetId(), ownerId);
            }
        }
        
        // 只更新非空字段
        if (recordVo.getChiefComplaint() != null) {
            record.setChiefComplaint(recordVo.getChiefComplaint());
        }
        if (recordVo.getSymptoms() != null) {
            record.setSymptoms(recordVo.getSymptoms());
        }
        if (recordVo.getPresentIllness() != null) {
            record.setPresentIllness(recordVo.getPresentIllness());
        }
        if (recordVo.getPastHistory() != null) {
            record.setPastHistory(recordVo.getPastHistory());
        }
        if (recordVo.getPhysicalExam() != null) {
            record.setPhysicalExam(recordVo.getPhysicalExam());
        }
        if (recordVo.getAuxiliaryExam() != null) {
            record.setAuxiliaryExam(recordVo.getAuxiliaryExam());
        }
        if (recordVo.getDiagnosis() != null) {
            record.setDiagnosis(recordVo.getDiagnosis());
        }
        if (recordVo.getTreatmentPlan() != null) {
            record.setTreatmentPlan(recordVo.getTreatmentPlan());
        }
        if (recordVo.getDoctorAdvice() != null) {
            record.setDoctorAdvice(recordVo.getDoctorAdvice());
        }
        if (recordVo.getRemark() != null) {
            record.setRemark(recordVo.getRemark());
        }
        if (recordVo.getStatus() != null) {
            record.setStatus(recordVo.getStatus());
        }
        
        record.setUpdateTime(LocalDateTime.now());
        medicalRecordMapper.updateById(record);
        
        log.info("更新病历成功, 病历ID: {}", recordVo.getRecordId());
        return convertToMedicalRecordVo(record);
    }

    @Override
    public PageResultVo<MedicalRecordVo> getMedicalRecordList(PageQueryDto pageQuery, Long petId) {
        try {
            log.info("查询病历列表, pageNum={}, pageSize={}, petId={}, doctorId={}, keyword={}, startDate={}, endDate={}", 
                pageQuery.getPageNum(), pageQuery.getPageSize(), petId, 
                pageQuery.getDoctorId(), pageQuery.getKeyword(), 
                pageQuery.getStartDate(), pageQuery.getEndDate());
            
            // 1. 构建查询条件
            LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MedicalRecord::getIsDeleted, 0);
            
            // 按宠物ID筛选
            if (petId != null && petId > 0) {
                wrapper.eq(MedicalRecord::getPetId, petId);
            }
            
            // 按医生ID筛选
            if (pageQuery.getDoctorId() != null && pageQuery.getDoctorId() > 0) {
                wrapper.eq(MedicalRecord::getDoctorId, pageQuery.getDoctorId());
            }
            
            // 按状态筛选
            if (pageQuery.getStatus() != null) {
                wrapper.eq(MedicalRecord::getStatus, pageQuery.getStatus());
            }
            
            // 按关键词搜索（病历编号、诊断结果、或宠物名称）
            String keyword = pageQuery.getKeyword();
            if (keyword != null && !keyword.isEmpty()) {
                // 先查询匹配宠物名称的宠物ID列表
                LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
                petWrapper.select(Pet::getId);
                petWrapper.like(Pet::getName, keyword);
                petWrapper.eq(Pet::getIsDeleted, 0);
                List<Pet> matchedPets = petMapper.selectList(petWrapper);
                
                if (matchedPets != null && !matchedPets.isEmpty()) {
                    List<Long> matchedPetIds = matchedPets.stream()
                        .map(Pet::getId)
                        .collect(Collectors.toList());
                    log.info("通过宠物名称'{}'匹配到宠物ID: {}", keyword, matchedPetIds);
                    
                    // 按宠物ID 或 病历编号 或 诊断结果 搜索
                    wrapper.and(w -> w
                        .in(MedicalRecord::getPetId, matchedPetIds)
                        .or()
                        .like(MedicalRecord::getRecordNo, keyword)
                        .or()
                        .like(MedicalRecord::getDiagnosis, keyword)
                    );
                } else {
                    // 没有匹配到宠物，只按病历编号或诊断结果搜索
                    wrapper.and(w -> w
                        .like(MedicalRecord::getRecordNo, keyword)
                        .or()
                        .like(MedicalRecord::getDiagnosis, keyword)
                    );
                }
            }
            
            // 按时间范围筛选 - 修复：LocalDate 类型用 != null 判断，不能用 isEmpty()
            if (pageQuery.getStartDate() != null) {
                wrapper.ge(MedicalRecord::getCreateTime, pageQuery.getStartDate() + " 00:00:00");
            }
            if (pageQuery.getEndDate() != null) {
                wrapper.le(MedicalRecord::getCreateTime, pageQuery.getEndDate() + " 23:59:59");
            }
            
            // 按创建时间倒序
            wrapper.orderByDesc(MedicalRecord::getCreateTime);
            
            // 2. 执行分页查询
            Page<MedicalRecord> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
            Page<MedicalRecord> recordPage = medicalRecordMapper.selectPage(page, wrapper);
            
            log.info("查询到病历记录数: {}", recordPage.getRecords().size());
            
            // 3. 转换为VO列表
            List<MedicalRecordVo> voList = new ArrayList<>();
            for (MedicalRecord record : recordPage.getRecords()) {
                MedicalRecordVo vo = convertToMedicalRecordVo(record);
                
                // 补充宠物信息
                Pet pet = petMapper.selectById(record.getPetId());
                if (pet != null) {
                    vo.setPetName(pet.getName());
                    vo.setPetType(pet.getSpecies());
                }
                
                voList.add(vo);
            }
            
            // 4. 构建返回结果
            PageResultVo<MedicalRecordVo> result = new PageResultVo<>();
            result.setCode(200);
            result.setMsg("Query successful");
            result.setData(voList);
            result.setTotal(recordPage.getTotal());
            result.setCurrent(recordPage.getCurrent());
            result.setSize(recordPage.getSize());
            result.setPages(recordPage.getPages());
            
            log.info("查询病历列表成功, 共{}条记录", voList.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询病历列表失败", e);
            throw new RuntimeException("Query medical record list failed: " + e.getMessage());
        }
    }


    @Override
    public MedicalRecordVo getMedicalRecordDetail(Long recordId) {
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("Medical record does not exist");
        }
        return convertToMedicalRecordVo(record);
    }

    @Override
    public void exportMedicalRecord(String recordIds, String format, HttpServletResponse response) {
        log.info("导出病历，病历ID: {}, 格式: {}", recordIds, format);
        // TODO: 实现导出逻辑
    }
    
    /**
     * 确保医生档案存在，返回有效的 doctor_profile.id
     * 注意：doctorId 是 sys_user.id，需要匹配 doctor_profile.user_id
     */
    private Long ensureDoctorProfile(Long doctorId, String doctorName) {
        if (doctorId != null && doctorId > 0) {
            // 1. 先通过 user_id 查找已有的 doctor_profile
            try {
                List<Long> ids = jdbcTemplate.queryForList(
                    "SELECT id FROM doctor_profile WHERE user_id = ? AND is_deleted = 0 LIMIT 1",
                    Long.class, doctorId
                );
                if (!ids.isEmpty()) {
                    return ids.get(0);
                }
            } catch (Exception e) {
                log.warn("通过user_id检查医生档案失败, doctorId={}", doctorId);
            }
            
            // 2. 再检查 doctor_profile.id 本身（兼容旧数据）
            try {
                Integer count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM doctor_profile WHERE id = ? AND is_deleted = 0",
                    Integer.class, doctorId
                );
                if (count != null && count > 0) {
                    return doctorId;
                }
            } catch (Exception e) {
                log.warn("通过id检查医生档案失败, doctorId={}", doctorId);
            }
        }
        // 3. 不存在则自动创建
        try {
            // 从 sys_user 查询医生真实姓名
            String realName = doctorName;
            if (realName == null || realName.isEmpty()) {
                try {
                    List<String> names = jdbcTemplate.queryForList(
                        "SELECT real_name FROM sys_user WHERE id = ? LIMIT 1",
                        String.class, doctorId
                    );
                    if (!names.isEmpty() && names.get(0) != null && !names.get(0).isEmpty()) {
                        realName = names.get(0);
                    }
                } catch (Exception e) {
                    log.warn("查询用户姓名失败, doctorId={}", doctorId);
                }
            }
            if (realName == null || realName.isEmpty()) {
                realName = "Doctor " + System.currentTimeMillis();
            }
            jdbcTemplate.update(
                "INSERT INTO doctor_profile(user_id, name, department, title, work_status, status, is_deleted, create_time, update_time) " +
                "VALUES(?, ?, 'General Medicine Department', 'Attending Physician', 1, 1, 0, NOW(), NOW())",
                doctorId != null ? doctorId : 0, realName
            );
            Long newId = jdbcTemplate.queryForObject(
                "SELECT id FROM doctor_profile WHERE user_id = ? AND is_deleted = 0 ORDER BY id DESC LIMIT 1",
                Long.class, doctorId != null ? doctorId : 0
            );
            log.info("自动创建医生档案, user_id={}, newDoctorId={}", doctorId, newId);
            return newId != null ? newId : doctorId;
        } catch (Exception e) {
            log.error("自动创建医生档案失败", e);
            return doctorId;
        }
    }
    
    /**
     * 将MedicalRecord实体转换为MedicalRecordVo
     */
    private MedicalRecordVo convertToMedicalRecordVo(MedicalRecord record) {
        MedicalRecordVo vo = new MedicalRecordVo();
        vo.setRecordId(record.getId());
        vo.setRecordNo(record.getRecordNo());
        vo.setRegisterId(record.getRegisterId());
        vo.setPetId(record.getPetId());
        vo.setDoctorId(record.getDoctorId());
        vo.setDoctorName(record.getDoctorName());
        vo.setChiefComplaint(record.getChiefComplaint());
        vo.setSymptoms(record.getSymptoms());
        vo.setPresentIllness(record.getPresentIllness());
        vo.setPastHistory(record.getPastHistory());
        vo.setPhysicalExam(record.getPhysicalExam());
        vo.setAuxiliaryExam(record.getAuxiliaryExam());
        vo.setDiagnosis(record.getDiagnosis());
        vo.setTreatmentPlan(record.getTreatmentPlan());
        vo.setDoctorAdvice(record.getDoctorAdvice());
        vo.setRemark(record.getRemark());
        vo.setStatus(record.getStatus());
        vo.setCreateTime(record.getCreateTime());
        vo.setUpdateTime(record.getUpdateTime());

        // 补充查询宠物名称和类型
        if (record.getPetId() != null) {
            Pet pet = petMapper.selectById(record.getPetId());
            if (pet != null) {
                vo.setPetName(pet.getName());
                vo.setPetType(pet.getType());
            }
        }

        // 补充查询医生职称
        if (record.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(record.getDoctorId());
            if (doctor != null) {
                vo.setDoctorTitle(doctor.getTitle());
                if (vo.getDoctorName() == null || vo.getDoctorName().isEmpty()) {
                    vo.setDoctorName(doctor.getName());
                }
            }
        }

        return vo;
    }

    // ==================== 处方管理（保持简化实现）====================

    // 修改 getDrugList 方法 - 从数据库查询真实药品
    @Override
    public List<Map<String, Object>> getDrugList(String keyword) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            log.info("开始查询药品列表, keyword={}", keyword);
            
            // 构建查询条件
            LambdaQueryWrapper<MedicineItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MedicineItem::getStatus, 1);
            wrapper.eq(MedicineItem::getIsDeleted, 0);
            // 过滤掉过期药品（expiry_date 不为 null 且小于今天）
            wrapper.and(w -> w.isNull(MedicineItem::getExpiryDate)
                              .or()
                              .ge(MedicineItem::getExpiryDate, LocalDate.now()));
            
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like(MedicineItem::getMedicineName, keyword)
                                .or()
                                .like(MedicineItem::getMedicineCode, keyword));
            }
            
            List<MedicineItem> drugs = medicineItemMapper.selectList(wrapper);
            log.info("从数据库查询到药品数量: {}", drugs.size());
            
            for (MedicineItem drug : drugs) {
                Map<String, Object> item = new HashMap<>();
                item.put("drugId", drug.getId());
                item.put("drugName", drug.getMedicineName());
                item.put("specification", drug.getSpecification() != null ? drug.getSpecification() : "");
                item.put("unit", drug.getUnit() != null ? drug.getUnit() : "");
                item.put("category", drug.getCategory() != null ? drug.getCategory() : "");
                item.put("price", drug.getPrice() != null ? drug.getPrice() : BigDecimal.ZERO);
                item.put("stockQty", drug.getStockQty() != null ? drug.getStockQty() : 0);
                item.put("warningStockQty", drug.getWarningStockQty() != null ? drug.getWarningStockQty() : 0);
                item.put("producer", drug.getProducer() != null ? drug.getProducer() : "");
                item.put("caution", drug.getCaution() != null ? drug.getCaution() : "");
                item.put("instruction", drug.getInstruction() != null ? drug.getInstruction() : "");
                item.put("expiryDate", drug.getExpiryDate() != null ? drug.getExpiryDate().toString() : "");
                result.add(item);
            }
            
            log.info("返回药品数量: {}", result.size());
        } catch (Exception e) {
            log.error("查询药品列表失败", e);
        }
        
        return result;
    }

    // 修改 getServiceList 方法 - 从数据库查询真实服务
    @Override
    public PrescriptionVo createPrescription(PrescriptionCreateDto dto) {
        log.info("开始创建处方, 挂号ID: {}, 宠物ID: {}, 医生ID: {}, 药品数量: {}, 服务数量: {}", 
            dto.getRegisterId(), dto.getPetId(), dto.getDoctorId(),
            dto.getDrugs() != null ? dto.getDrugs().size() : 0,
            dto.getServices() != null ? dto.getServices().size() : 0);
        
        try {
            // 0. 确保 doctorId 有效
            Long validDoctorId = ensureDoctorProfile(dto.getDoctorId(), null);
            
            // 1. 生成处方编号
            String prescriptionNo = "PR" + System.currentTimeMillis();
            
            // 2. 创建处方实体
            // 自动查找 recordId：如果前端没传，通过 registerId 查询对应的病历
            Long recordId = dto.getRecordId();
            if (recordId == null && dto.getRegisterId() != null) {
                LambdaQueryWrapper<MedicalRecord> mrWrapper = new LambdaQueryWrapper<>();
                mrWrapper.eq(MedicalRecord::getRegisterId, dto.getRegisterId());
                mrWrapper.orderByDesc(MedicalRecord::getCreateTime);
                mrWrapper.last("LIMIT 1");
                MedicalRecord medicalRecord = medicalRecordMapper.selectOne(mrWrapper);
                if (medicalRecord != null) {
                    recordId = medicalRecord.getId();
                    log.info("通过挂号ID {} 自动关联病历ID: {}", dto.getRegisterId(), recordId);
                }
            }
            
            Prescription prescription = new Prescription();
            prescription.setPrescriptionNo(prescriptionNo);
            prescription.setRegisterId(dto.getRegisterId());
            prescription.setHospitalizationId(dto.getHospitalizationId());
            prescription.setRecordId(recordId);
            prescription.setPetId(dto.getPetId());
            prescription.setDoctorId(validDoctorId);
            prescription.setPrescriptionType(dto.getPrescriptionType() != null ? dto.getPrescriptionType() : 0);
            prescription.setDiagnosis(dto.getDiagnosis());
            prescription.setTotalAmount(dto.getTotalAmount());
            prescription.setRemark(dto.getRemark());
            prescription.setStatus(0);  // 0=草稿
            prescription.setIsDeleted(0);
            prescription.setCreateTime(LocalDateTime.now());
            prescription.setUpdateTime(LocalDateTime.now());
            
            // 3. 保存到数据库
            int result = prescriptionMapper.insert(prescription);
            log.info("处方保存结果: {}, 处方ID: {}, 处方号: {}", result, prescription.getId(), prescriptionNo);
            
            if (result <= 0) {
                throw new RuntimeException("Prescription save failed");
            }
            
            Long newPrescriptionId = prescription.getId();
            
            // 4. 保存处方明细 - 药品
            if (dto.getDrugs() != null && !dto.getDrugs().isEmpty()) {
                for (int i = 0; i < dto.getDrugs().size(); i++) {
                    PrescriptionCreateDto.PrescriptionDrugDto drugDto = dto.getDrugs().get(i);
                    PrescriptionItem item = new PrescriptionItem();
                    item.setPrescriptionId(newPrescriptionId);
                    item.setItemType("MEDICINE");
                    item.setRefItemId(drugDto.getDrugId());
                    item.setItemName(drugDto.getDrugName());
                    // 查询药品规格并校验保质期
                    if (drugDto.getDrugId() != null) {
                        try {
                            MedicineItem medicine = medicineItemMapper.selectById(drugDto.getDrugId());
                            if (medicine != null) {
                                item.setSpecification(medicine.getSpecification());
                                if (medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
                                    throw new RuntimeException("药品 " + medicine.getMedicineName() + " 已过期，无法使用");
                                }
                            }
                        } catch (RuntimeException re) {
                            throw re;
                        } catch (Exception e) {
                            log.warn("查询药品规格失败, drugId={}", drugDto.getDrugId());
                        }
                    }
                    item.setQuantity(BigDecimal.valueOf(drugDto.getQuantity() != null ? drugDto.getQuantity() : 1));
                    item.setDosage(drugDto.getDosage());
                    item.setUsageMethod(drugDto.getUsage());
                    item.setFrequency(drugDto.getFrequency());
                    item.setUseDays(drugDto.getDays());
                    item.setUnitPrice(drugDto.getPrice());
                    item.setLineAmount(drugDto.getAmount());
                    item.setSortNo(i + 1);
                    item.setCreateTime(LocalDateTime.now());
                    item.setUpdateTime(LocalDateTime.now());
                    prescriptionItemMapper.insert(item);
                }
                log.info("处方药品明细保存完成, 数量: {}", dto.getDrugs().size());
            }
            
            // 5. 保存处方明细 - 服务
            if (dto.getServices() != null && !dto.getServices().isEmpty()) {
                for (int i = 0; i < dto.getServices().size(); i++) {
                    PrescriptionCreateDto.PrescriptionServiceDto serviceDto = dto.getServices().get(i);
                    PrescriptionItem item = new PrescriptionItem();
                    item.setPrescriptionId(newPrescriptionId);
                    item.setItemType("SERVICE");
                    item.setRefItemId(serviceDto.getServiceId());
                    item.setItemName(serviceDto.getServiceName());
                    item.setQuantity(BigDecimal.valueOf(serviceDto.getQuantity() != null ? serviceDto.getQuantity() : 1));
                    item.setUnitPrice(serviceDto.getPrice());
                    item.setLineAmount(serviceDto.getAmount());
                    item.setSortNo(i + 1);
                    item.setCreateTime(LocalDateTime.now());
                    item.setUpdateTime(LocalDateTime.now());
                    prescriptionItemMapper.insert(item);
                }
                log.info("处方服务明细保存完成, 数量: {}", dto.getServices().size());
            }
            
            // 验证：立即查询确认明细已保存
            LambdaQueryWrapper<PrescriptionItem> verifyWrapper = new LambdaQueryWrapper<>();
            verifyWrapper.eq(PrescriptionItem::getPrescriptionId, newPrescriptionId);
            List<PrescriptionItem> verifyItems = prescriptionItemMapper.selectList(verifyWrapper);
            log.info("验证查询 - 处方ID={}, 明细数量={}", newPrescriptionId, verifyItems.size());
            
            // 6. 返回VO
            PrescriptionVo vo = new PrescriptionVo();
            vo.setPrescriptionId(prescription.getId());
            vo.setPrescriptionNo(prescriptionNo);
            vo.setRegisterId(dto.getRegisterId());
            vo.setPetId(dto.getPetId());
            vo.setDoctorId(dto.getDoctorId());
            vo.setDiagnosis(dto.getDiagnosis());
            vo.setTotalAmount(dto.getTotalAmount());
            vo.setStatus(0);
            vo.setStatusDesc("Draft");
            vo.setCreateTime(LocalDateTime.now());
            
            // 补充宠物名称
            Pet pet = petMapper.selectById(dto.getPetId());
            if (pet != null) {
                vo.setPetName(pet.getName());
            }
            
            log.info("处方创建完成, 处方号: {}", prescriptionNo);
            return vo;
        } catch (Exception e) {
            log.error("创建处方失败", e);
            String msg = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            throw new RuntimeException("Create prescription failed: " + msg);
        }
    }

    @Override
    public PrescriptionVo submitPrescription(Long prescriptionId) {
        log.info("提交处方, 处方ID: {}", prescriptionId);
        
        // 1. 查询处方是否存在
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new RuntimeException("Prescription does not exist");
        }
        
        // 2. 检查是否已经是已提交状态
        if (prescription.getStatus() == 1) {
            throw new RuntimeException("Prescription is already submitted");
        }
        
        // 3. 校验处方中的药品是否过期
        LambdaQueryWrapper<PrescriptionItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId)
                   .eq(PrescriptionItem::getItemType, "MEDICINE");
        List<PrescriptionItem> drugItems = prescriptionItemMapper.selectList(itemWrapper);
        for (PrescriptionItem drugItem : drugItems) {
            if (drugItem.getRefItemId() != null) {
                MedicineItem medicine = medicineItemMapper.selectById(drugItem.getRefItemId());
                if (medicine != null && medicine.getExpiryDate() != null && medicine.getExpiryDate().isBefore(LocalDate.now())) {
                    throw new RuntimeException("药品 " + medicine.getMedicineName() + " 已过期，无法使用");
                }
            }
        }
        
        // 4. 更新状态为已提交
        prescription.setStatus(1);
        prescription.setUpdateTime(LocalDateTime.now());
        
        int result = prescriptionMapper.updateById(prescription);
        if (result <= 0) {
            throw new RuntimeException("Submit prescription failed");
        }
        
        log.info("处方提交成功, 处方ID: {}, 状态已更新为已提交", prescriptionId);

        // 5. 同步处方金额到关联订单
        try {
            syncPrescriptionToOrder(prescription.getRegisterId());
        } catch (Exception e) {
            log.warn("同步处方金额到订单失败, prescriptionId={}", prescriptionId, e);
        }

        // 6. 返回VO
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescription.getId());
        vo.setPrescriptionNo(prescription.getPrescriptionNo());
        vo.setRegisterId(prescription.getRegisterId());
        vo.setPetId(prescription.getPetId());
        vo.setDoctorId(prescription.getDoctorId());
        vo.setDiagnosis(prescription.getDiagnosis());
        vo.setTotalAmount(prescription.getTotalAmount());
        vo.setStatus(1);
        vo.setStatusDesc("Submitted");
        vo.setCreateTime(prescription.getCreateTime());
        
        // 补充宠物名称
        Pet pet = petMapper.selectById(prescription.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
        }
        
        return vo;
    }

    @Override
    public PageResultVo<PrescriptionVo> getPrescriptionList(PageQueryDto pageQuery, Long petId) {
        try {
            log.info("查询处方列表, pageNum={}, pageSize={}, petId={}, doctorId={}, keyword={}", 
                pageQuery.getPageNum(), pageQuery.getPageSize(), petId, 
                pageQuery.getDoctorId(), pageQuery.getKeyword());
            
            String keyword = pageQuery.getKeyword();
            
            // 构建查询条件
            LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Prescription::getIsDeleted, 0);
            
            // 按传入的petId筛选
            if (petId != null && petId > 0) {
                wrapper.eq(Prescription::getPetId, petId);
            }
            
            // 按医生ID筛选
            if (pageQuery.getDoctorId() != null && pageQuery.getDoctorId() > 0) {
                wrapper.eq(Prescription::getDoctorId, pageQuery.getDoctorId());
            }
            
            // 按关键词搜索（处方号 或 诊断结果 或 宠物名称）
            if (keyword != null && !keyword.isEmpty()) {
                // 先查询匹配宠物名称的宠物ID列表
                LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
                petWrapper.select(Pet::getId);
                petWrapper.like(Pet::getName, keyword);
                petWrapper.eq(Pet::getIsDeleted, 0);
                List<Pet> matchedPets = petMapper.selectList(petWrapper);
                
                if (matchedPets != null && !matchedPets.isEmpty()) {
                    List<Long> matchedPetIds = matchedPets.stream()
                        .map(Pet::getId)
                        .collect(Collectors.toList());
                    log.info("通过宠物名称'{}'匹配到宠物ID: {}", keyword, matchedPetIds);
                    
                    // 按宠物ID 或 处方号 或 诊断结果 搜索
                    wrapper.and(w -> w
                        .in(Prescription::getPetId, matchedPetIds)
                        .or()
                        .like(Prescription::getPrescriptionNo, keyword)
                        .or()
                        .like(Prescription::getDiagnosis, keyword)
                    );
                } else {
                    // 没有匹配到宠物，只按处方号或诊断结果搜索
                    wrapper.and(w -> w
                        .like(Prescription::getPrescriptionNo, keyword)
                        .or()
                        .like(Prescription::getDiagnosis, keyword)
                    );
                }
            }
            
            // 按状态筛选
            if (pageQuery.getStatus() != null) {
                wrapper.eq(Prescription::getStatus, pageQuery.getStatus());
            }
            
            // 按创建时间倒序
            wrapper.orderByDesc(Prescription::getCreateTime);
            
            // 分页查询
            Page<Prescription> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
            Page<Prescription> prescriptionPage = prescriptionMapper.selectPage(page, wrapper);
            
            log.info("查询到处方记录数: {}", prescriptionPage.getRecords().size());
            
            // 转换为VO
            List<PrescriptionVo> voList = new ArrayList<>();
            for (Prescription prescription : prescriptionPage.getRecords()) {
                PrescriptionVo vo = new PrescriptionVo();
                vo.setPrescriptionId(prescription.getId());
                vo.setPrescriptionNo(prescription.getPrescriptionNo());
                vo.setRegisterId(prescription.getRegisterId());
                vo.setPetId(prescription.getPetId());
                vo.setDoctorId(prescription.getDoctorId());
                vo.setDiagnosis(prescription.getDiagnosis());
                vo.setTotalAmount(prescription.getTotalAmount());
                vo.setStatus(prescription.getStatus());
                vo.setStatusDesc(prescription.getStatus() == 1 ? "Submitted" : "Draft");
                vo.setRemark(prescription.getRemark());
                vo.setCreateTime(prescription.getCreateTime());
                
                // 补充宠物名称
                if (prescription.getPetId() != null) {
                    Pet pet = petMapper.selectById(prescription.getPetId());
                    if (pet != null) {
                        vo.setPetName(pet.getName());
                    }
                }
                
                voList.add(vo);
            }
            
            return PageResultVo.success(voList, prescriptionPage.getTotal(), 
                prescriptionPage.getCurrent(), prescriptionPage.getSize());
                
        } catch (Exception e) {
            log.error("查询处方列表失败", e);
            return PageResultVo.success(new ArrayList<>(), 0L, 
                (long) pageQuery.getPageNum(), (long) pageQuery.getPageSize());
        }
    }


    @Override
    public PrescriptionVo getPrescriptionDetail(Long prescriptionId) {
        // 1. 查询处方主表
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            return new PrescriptionVo();
        }

        // 2. 查询宠物名称
        String petName = "";
        if (prescription.getPetId() != null) {
            Pet pet = petMapper.selectById(prescription.getPetId());
            if (pet != null) {
                petName = pet.getName();
            }
        }

        // 3. 查询医生名称
        String doctorName = "";
        if (prescription.getDoctorId() != null) {
            Doctor doctor = doctorMapper.selectById(prescription.getDoctorId());
            if (doctor != null) {
                doctorName = doctor.getName();
            }
        }

        // 4. 查询处方明细（药品 + 服务）
        log.info("查询处方明细, prescriptionId={}", prescriptionId);
        LambdaQueryWrapper<PrescriptionItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(PrescriptionItem::getPrescriptionId, prescriptionId);
        List<PrescriptionItem> items = prescriptionItemMapper.selectList(itemWrapper);
        log.info("查询到处方明细数量: {}, items={}", items.size(), items);

        List<PrescriptionVo.PrescriptionDrugVo> drugs = new ArrayList<>();
        List<PrescriptionVo.PrescriptionServiceVo> services = new ArrayList<>();

        for (PrescriptionItem item : items) {
            if ("MEDICINE".equals(item.getItemType())) {
                PrescriptionVo.PrescriptionDrugVo drug = new PrescriptionVo.PrescriptionDrugVo();
                drug.setDrugId(item.getRefItemId());
                drug.setDrugName(item.getItemName());
                drug.setSpecification(item.getSpecification());
                drug.setQuantity(item.getQuantity() != null ? item.getQuantity().intValue() : 0);
                drug.setDosage(item.getDosage());
                drug.setUsage(item.getUsageMethod());
                drug.setFrequency(item.getFrequency());
                drug.setDays(item.getUseDays());
                drug.setPrice(item.getUnitPrice());
                drug.setAmount(item.getLineAmount());
                drugs.add(drug);
            } else if ("SERVICE".equals(item.getItemType())) {
                PrescriptionVo.PrescriptionServiceVo service = new PrescriptionVo.PrescriptionServiceVo();
                service.setServiceId(item.getRefItemId());
                service.setServiceName(item.getItemName());
                service.setQuantity(item.getQuantity() != null ? item.getQuantity().intValue() : 0);
                service.setPrice(item.getUnitPrice());
                service.setAmount(item.getLineAmount());
                services.add(service);
            }
        }

        // 5. 组装 VO
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescription.getId());
        vo.setPrescriptionNo(prescription.getPrescriptionNo());
        vo.setRegisterId(prescription.getRegisterId());
        vo.setRecordId(prescription.getRecordId());
        vo.setPetId(prescription.getPetId());
        vo.setDoctorId(prescription.getDoctorId());
        vo.setPetName(petName);
        vo.setDoctorName(doctorName);
        vo.setPrescriptionType(prescription.getPrescriptionType());
        vo.setDiagnosis(prescription.getDiagnosis());
        vo.setTotalAmount(prescription.getTotalAmount());
        vo.setStatus(prescription.getStatus());
        vo.setStatusDesc(getStatusDesc(prescription.getStatus()));
        vo.setRemark(prescription.getRemark());
        vo.setCreateTime(prescription.getCreateTime());
        vo.setDrugs(drugs);
        vo.setServices(services);

        return vo;
    }

    @Override
    public void printPrescription(Long prescriptionId, HttpServletResponse response) {
        log.info("打印处方，处方ID: {}", prescriptionId);
    }

    @Override
    public List<Map<String, Object>> getServiceList(String keyword) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        try {
            log.info("开始查询服务列表, keyword={}", keyword);
            
            // 构建查询条件
            LambdaQueryWrapper<ServiceItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ServiceItem::getStatus, 1);
            wrapper.eq(ServiceItem::getIsDeleted, 0);
            
            if (keyword != null && !keyword.isEmpty()) {
                wrapper.and(w -> w.like(ServiceItem::getServiceName, keyword)
                                .or()
                                .like(ServiceItem::getServiceCode, keyword));
            }
            
            List<ServiceItem> services = serviceItemMapper.selectList(wrapper);
            log.info("从数据库查询到服务数量: {}", services.size());
            
            for (ServiceItem service : services) {
                Map<String, Object> item = new HashMap<>();
                item.put("serviceId", service.getId());
                item.put("serviceName", service.getServiceName());
                item.put("price", service.getPrice() != null ? service.getPrice() : BigDecimal.ZERO);
                result.add(item);
            }
            
            log.info("返回服务数量: {}", result.size());
        } catch (Exception e) {
            log.error("查询服务列表失败", e);
        }
        
        return result;
    }

    // ==================== 在线咨询（保持简化实现）====================

    @Override
    public Integer getUnreadConsultCount(Long doctorId) {
        try {
            // 查询该医生名下没有医生回复的咨询数量
            // 先查询该医生名下的所有咨询ID
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Consultation::getDoctorId, doctorId);
            List<Consultation> consultations = consultationMapper.selectList(wrapper);
            
            if (consultations.isEmpty()) {
                return 0;
            }
            
            List<Long> consultationIds = consultations.stream()
                .map(Consultation::getId)
                .collect(Collectors.toList());
            
            // 查询有医生回复的咨询ID
            LambdaQueryWrapper<ConsultationReply> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.in(ConsultationReply::getConsultationId, consultationIds);
            replyWrapper.eq(ConsultationReply::getSenderType, "doctor");
            List<ConsultationReply> replies = consultationReplyMapper.selectList(replyWrapper);
            
            List<Long> repliedIds = replies.stream()
                .map(ConsultationReply::getConsultationId)
                .distinct()
                .collect(Collectors.toList());
            
            // 未回复数量 = 总咨询数 - 已回复数
            return consultationIds.size() - repliedIds.size();
        } catch (Exception e) {
            log.error("获取未读咨询数量失败", e);
            return 0;
        }
    }

    @Override
    public PageResultVo<ConsultVo> getConsultList(PageQueryDto pageQuery, Integer replyStatus) {
        try {
            log.info("查询咨询列表, pageNum={}, pageSize={}, replyStatus={}, doctorId={}, keyword={}", 
                pageQuery.getPageNum(), pageQuery.getPageSize(), replyStatus, 
                pageQuery.getDoctorId(), pageQuery.getKeyword());
            
            String keyword = pageQuery.getKeyword();
            
            // 构建查询条件
            LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
            
            // 按医生ID筛选
            if (pageQuery.getDoctorId() != null && pageQuery.getDoctorId() > 0) {
                wrapper.eq(Consultation::getDoctorId, pageQuery.getDoctorId());
            }
            
            // 按关键词搜索（宠物名称、主人姓名、主人电话、咨询标题、咨询内容）
            if (keyword != null && !keyword.isEmpty()) {
                // 1. 查询匹配宠物名称的宠物ID列表
                LambdaQueryWrapper<Pet> petWrapper = new LambdaQueryWrapper<>();
                petWrapper.select(Pet::getId);
                petWrapper.like(Pet::getName, keyword);
                petWrapper.eq(Pet::getIsDeleted, 0);
                List<Pet> matchedPets = petMapper.selectList(petWrapper);
                
                // 2. 查询匹配主人姓名或电话的用户ID列表
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.select(User::getId);
                userWrapper.and(w -> w
                    .like(User::getRealName, keyword)
                    .or()
                    .like(User::getPhone, keyword)
                );
                userWrapper.eq(User::getIsDeleted, 0);
                List<User> matchedUsers = userMapper.selectList(userWrapper);
                
                // 收集匹配的宠物ID和主人ID
                List<Long> matchedPetIds = matchedPets.stream()
                    .map(Pet::getId)
                    .collect(Collectors.toList());
                List<Long> matchedOwnerIds = matchedUsers.stream()
                    .map(User::getId)
                    .collect(Collectors.toList());
                
                log.info("通过关键词'{}'匹配到宠物ID: {}, 主人ID: {}", keyword, matchedPetIds, matchedOwnerIds);
                
                // 构建搜索条件
                wrapper.and(w -> {
                    // 按宠物ID搜索
                    if (!matchedPetIds.isEmpty()) {
                        w.or().in(Consultation::getPetId, matchedPetIds);
                    }
                    // 按主人ID搜索
                    if (!matchedOwnerIds.isEmpty()) {
                        w.or().in(Consultation::getOwnerId, matchedOwnerIds);
                    }
                    // 按标题或内容搜索
                    w.or().like(Consultation::getTitle, keyword)
                    .or().like(Consultation::getContent, keyword);
                });
            }
            
            // 按创建时间倒序
            wrapper.orderByDesc(Consultation::getCreateTime);
            
            // 分页查询
            Page<Consultation> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
            Page<Consultation> consultationPage = consultationMapper.selectPage(page, wrapper);
            
            log.info("查询到咨询记录数: {}", consultationPage.getRecords().size());
            
            // 批量查询回复状态：先获取所有咨询ID
            List<Long> consultationIds = consultationPage.getRecords().stream()
                .map(Consultation::getId)
                .collect(Collectors.toList());
            
            // 查询所有医生回复
            Map<Long, Integer> replyStatusMap = new HashMap<>();
            Map<Long, ConsultationReply> latestDoctorReplyMap = new HashMap<>();
            if (!consultationIds.isEmpty()) {
                LambdaQueryWrapper<ConsultationReply> replyWrapper = new LambdaQueryWrapper<>();
                replyWrapper.in(ConsultationReply::getConsultationId, consultationIds);
                replyWrapper.eq(ConsultationReply::getSenderType, "doctor");
                replyWrapper.orderByDesc(ConsultationReply::getCreateTime);
                List<ConsultationReply> replies = consultationReplyMapper.selectList(replyWrapper);
                
                for (ConsultationReply reply : replies) {
                    replyStatusMap.put(reply.getConsultationId(), 1);
                    // 只保留最新的回复
                    if (!latestDoctorReplyMap.containsKey(reply.getConsultationId())) {
                        latestDoctorReplyMap.put(reply.getConsultationId(), reply);
                    }
                }
            }
            
            // 转换为VO
            List<ConsultVo> voList = new ArrayList<>();
            for (Consultation consultation : consultationPage.getRecords()) {
                int status = replyStatusMap.getOrDefault(consultation.getId(), 0);
                
                // 如果按回复状态筛选，过滤掉不符合的
                if (replyStatus != null && status != replyStatus) {
                    continue;
                }
                
                ConsultVo vo = new ConsultVo();
                vo.setConsultId(consultation.getId());
                vo.setConsultNo(consultation.getConsultationNo());
                vo.setPetId(consultation.getPetId());
                vo.setDoctorId(consultation.getDoctorId());
                vo.setTitle(consultation.getTitle());
                vo.setContent(consultation.getContent());
                vo.setImages(consultation.getImages());
                vo.setReplyStatus(status);
                vo.setReplyStatusDesc(status == 1 ? "Replied" : "Not replied");
                vo.setConsultTime(consultation.getCreateTime());
                vo.setRating(consultation.getRating());
                vo.setComment(consultation.getRatingComment());
                
                // 补充最新医生回复信息
                ConsultationReply latestReply = latestDoctorReplyMap.get(consultation.getId());
                if (latestReply != null) {
                    vo.setReplyContent(latestReply.getContent());
                    vo.setReplyImages(latestReply.getImages());
                    vo.setReplyTime(latestReply.getCreateTime());
                }
                
                // 补充宠物信息
                if (consultation.getPetId() != null) {
                    Pet pet = petMapper.selectById(consultation.getPetId());
                    if (pet != null) {
                        vo.setPetName(pet.getName());
                        String species = pet.getSpecies();
                        if ("dog".equals(species) || "狗".equals(species)) {
                            vo.setPetType("Dog");
                        } else if ("cat".equals(species) || "猫".equals(species)) {
                            vo.setPetType("Cat");
                        } else if ("rabbit".equals(species) || "兔".equals(species)) {
                            vo.setPetType("Rabbit");
                        } else {
                            vo.setPetType(species);
                        }
                    }
                }
                
                // 补充主人信息
                if (consultation.getOwnerId() != null) {
                    User owner = userMapper.selectById(consultation.getOwnerId());
                    if (owner != null) {
                        vo.setOwnerName(owner.getRealName() != null ? owner.getRealName() : owner.getUsername());
                        vo.setOwnerPhone(owner.getPhone());
                        vo.setOwnerId(owner.getId());
                    }
                }
                
                voList.add(vo);
            }
            
            return PageResultVo.success(voList, consultationPage.getTotal(), 
                consultationPage.getCurrent(), consultationPage.getSize());
                
        } catch (Exception e) {
            log.error("查询咨询列表失败", e);
            return PageResultVo.success(new ArrayList<>(), 0L, 
                (long) pageQuery.getPageNum(), (long) pageQuery.getPageSize());
        }
    }

    @Override
    public ConsultVo replyConsult(ConsultReplyDto dto) {
        log.info("开始回复咨询, consultId={}, doctorId={}", dto.getConsultId(), dto.getDoctorId());
        
        // 查询咨询是否存在
        Consultation consultation = consultationMapper.selectById(dto.getConsultId());
        if (consultation == null) {
            throw new RuntimeException("Consultation record does not exist");
        }
        
        // 插入医生回复到 consultation_reply 表
        ConsultationReply reply = new ConsultationReply();
        reply.setConsultationId(dto.getConsultId());
        reply.setSenderType("doctor");
        reply.setSenderId(dto.getDoctorId());
        reply.setContent(dto.getReplyContent());
        reply.setImages(dto.getReplyImages());
        reply.setIsRead(0);
        reply.setCreateTime(LocalDateTime.now());
        
        int result = consultationReplyMapper.insert(reply);
        if (result <= 0) {
            throw new RuntimeException("Reply save failed");
        }
        
        // 更新咨询状态为已完成
        consultation.setStatus("done");
        consultation.setUpdateTime(LocalDateTime.now());
        consultationMapper.updateById(consultation);
        
        log.info("回复咨询成功, consultId={}", dto.getConsultId());
        
        // 返回VO
        ConsultVo vo = new ConsultVo();
        vo.setConsultId(consultation.getId());
        vo.setReplyStatus(1);
        vo.setReplyContent(dto.getReplyContent());
        vo.setReplyImages(dto.getReplyImages());
        vo.setReplyTime(LocalDateTime.now());
        
        return vo;
    }

    @Override
    public ConsultVo getConsultDetail(Long consultId) {
        log.info("获取咨询详情, consultId={}", consultId);
        
        // 从数据库查询咨询记录
        Consultation consultation = consultationMapper.selectById(consultId);
        if (consultation == null) {
            throw new RuntimeException("Consultation record does not exist");
        }
        
        // 查询所有医生回复，取最新的
        LambdaQueryWrapper<ConsultationReply> replyWrapper = new LambdaQueryWrapper<>();
        replyWrapper.eq(ConsultationReply::getConsultationId, consultId);
        replyWrapper.eq(ConsultationReply::getSenderType, "doctor");
        replyWrapper.orderByDesc(ConsultationReply::getCreateTime);
        List<ConsultationReply> replies = consultationReplyMapper.selectList(replyWrapper);
        
        int replyStatus = replies.isEmpty() ? 0 : 1;
        ConsultationReply latestReply = replies.isEmpty() ? null : replies.get(0);
        
        // 转换为VO
        ConsultVo vo = new ConsultVo();
        vo.setConsultId(consultation.getId());
        vo.setConsultNo(consultation.getConsultationNo());
        vo.setPetId(consultation.getPetId());
        vo.setDoctorId(consultation.getDoctorId());
        vo.setTitle(consultation.getTitle());
        vo.setContent(consultation.getContent());
        vo.setImages(consultation.getImages());
        vo.setReplyStatus(replyStatus);
        vo.setReplyStatusDesc(replyStatus == 1 ? "Replied" : "Not replied");
        vo.setConsultTime(consultation.getCreateTime());
        vo.setRating(consultation.getRating());
        vo.setComment(consultation.getRatingComment());
        
        if (latestReply != null) {
            vo.setReplyContent(latestReply.getContent());
            vo.setReplyImages(latestReply.getImages());
            vo.setReplyTime(latestReply.getCreateTime());
        }
        
        // 补充宠物名称
        if (consultation.getPetId() != null) {
            Pet pet = petMapper.selectById(consultation.getPetId());
            if (pet != null) {
                vo.setPetName(pet.getName());
                String species = pet.getSpecies();
                if ("dog".equals(species) || "狗".equals(species)) {
                    vo.setPetType("Dog");
                } else if ("cat".equals(species) || "猫".equals(species)) {
                    vo.setPetType("Cat");
                } else if ("rabbit".equals(species) || "兔".equals(species)) {
                    vo.setPetType("Rabbit");
                } else {
                    vo.setPetType(species);
                }
            }
        }
        
        // 补充主人信息
        if (consultation.getOwnerId() != null) {
            User owner = userMapper.selectById(consultation.getOwnerId());
            if (owner != null) {
                vo.setOwnerName(owner.getRealName() != null ? owner.getRealName() : owner.getUsername());
                vo.setOwnerPhone(owner.getPhone());
                vo.setOwnerId(owner.getId());
            }
        }
        
        log.info("咨询详情查询成功, consultId={}, replyStatus={}", 
            consultId, replyStatus);
        
        return vo;
    }

    // ==================== 医生统计（保持简化实现）====================

    @Override
    public DoctorStatisticsVo getDoctorStatistics(Long doctorId, String startDate, String endDate) {
        DoctorStatisticsVo vo = new DoctorStatisticsVo();
        
        try {
            Long acceptCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM register_record WHERE doctor_id = ? AND status = 2 AND is_deleted = 0",
                Long.class, doctorId);
            vo.setTotalAccept(acceptCount != null ? acceptCount.intValue() : 0);
        } catch (Exception e) {
            vo.setTotalAccept(0);
        }
        
        try {
            Long prescriptionCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM prescription WHERE doctor_id = ? AND is_deleted = 0",
                Long.class, doctorId);
            vo.setTotalPrescription(prescriptionCount != null ? prescriptionCount.intValue() : 0);
        } catch (Exception e) {
            vo.setTotalPrescription(0);
        }
        
        try {
            Long consultCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM consultation WHERE doctor_id = ?",
                Long.class, doctorId);
            vo.setTotalConsult(consultCount != null ? consultCount.intValue() : 0);
        } catch (Exception e) {
            vo.setTotalConsult(0);
        }
        
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
        Map<String, Object> item = new HashMap<>();
        item.put("date", "2026-04-01");
        item.put("count", 5);
        data.add(item);
        result.put("data", data);
        result.put("statType", statType);
        return result;
    }

    @Override
    public Map<String, Object> getRegisterDetail(Long registerId) {
        Map<String, Object> result = registerRecordMapper.getById(registerId);
        if (result == null) {
            throw new RuntimeException("Register record does not exist");
        }
        return result;
    }

    /**
     * 将处方明细同步到关联订单的 order_item，并重新计算订单总金额
     */
    private void syncPrescriptionToOrder(Long registerId) {
        if (registerId == null) {
            return;
        }
        try {
            // 1. 找到关联的未支付订单
            List<Map<String, Object>> orderRows = jdbcTemplate.queryForList(
                    "SELECT id FROM order_info WHERE register_id = ? AND pay_status = 0", registerId);
            if (orderRows.isEmpty()) {
                return;
            }
            Long orderId = safeParseLong(orderRows.get(0).get("id"));
            if (orderId == null) {
                return;
            }

            // 2. 删除该订单已有的处方相关 order_item（避免重复）
            jdbcTemplate.update("DELETE FROM order_item WHERE order_id = ? AND item_type = 'prescription'", orderId);

            // 3. 查询所有该 registerId 关联的已提交处方
            List<Map<String, Object>> prescRows = jdbcTemplate.queryForList(
                    "SELECT id FROM prescription WHERE register_id = ? AND status = 1 AND is_deleted = 0", registerId);

            for (Map<String, Object> prescRow : prescRows) {
                Long prescId = safeParseLong(prescRow.get("id"));
                if (prescId == null) {
                    continue;
                }
                List<Map<String, Object>> itemRows = jdbcTemplate.queryForList(
                        "SELECT item_name, quantity, unit_price, line_amount FROM prescription_item WHERE prescription_id = ?", prescId);
                for (Map<String, Object> itemRow : itemRows) {
                    String itemName = itemRow.get("item_name") != null ? String.valueOf(itemRow.get("item_name")) : "处方项目";
                    double qty = safeParseDouble(itemRow.get("quantity"));
                    double unitPrice = safeParseDouble(itemRow.get("unit_price"));
                    double lineAmount = safeParseDouble(itemRow.get("line_amount"));
                    if (lineAmount <= 0) {
                        lineAmount = qty * unitPrice;
                    }
                    jdbcTemplate.update(
                            "INSERT INTO order_item(order_id, item_type, item_name, quantity, unit_price, amount, line_amount, create_time) VALUES(?, 'prescription', ?, ?, ?, ?, ?, now())",
                            orderId, itemName, qty, unitPrice, lineAmount, lineAmount);
                }
            }

            // 4. 重新计算订单总金额（所有 order_item 之和）
            Double currentTotal = jdbcTemplate.queryForObject(
                    "SELECT COALESCE(SUM(line_amount), 0) FROM order_item WHERE order_id = ?", Double.class, orderId);
            double newTotal = currentTotal != null ? currentTotal : 0;

            jdbcTemplate.update(
                    "UPDATE order_info SET total_amount = ?, payable_amount = ? WHERE id = ?",
                    newTotal, newTotal, orderId);

            log.info("同步处方金额到订单成功, registerId={}, orderId={}, newTotal={}", registerId, orderId, newTotal);
        } catch (Exception e) {
            log.warn("同步处方金额到订单失败, registerId={}", registerId, e);
        }
    }

    private double safeParseDouble(Object obj) {
        if (obj == null) {
            return 0;
        }
        try {
            return Double.parseDouble(String.valueOf(obj));
        } catch (Exception e) {
            return 0;
        }
    }

    private Long safeParseLong(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return Long.parseLong(String.valueOf(obj));
        } catch (Exception e) {
            return null;
        }
    }
}