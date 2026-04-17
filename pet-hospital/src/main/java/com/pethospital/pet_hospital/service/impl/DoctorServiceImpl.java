package com.pethospital.pet_hospital.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.dto.common.PageQueryDto;
import com.pethospital.pet_hospital.dto.doctor.ConsultReplyDto;
import com.pethospital.pet_hospital.dto.doctor.DoctorStatusUpdateDto;
import com.pethospital.pet_hospital.dto.doctor.MedicalRecordCreateDto;
import com.pethospital.pet_hospital.dto.doctor.PrescriptionCreateDto;
import com.pethospital.pet_hospital.entity.Consult;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.MedicineItem;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.Prescription;
import com.pethospital.pet_hospital.entity.ServiceItem;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.ConsultMapper;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.MedicalRecordMapper;
import com.pethospital.pet_hospital.mapper.MedicineItemMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
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
    private final ConsultMapper consultMapper;
    private final MedicineItemMapper medicineItemMapper;
    private final ServiceItemMapper serviceItemMapper;

    // 修改构造函数
    public DoctorServiceImpl(DoctorMapper doctorMapper, 
                            PetMapper petMapper, 
                            UserMapper userMapper,
                            RegisterRecordMapper registerRecordMapper,
                            MedicalRecordMapper medicalRecordMapper,
                            PrescriptionMapper prescriptionMapper,
                            ConsultMapper consultMapper,
                            MedicineItemMapper medicineItemMapper,
                            ServiceItemMapper serviceItemMapper) {
        this.doctorMapper = doctorMapper;
        this.petMapper = petMapper;
        this.userMapper = userMapper;
        this.registerRecordMapper = registerRecordMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.prescriptionMapper = prescriptionMapper;
        this.consultMapper = consultMapper;
        this.medicineItemMapper = medicineItemMapper;
        this.serviceItemMapper = serviceItemMapper;
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

        return vo;
    }

    @Override
    public void updateDoctorInfo(DoctorInfoVo doctorInfoVo) {
        if (doctorInfoVo == null || doctorInfoVo.getDoctorId() == null) {
            throw new RuntimeException("医生信息不能为空");
        }

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
            result.setMsg("查询成功");
            result.setData(voList);
            result.setTotal(total);
            result.setCurrent((long) pageQuery.getPageNum());
            result.setSize((long) pageQuery.getPageSize());
            result.setPages((total + pageQuery.getPageSize() - 1) / pageQuery.getPageSize());

            return result;

        } catch (Exception e) {
            log.error("查询接诊列表失败", e);
            throw new RuntimeException("查询接诊列表失败: " + e.getMessage());
        }
    }

    private WaitAcceptRegisterVo convertToWaitAcceptRegisterVo(Map<String, Object> record) {
        WaitAcceptRegisterVo vo = new WaitAcceptRegisterVo();
        vo.setRegisterId(getLongValue(record, "id"));
        vo.setRegisterNo(getStringValue(record, "register_no"));
        vo.setPetId(getLongValue(record, "pet_id"));
        vo.setPetName(getStringValue(record, "pet_name"));
        vo.setPetType(getStringValue(record, "pet_type"));
        vo.setBreed(getStringValue(record, "breed"));
        vo.setAge(getIntegerValue(record, "age"));
        vo.setGender(getStringValue(record, "gender"));
        vo.setOwnerId(getLongValue(record, "owner_id"));
        vo.setOwnerName(getStringValue(record, "owner_name"));
        vo.setOwnerPhone(getStringValue(record, "owner_phone"));
        vo.setServiceType(getStringValue(record, "service_type"));
        vo.setSymptom(getStringValue(record, "symptom"));
        vo.setAmount(getBigDecimalValue(record, "amount"));
        
        // 关键修复：直接从 record 中获取原始状态值
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
                    default: statusInt = 0;
                }
            } else if (rawStatus instanceof Number) {
                statusInt = ((Number) rawStatus).intValue();
            }
        }
        
        // 添加日志
        log.info("转换状态: petName={}, rawStatus={}, convertedStatus={}", 
                vo.getPetName(), rawStatus, statusInt);
        
        vo.setStatus(statusInt);
        vo.setStatusDesc(getStatusDesc(statusInt));
        
        Object visitTime = record.get("visit_time");
        if (visitTime != null) {
            vo.setRegisterTime(parseDateTime(visitTime.toString()));
        }
        Object acceptTime = record.get("accept_time");
        if (acceptTime != null) {
            vo.setAcceptTime(parseDateTime(acceptTime.toString()));
        }
        return vo;
    }

    // 添加这个辅助方法
    private String getStatusDesc(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待接诊";
            case 1: return "接诊中";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
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
        result.put("name", pet.getName() != null ? pet.getName() : "未命名");

        String species = pet.getSpecies();
        String typeStr;
        if ("dog".equals(species) || "狗".equals(species)) {
            typeStr = "犬";
        } else if ("cat".equals(species) || "猫".equals(species)) {
            typeStr = "猫";
        } else if ("rabbit".equals(species) || "兔".equals(species)) {
            typeStr = "兔";
        } else {
            typeStr = species != null ? species : "未知";
        }
        result.put("type", typeStr);

        result.put("breed", pet.getBreed() != null ? pet.getBreed() : "未知品种");
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
        result.put("chipNumber", pet.getChipNumber() != null ? pet.getChipNumber() : "未植入");
        result.put("allergy", pet.getAllergy() != null ? pet.getAllergy() : "");
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

        String ownerName = "未知";
        String ownerPhone = "未填写";
        String ownerAddress = pet.getOwnerAddress();

        if (pet.getOwnerUserId() != null) {
            User owner = userMapper.selectById(pet.getOwnerUserId());
            if (owner != null) {
                ownerName = owner.getRealName() != null ? owner.getRealName() : owner.getUsername();
                ownerPhone = owner.getPhone() != null ? owner.getPhone() : "未填写";
            }
        }
        if ("未知".equals(ownerName) && pet.getOwnerName() != null) {
            ownerName = pet.getOwnerName();
        }
        if ("未填写".equals(ownerPhone) && pet.getOwnerPhone() != null) {
            ownerPhone = pet.getOwnerPhone();
        }
        if (ownerAddress == null) {
            ownerAddress = "未填写地址";
        }

        result.put("ownerName", ownerName);
        result.put("ownerPhone", ownerPhone);
        result.put("ownerAddress", ownerAddress);
        result.put("vaccineStatus", "已接种狂犬疫苗");

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
            throw new RuntimeException("更新接诊状态失败，挂号单不存在或已被删除");
        }
    }

    // ==================== 病历管理（关键修复：真正查询数据库）====================

    @Override
    public MedicalRecordVo createMedicalRecord(MedicalRecordCreateDto dto) {
        // 1. 创建实体对象
        MedicalRecord record = new MedicalRecord();
        record.setRegisterId(dto.getRegisterId());
        record.setPetId(dto.getPetId());
        record.setDoctorId(dto.getDoctorId());
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
    }

    @Override
    public MedicalRecordVo updateMedicalRecord(MedicalRecordVo recordVo) {
        if (recordVo == null || recordVo.getRecordId() == null) {
            throw new RuntimeException("病历信息不能为空");
        }
        
        MedicalRecord record = medicalRecordMapper.selectById(recordVo.getRecordId());
        if (record == null) {
            throw new RuntimeException("病历不存在");
        }
        
        // 只更新非空字段
        if (recordVo.getChiefComplaint() != null) {
            record.setChiefComplaint(recordVo.getChiefComplaint());
        }
        if (recordVo.getSymptoms() != null) {
            record.setSymptoms(recordVo.getSymptoms());
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
            result.setMsg("查询成功");
            result.setData(voList);
            result.setTotal(recordPage.getTotal());
            result.setCurrent(recordPage.getCurrent());
            result.setSize(recordPage.getSize());
            result.setPages(recordPage.getPages());
            
            log.info("查询病历列表成功, 共{}条记录", voList.size());
            return result;
            
        } catch (Exception e) {
            log.error("查询病历列表失败", e);
            throw new RuntimeException("查询病历列表失败: " + e.getMessage());
        }
    }


    @Override
    public MedicalRecordVo getMedicalRecordDetail(Long recordId) {
        MedicalRecord record = medicalRecordMapper.selectById(recordId);
        if (record == null) {
            throw new RuntimeException("病历不存在");
        }
        return convertToMedicalRecordVo(record);
    }

    @Override
    public void exportMedicalRecord(String recordIds, String format, HttpServletResponse response) {
        log.info("导出病历，病历ID: {}, 格式: {}", recordIds, format);
        // TODO: 实现导出逻辑
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
                item.put("price", drug.getPrice() != null ? drug.getPrice() : BigDecimal.ZERO);
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
        log.info("开始创建处方, 挂号ID: {}, 宠物ID: {}, 医生ID: {}", 
            dto.getRegisterId(), dto.getPetId(), dto.getDoctorId());
        
        // 1. 生成处方编号
        String prescriptionNo = "PR" + System.currentTimeMillis();
        
        // 2. 创建处方实体
        Prescription prescription = new Prescription();
        prescription.setPrescriptionNo(prescriptionNo);
        prescription.setRegisterId(dto.getRegisterId());
        prescription.setPetId(dto.getPetId());
        prescription.setDoctorId(dto.getDoctorId());
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
            throw new RuntimeException("处方保存失败");
        }
        
        // 4. 返回VO
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescription.getId());
        vo.setPrescriptionNo(prescriptionNo);
        vo.setRegisterId(dto.getRegisterId());
        vo.setPetId(dto.getPetId());
        vo.setDoctorId(dto.getDoctorId());
        vo.setDiagnosis(dto.getDiagnosis());
        vo.setTotalAmount(dto.getTotalAmount());
        vo.setStatus(0);
        vo.setStatusDesc("草稿");
        vo.setCreateTime(LocalDateTime.now());
        
        // 补充宠物名称
        Pet pet = petMapper.selectById(dto.getPetId());
        if (pet != null) {
            vo.setPetName(pet.getName());
        }
        
        log.info("处方创建完成, 处方号: {}", prescriptionNo);
        return vo;
    }

    @Override
    public PrescriptionVo submitPrescription(Long prescriptionId) {
        log.info("提交处方, 处方ID: {}", prescriptionId);
        
        // 1. 查询处方是否存在
        Prescription prescription = prescriptionMapper.selectById(prescriptionId);
        if (prescription == null) {
            throw new RuntimeException("处方不存在");
        }
        
        // 2. 检查是否已经是已提交状态
        if (prescription.getStatus() == 1) {
            throw new RuntimeException("处方已经是已提交状态");
        }
        
        // 3. 更新状态为已提交
        prescription.setStatus(1);
        prescription.setUpdateTime(LocalDateTime.now());
        
        int result = prescriptionMapper.updateById(prescription);
        if (result <= 0) {
            throw new RuntimeException("提交处方失败");
        }
        
        log.info("处方提交成功, 处方ID: {}, 状态已更新为已提交", prescriptionId);
        
        // 4. 返回VO
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescription.getId());
        vo.setPrescriptionNo(prescription.getPrescriptionNo());
        vo.setRegisterId(prescription.getRegisterId());
        vo.setPetId(prescription.getPetId());
        vo.setDoctorId(prescription.getDoctorId());
        vo.setDiagnosis(prescription.getDiagnosis());
        vo.setTotalAmount(prescription.getTotalAmount());
        vo.setStatus(1);
        vo.setStatusDesc("已提交");
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
                vo.setStatusDesc(prescription.getStatus() == 1 ? "已提交" : "草稿");
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
        PrescriptionVo vo = new PrescriptionVo();
        vo.setPrescriptionId(prescriptionId);
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
        return 0;
    }

    @Override
    public PageResultVo<ConsultVo> getConsultList(PageQueryDto pageQuery, Integer replyStatus) {
        try {
            log.info("查询咨询列表, pageNum={}, pageSize={}, replyStatus={}, doctorId={}, keyword={}", 
                pageQuery.getPageNum(), pageQuery.getPageSize(), replyStatus, 
                pageQuery.getDoctorId(), pageQuery.getKeyword());
            
            String keyword = pageQuery.getKeyword();
            
            // 构建查询条件
            LambdaQueryWrapper<Consult> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Consult::getIsDeleted, 0);
            
            // 按医生ID筛选
            if (pageQuery.getDoctorId() != null && pageQuery.getDoctorId() > 0) {
                wrapper.eq(Consult::getDoctorId, pageQuery.getDoctorId());
            }
            
            // 按回复状态筛选
            if (replyStatus != null) {
                wrapper.eq(Consult::getReplyStatus, replyStatus);
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
                        w.or().in(Consult::getPetId, matchedPetIds);
                    }
                    // 按主人ID搜索
                    if (!matchedOwnerIds.isEmpty()) {
                        w.or().in(Consult::getOwnerUserId, matchedOwnerIds);
                    }
                    // 按标题或内容搜索
                    w.or().like(Consult::getTitle, keyword)
                    .or().like(Consult::getContent, keyword);
                });
            }
            
            // 按咨询时间倒序
            wrapper.orderByDesc(Consult::getConsultTime);
            
            // 分页查询
            Page<Consult> page = new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize());
            Page<Consult> consultPage = consultMapper.selectPage(page, wrapper);
            
            log.info("查询到咨询记录数: {}", consultPage.getRecords().size());
            
            // 转换为VO
            List<ConsultVo> voList = new ArrayList<>();
            for (Consult consult : consultPage.getRecords()) {
                ConsultVo vo = new ConsultVo();
                vo.setConsultId(consult.getId());
                vo.setConsultNo(consult.getConsultNo());
                vo.setPetId(consult.getPetId());
                vo.setDoctorId(consult.getDoctorId());
                vo.setTitle(consult.getTitle());
                vo.setContent(consult.getContent());
                vo.setImages(consult.getImages());
                vo.setReplyContent(consult.getReplyContent());
                vo.setReplyImages(consult.getReplyImages());
                vo.setReplyStatus(consult.getReplyStatus());
                vo.setReplyStatusDesc(consult.getReplyStatus() == 1 ? "已回复" : "未回复");
                vo.setConsultTime(consult.getConsultTime());
                vo.setReplyTime(consult.getReplyTime());
                vo.setRating(consult.getRating());
                vo.setComment(consult.getComment());
                
                // 补充宠物信息
                if (consult.getPetId() != null) {
                    Pet pet = petMapper.selectById(consult.getPetId());
                    if (pet != null) {
                        vo.setPetName(pet.getName());
                        String species = pet.getSpecies();
                        if ("dog".equals(species) || "狗".equals(species)) {
                            vo.setPetType("犬");
                        } else if ("cat".equals(species) || "猫".equals(species)) {
                            vo.setPetType("猫");
                        } else if ("rabbit".equals(species) || "兔".equals(species)) {
                            vo.setPetType("兔");
                        } else {
                            vo.setPetType(species);
                        }
                    }
                }
                
                // 补充主人信息
                if (consult.getOwnerUserId() != null) {
                    User owner = userMapper.selectById(consult.getOwnerUserId());
                    if (owner != null) {
                        vo.setOwnerName(owner.getRealName() != null ? owner.getRealName() : owner.getUsername());
                        vo.setOwnerPhone(owner.getPhone());
                        vo.setOwnerId(owner.getId());
                    }
                }
                
                voList.add(vo);
            }
            
            return PageResultVo.success(voList, consultPage.getTotal(), 
                consultPage.getCurrent(), consultPage.getSize());
                
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
        Consult consult = consultMapper.selectById(dto.getConsultId());
        if (consult == null) {
            throw new RuntimeException("咨询记录不存在");
        }
        
        // 更新咨询记录
        consult.setReplyContent(dto.getReplyContent());
        consult.setReplyImages(dto.getImages());
        consult.setReplyStatus(1);
        consult.setReplyTime(LocalDateTime.now());
        consult.setDoctorId(dto.getDoctorId());
        consult.setUpdateTime(LocalDateTime.now());
        
        // 保存到数据库
        int result = consultMapper.updateById(consult);
        if (result <= 0) {
            throw new RuntimeException("回复保存失败");
        }
        
        log.info("回复咨询成功, consultId={}", dto.getConsultId());
        
        // 返回VO
        ConsultVo vo = new ConsultVo();
        vo.setConsultId(consult.getId());
        vo.setReplyStatus(consult.getReplyStatus());
        vo.setReplyContent(consult.getReplyContent());
        vo.setReplyTime(consult.getReplyTime());
        
        return vo;
    }

    @Override
    public ConsultVo getConsultDetail(Long consultId) {
        log.info("获取咨询详情, consultId={}", consultId);
        
        // 从数据库查询咨询记录
        Consult consult = consultMapper.selectById(consultId);
        if (consult == null) {
            throw new RuntimeException("咨询记录不存在");
        }
        
        // 转换为VO
        ConsultVo vo = new ConsultVo();
        vo.setConsultId(consult.getId());
        vo.setConsultNo(consult.getConsultNo());
        vo.setPetId(consult.getPetId());
        vo.setDoctorId(consult.getDoctorId());
        vo.setTitle(consult.getTitle());
        vo.setContent(consult.getContent());
        vo.setImages(consult.getImages());
        vo.setReplyContent(consult.getReplyContent());
        vo.setReplyImages(consult.getReplyImages());
        vo.setReplyStatus(consult.getReplyStatus());
        vo.setReplyTime(consult.getReplyTime());
        vo.setConsultTime(consult.getConsultTime());
        vo.setRating(consult.getRating());
        vo.setComment(consult.getComment());
        
        // 补充宠物名称
        if (consult.getPetId() != null) {
            Pet pet = petMapper.selectById(consult.getPetId());
            if (pet != null) {
                vo.setPetName(pet.getName());
                String species = pet.getSpecies();
                if ("dog".equals(species) || "狗".equals(species)) {
                    vo.setPetType("犬");
                } else if ("cat".equals(species) || "猫".equals(species)) {
                    vo.setPetType("猫");
                } else if ("rabbit".equals(species) || "兔".equals(species)) {
                    vo.setPetType("兔");
                } else {
                    vo.setPetType(species);
                }
            }
        }
        
        // 补充主人信息
        if (consult.getOwnerUserId() != null) {
            User owner = userMapper.selectById(consult.getOwnerUserId());
            if (owner != null) {
                vo.setOwnerName(owner.getRealName() != null ? owner.getRealName() : owner.getUsername());
                vo.setOwnerPhone(owner.getPhone());
                vo.setOwnerId(owner.getId());
            }
        }
        
        log.info("咨询详情查询成功, consultId={}, replyStatus={}, replyContent={}", 
            consultId, consult.getReplyStatus(), consult.getReplyContent());
        
        return vo;
    }

    // ==================== 医生统计（保持简化实现）====================

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
            throw new RuntimeException("挂号单不存在");
        }
        return result;
    }
}