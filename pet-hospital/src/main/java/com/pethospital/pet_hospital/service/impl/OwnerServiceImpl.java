package com.pethospital.pet_hospital.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.entity.Appointment;
import com.pethospital.pet_hospital.entity.Consultation;
import com.pethospital.pet_hospital.entity.ConsultationReply;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.OrderInfo;
import com.pethospital.pet_hospital.entity.OrderItem;
import com.pethospital.pet_hospital.entity.OwnerHealthRecord;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.Prescription;
import com.pethospital.pet_hospital.entity.PrescriptionItem;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.AppointmentMapper;
import com.pethospital.pet_hospital.mapper.ConsultationMapper;
import com.pethospital.pet_hospital.mapper.ConsultationReplyMapper;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.MedicalRecordMapper;
import com.pethospital.pet_hospital.mapper.OrderInfoMapper;
import com.pethospital.pet_hospital.mapper.OrderItemMapper;
import com.pethospital.pet_hospital.mapper.OwnerHealthRecordMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.PrescriptionItemMapper;
import com.pethospital.pet_hospital.mapper.PrescriptionMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IOwnerService;
import com.pethospital.pet_hospital.utils.EncryptUtil;
import com.pethospital.pet_hospital.vo.owner.HealthRecordDetailVo;

@Service
public class OwnerServiceImpl implements IOwnerService {

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private DoctorMapper doctorMapper;
    @Autowired
    private MedicalRecordMapper medicalRecordMapper;
    @Autowired
    private OwnerHealthRecordMapper ownerHealthRecordMapper;
    @Autowired
    private PrescriptionMapper prescriptionMapper;
    @Autowired
    private PrescriptionItemMapper prescriptionItemMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ConsultationMapper consultationMapper;
    @Autowired
    private ConsultationReplyMapper consultationReplyMapper;
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderItemMapper orderItemMapper;
    @Autowired
    private UserMapper userMapper;

    // ==================== 宠物管理 ====================
    @Override
    public Page<Pet> getPetList(Page<Pet> page, Long userId, String keyword) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(Pet::getOwnerId, userId).or().eq(Pet::getOwnerUserId, userId));
        wrapper.eq(Pet::getStatus, 1);
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Pet::getName, keyword);
        }
        wrapper.orderByDesc(Pet::getCreateTime);
        return petMapper.selectPage(page, wrapper);
    }

    @Override
    public Pet getPetDetail(Long petId, Long userId) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Pet::getId, petId);
        wrapper.and(w -> w.eq(Pet::getOwnerId, userId).or().eq(Pet::getOwnerUserId, userId));
        return petMapper.selectOne(wrapper);
    }

    @Override
    public boolean addPet(Pet pet) {
        pet.setCreateTime(LocalDateTime.now());
        pet.setUpdateTime(LocalDateTime.now());
        pet.setStatus(1);
        // 根据生日计算年龄
        if (pet.getBirthday() != null && pet.getAge() == null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate birthday = pet.getBirthday();
            int age = today.getYear() - birthday.getYear();
            if (today.getMonthValue() < birthday.getMonthValue() ||
                    (today.getMonthValue() == birthday.getMonthValue() && today.getDayOfMonth() < birthday.getDayOfMonth())) {
                age--;
            }
            pet.setAge(age);
        }
        // 同步 type -> species，确保管理端和医生端显示一致
        if (pet.getType() != null && !pet.getType().isEmpty()) {
            pet.setSpecies(pet.getType());
        }
        // 同步 gender -> genderCode
        String gender = pet.getGender();
        if (gender != null) {
            String g = gender.trim().toLowerCase();
            if (g.equals("male") || g.equals("公") || g.equals("1")) {
                pet.setGenderCode(1);
            } else if (g.equals("female") || g.equals("母") || g.equals("2")) {
                pet.setGenderCode(2);
            }
        }
        return petMapper.insert(pet) > 0;
    }

    @Override
    public boolean updatePet(Pet pet, Long userId) {
        Pet existPet = getPetDetail(pet.getId(), userId);
        if (existPet == null) {
            return false;
        }
        pet.setUpdateTime(LocalDateTime.now());
        // 根据生日计算年龄
        if (pet.getBirthday() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate birthday = pet.getBirthday();
            int age = today.getYear() - birthday.getYear();
            if (today.getMonthValue() < birthday.getMonthValue() ||
                    (today.getMonthValue() == birthday.getMonthValue() && today.getDayOfMonth() < birthday.getDayOfMonth())) {
                age--;
            }
            pet.setAge(age);
        }
        // 同步 type -> species，确保管理端和医生端显示一致
        if (pet.getType() != null && !pet.getType().isEmpty()) {
            pet.setSpecies(pet.getType());
        }
        // 同步 gender -> genderCode
        String gender = pet.getGender();
        if (gender != null) {
            String g = gender.trim().toLowerCase();
            if (g.equals("male") || g.equals("公") || g.equals("1")) {
                pet.setGenderCode(1);
            } else if (g.equals("female") || g.equals("母") || g.equals("2")) {
                pet.setGenderCode(2);
            }
        }
        return petMapper.updateById(pet) > 0;
    }

    @Override
    public boolean deletePet(Long petId, Long userId) {
        Pet existPet = getPetDetail(petId, userId);
        if (existPet == null) {
            return false;
        }
        Pet pet = new Pet();
        pet.setId(petId);
        pet.setIsDeleted(1);  // 软删除
        pet.setStatus(0);
        pet.setUpdateTime(LocalDateTime.now());
        pet.setUpdatedTime(LocalDateTime.now());
        return petMapper.updateById(pet) > 0;
    }

    // ==================== 健康记录（就诊记录） ====================
    @Override
    public Page<MedicalRecord> getHealthRecords(Page<MedicalRecord> page, Long userId, Long petId, String type) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getOwnerId, userId);
        wrapper.eq(MedicalRecord::getIsDeleted, 0);
        if (petId != null) {
            wrapper.eq(MedicalRecord::getPetId, petId);
        }
        if (StringUtils.hasText(type)) {
            wrapper.eq(MedicalRecord::getType, type);
        }
        wrapper.orderByDesc(MedicalRecord::getCreateTime);
        return medicalRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public MedicalRecord getHealthRecordDetail(Long recordId, Long userId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getId, recordId);
        wrapper.eq(MedicalRecord::getOwnerId, userId);
        return medicalRecordMapper.selectOne(wrapper);
    }

    @Override
    public HealthRecordDetailVo getHealthRecordDetailWithPrescription(Long recordId, Long userId) {
        // 1. 查询病历详情（校验归属）
        MedicalRecord medicalRecord = getHealthRecordDetail(recordId, userId);
        if (medicalRecord == null) {
            return null;
        }

        HealthRecordDetailVo vo = new HealthRecordDetailVo();
        vo.setMedicalRecord(medicalRecord);

        // 2. 通过 recordId 查询关联处方（一一对应），查不到则降级用 registerId + petId
        List<Prescription> prescriptions = new ArrayList<>();
        
        // 优先用 recordId 查询
        if (medicalRecord.getId() != null) {
            LambdaQueryWrapper<Prescription> preWrapper = new LambdaQueryWrapper<>();
            preWrapper.eq(Prescription::getIsDeleted, 0);
            preWrapper.eq(Prescription::getRecordId, medicalRecord.getId());
            preWrapper.orderByDesc(Prescription::getCreateTime);
            prescriptions = prescriptionMapper.selectList(preWrapper);
        }
        
        // 如果 recordId 查不到，再用 registerId + petId 兜底查询
        if (prescriptions.isEmpty() && medicalRecord.getRegisterId() != null) {
            LambdaQueryWrapper<Prescription> fallbackWrapper = new LambdaQueryWrapper<>();
            fallbackWrapper.eq(Prescription::getIsDeleted, 0);
            fallbackWrapper.eq(Prescription::getRegisterId, medicalRecord.getRegisterId());
            if (medicalRecord.getPetId() != null) {
                fallbackWrapper.eq(Prescription::getPetId, medicalRecord.getPetId());
            }
            fallbackWrapper.orderByDesc(Prescription::getCreateTime);
            prescriptions = prescriptionMapper.selectList(fallbackWrapper);
        }

            List<HealthRecordDetailVo.PrescriptionWithItemsVo> preVoList = new ArrayList<>();
            for (Prescription pre : prescriptions) {
                HealthRecordDetailVo.PrescriptionWithItemsVo preVo = new HealthRecordDetailVo.PrescriptionWithItemsVo();
                preVo.setId(pre.getId());
                preVo.setPrescriptionNo(pre.getPrescriptionNo());
                preVo.setDiagnosis(pre.getDiagnosis());
                preVo.setTotalAmount(pre.getTotalAmount());
                preVo.setStatus(pre.getStatus());
                preVo.setRemark(pre.getRemark());
                preVo.setCreateTime(pre.getCreateTime());

                // 查询处方明细
                LambdaQueryWrapper<PrescriptionItem> itemWrapper = new LambdaQueryWrapper<>();
                itemWrapper.eq(PrescriptionItem::getPrescriptionId, pre.getId());
                itemWrapper.orderByAsc(PrescriptionItem::getSortNo);
                preVo.setItems(prescriptionItemMapper.selectList(itemWrapper));

                preVoList.add(preVo);
            }
            vo.setPrescriptions(preVoList);

        return vo;
    }

    // ==================== 自填记录 ====================
    @Override
    public Page<OwnerHealthRecord> getOwnerHealthRecords(Page<OwnerHealthRecord> page, Long userId, Long petId) {
        LambdaQueryWrapper<OwnerHealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OwnerHealthRecord::getOwnerId, userId);
        if (petId != null) {
            wrapper.eq(OwnerHealthRecord::getPetId, petId);
        }
        wrapper.orderByDesc(OwnerHealthRecord::getCreateTime);
        return ownerHealthRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public boolean addOwnerHealthRecord(OwnerHealthRecord record) {
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        return ownerHealthRecordMapper.insert(record) > 0;
    }

    @Override
    public boolean deleteOwnerHealthRecord(Long recordId, Long userId) {
        LambdaQueryWrapper<OwnerHealthRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OwnerHealthRecord::getId, recordId);
        wrapper.eq(OwnerHealthRecord::getOwnerId, userId);
        return ownerHealthRecordMapper.delete(wrapper) > 0;
    }

    // ==================== 预约管理 ====================
    @Override
    public Page<Appointment> getReserveList(Page<Appointment> page, Long userId, String status) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getOwnerId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Appointment::getStatus, status);
        }
        wrapper.orderByDesc(Appointment::getCreateTime);
        return appointmentMapper.selectPage(page, wrapper);
    }

    @Override
    public Appointment getReserveDetail(Long reserveId, Long userId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getId, reserveId);
        wrapper.eq(Appointment::getOwnerId, userId);
        return appointmentMapper.selectOne(wrapper);
    }

    @Override
    public boolean createReserve(Appointment appointment) {
        appointment.setAppointmentNo(generateReserveNo());
        appointment.setStatus("0");
        appointment.setStatusText("pending");
        appointment.setSourceType("OWNER");
        // 兼容：如果 ownerUserId 为空但 ownerId 有值，自动同步
        if (appointment.getOwnerUserId() == null && appointment.getOwnerId() != null) {
            appointment.setOwnerUserId(appointment.getOwnerId());
        }
        appointment.setCreateTime(LocalDateTime.now());
        appointment.setUpdateTime(LocalDateTime.now());
        return appointmentMapper.insert(appointment) > 0;
    }

    @Override
    public boolean cancelReserve(Long reserveId, Long userId, String cancelReason) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getId, reserveId);
        wrapper.eq(Appointment::getOwnerId, userId);
        Appointment appointment = appointmentMapper.selectOne(wrapper);
        // 修复：兼容 "0"（Owner端创建）和 "pending"（Desk端创建）两种待确认状态
        String status = appointment == null ? null : appointment.getStatus();
        if (appointment == null || !("0".equals(status) || "pending".equals(status))) {
            return false;
        }
        // 统一使用 "3" 表示已取消，与前端状态定义一致
        appointment.setStatus("3");
        appointment.setCancelReason(cancelReason);
        appointment.setUpdateTime(LocalDateTime.now());
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public boolean confirmReserve(Long reserveId, Long userId) {
        LambdaQueryWrapper<Appointment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Appointment::getId, reserveId);
        wrapper.eq(Appointment::getOwnerId, userId);
        Appointment appointment = appointmentMapper.selectOne(wrapper);
        // 只允许确认状态为 "0" 或 "pending" 的预约
        String status = appointment == null ? null : appointment.getStatus();
        if (appointment == null || !("0".equals(status) || "pending".equals(status))) {
            return false;
        }
        appointment.setStatus("1");
        appointment.setUpdateTime(LocalDateTime.now());
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public List<Map<String, Object>> getAvailableDoctors(String serviceType, String date) {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            // 查询真实数据库中的医生列表（排除已休息的）
            List<Map<String, Object>> doctors = jdbcTemplate.queryForList(
                "select id, name, title from doctor_profile where is_deleted = 0 and (work_status is null or work_status != 0) and (consult_visible is null or consult_visible = 1) order by id"
            );
            for (Map<String, Object> d : doctors) {
                Map<String, Object> doctor = new HashMap<>();
                doctor.put("id", d.get("id"));
                doctor.put("name", d.get("name"));
                Object title = d.get("title");
                doctor.put("title", title != null ? title : "Doctor");
                result.add(doctor);
            }
        } catch (Exception ex) {
            // 若数据库查询失败，降级为假数据，保证前端可用
        }
        if (result.isEmpty()) {
            Map<String, Object> doctor1 = new HashMap<>();
            doctor1.put("id", 1L);
            doctor1.put("name", "Dr. Zhang");
            doctor1.put("title", "Attending Physician");
            result.add(doctor1);

            Map<String, Object> doctor2 = new HashMap<>();
            doctor2.put("id", 2L);
            doctor2.put("name", "Dr. Li");
            doctor2.put("title", "Practicing Physician");
            result.add(doctor2);

            Map<String, Object> doctor3 = new HashMap<>();
            doctor3.put("id", 3L);
            doctor3.put("name", "Dr. Wang");
            doctor3.put("title", "Chief Physician");
            result.add(doctor3);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getAvailableTimeSlots(Long doctorId, String date) {
        List<Map<String, Object>> slots = new ArrayList<>();
        String[] allTimes = {"09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};
        Set<String> bookedTimes = new HashSet<>();

        if (doctorId != null && date != null && !date.isEmpty()) {
            try {
                // 查询该医生该日期已被预约（待确认/已确认/已核销）的时间段
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                    "select appointment_time from appointment where doctor_id = ? and date(appointment_time) = ? and status in (0,1,2) and is_deleted = 0",
                    doctorId, date
                );
                for (Map<String, Object> row : rows) {
                    Object timeObj = row.get("appointment_time");
                    if (timeObj != null) {
                        String timeStr = timeObj.toString();
                        // 提取 HH:mm，兼容 "2024-01-01 09:00:00" 和带 T 的 ISO 格式
                        if (timeStr.length() >= 16) {
                            bookedTimes.add(timeStr.substring(11, 16));
                        } else if (timeStr.contains("T") && timeStr.length() >= 16) {
                            bookedTimes.add(timeStr.substring(11, 16));
                        }
                    }
                }
            } catch (Exception ex) {
                // 查询失败时不阻断，全部返回可预约
            }
        }

        for (String time : allTimes) {
            Map<String, Object> slot = new HashMap<>();
            slot.put("time", time);
            slot.put("available", !bookedTimes.contains(time));
            slots.add(slot);
        }
        return slots;
    }

    private String generateReserveNo() {
        return "R" + System.currentTimeMillis();
    }

    // ==================== 在线咨询 ====================
    @Override
    public Page<Consultation> getConsultList(Page<Consultation> page, Long userId, String status) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getOwnerId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Consultation::getStatus, status);
        }
        wrapper.orderByDesc(Consultation::getCreateTime);
        Page<Consultation> result = consultationMapper.selectPage(page, wrapper);
        // 补充宠物名、医生名、回复数
        for (Consultation c : result.getRecords()) {
            if (c.getPetId() != null) {
                Pet pet = petMapper.selectById(c.getPetId());
                c.setPetName(pet != null ? pet.getName() : null);
            }
            if (c.getDoctorId() != null) {
                Doctor doctor = doctorMapper.selectById(c.getDoctorId());
                c.setDoctorName(doctor != null ? doctor.getName() : null);
            }
            LambdaQueryWrapper<ConsultationReply> rc = new LambdaQueryWrapper<>();
            rc.eq(ConsultationReply::getConsultationId, c.getId());
            Long count = consultationReplyMapper.selectCount(rc);
            c.setReplyCount(count == null ? 0 : count.intValue());
        }
        return result;
    }

    @Override
    public Consultation getConsultDetail(Long consultId, Long userId) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getId, consultId);
        wrapper.eq(Consultation::getOwnerId, userId);
        Consultation consultation = consultationMapper.selectOne(wrapper);
        
        if (consultation != null) {
            if (consultation.getPetId() != null) {
                Pet pet = petMapper.selectById(consultation.getPetId());
                consultation.setPetName(pet != null ? pet.getName() : null);
            }
            if (consultation.getDoctorId() != null) {
                Doctor doctor = doctorMapper.selectById(consultation.getDoctorId());
                consultation.setDoctorName(doctor != null ? doctor.getName() : null);
            }
            LambdaQueryWrapper<ConsultationReply> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(ConsultationReply::getConsultationId, consultId);
            replyWrapper.orderByAsc(ConsultationReply::getCreateTime);
            List<ConsultationReply> replyList = consultationReplyMapper.selectList(replyWrapper);
            // 补充 senderName
            for (ConsultationReply reply : replyList) {
                if ("doctor".equals(reply.getSenderType())) {
                    Doctor d = doctorMapper.selectById(reply.getSenderId());
                    reply.setSenderName(d != null ? d.getName() : "Doctor");
                } else {
                    User u = userMapper.selectById(reply.getSenderId());
                    reply.setSenderName(u != null ? (u.getRealName() != null ? u.getRealName() : u.getUsername()) : "Owner");
                }
            }
            consultation.setReplies(replyList);
            consultation.setReplyCount(replyList.size());
        }
        return consultation;
    }

    @Override
    public boolean createConsult(Consultation consultation) {
        consultation.setConsultationNo(generateConsultNo());
        consultation.setStatus("ongoing");
        consultation.setCreateTime(LocalDateTime.now());
        consultation.setUpdateTime(LocalDateTime.now());
        return consultationMapper.insert(consultation) > 0;
    }

    @Override
    public boolean replyConsult(Long consultId, Long userId, String content) {
        ConsultationReply reply = new ConsultationReply();
        reply.setConsultationId(consultId);
        reply.setSenderType("owner");
        reply.setSenderId(userId);
        reply.setContent(content);
        reply.setIsRead(0);
        reply.setCreateTime(LocalDateTime.now());
        return consultationReplyMapper.insert(reply) > 0;
    }

    @Override
    public boolean rateConsult(Long consultId, Long userId, Integer rating, String comment) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getId, consultId);
        wrapper.eq(Consultation::getOwnerId, userId);
        Consultation consultation = consultationMapper.selectOne(wrapper);
        if (consultation == null) {
            return false;
        }
        consultation.setRating(rating);
        consultation.setRatingComment(comment);
        consultation.setStatus("completed");
        consultation.setUpdateTime(LocalDateTime.now());
        return consultationMapper.updateById(consultation) > 0;
    }

    private String generateConsultNo() {
        return "C" + System.currentTimeMillis();
    }

    // ==================== 订单管理 ====================
    // ==================== 订单管理 ====================
    @Override
    public Page<OrderInfo> getOrderList(Page<OrderInfo> page, Long userId, String payStatus) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getOwnerUserId, userId);
        if (StringUtils.hasText(payStatus)) {
            wrapper.eq(OrderInfo::getPayStatusText, payStatus);
        }
        wrapper.orderByDesc(OrderInfo::getCreateTime);
        Page<OrderInfo> orderPage = orderInfoMapper.selectPage(page, wrapper);
        
        // 为每个订单补充宠物名称
        if (orderPage.getRecords() != null && !orderPage.getRecords().isEmpty()) {
            for (OrderInfo order : orderPage.getRecords()) {
                if (order.getPetId() != null) {
                    Pet pet = petMapper.selectById(order.getPetId());
                    if (pet != null) {
                        order.setPetName(pet.getName());
                    }
                }
            }
        }
        
        return orderPage;
    }

    @Override
    public OrderInfo getOrderDetail(Long orderId, Long userId) {
        LambdaQueryWrapper<OrderInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderInfo::getId, orderId);
        wrapper.eq(OrderInfo::getOwnerUserId, userId);
        OrderInfo order = orderInfoMapper.selectOne(wrapper);
        
        if (order != null) {
            // 补充宠物名称
            if (order.getPetId() != null) {
                Pet pet = petMapper.selectById(order.getPetId());
                if (pet != null) {
                    order.setPetName(pet.getName());
                }
            }
            
            // 查询订单明细
            LambdaQueryWrapper<OrderItem> itemWrapper = new LambdaQueryWrapper<>();
            itemWrapper.eq(OrderItem::getOrderId, orderId);
            List<OrderItem> items = orderItemMapper.selectList(itemWrapper);
            order.setOrderItems(items);
        }
        return order;
    }

    // ==================== 个人中心 ====================
    @Override
    public User getUserInfo(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public boolean updateUserInfo(User user) {
        // 使用 UpdateWrapper 只更新非 null 字段，避免前端未返回的字段覆盖数据库原值
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId());
        boolean hasUpdate = false;
        if (user.getRealName() != null) {
            updateWrapper.set(User::getRealName, user.getRealName());
            hasUpdate = true;
        }
        if (user.getPhone() != null) {
            updateWrapper.set(User::getPhone, user.getPhone());
            hasUpdate = true;
        }
        if (user.getEmail() != null) {
            updateWrapper.set(User::getEmail, user.getEmail());
            hasUpdate = true;
        }
        if (user.getAvatar() != null) {
            updateWrapper.set(User::getAvatar, user.getAvatar());
            hasUpdate = true;
        }
        if (user.getGender() != null) {
            updateWrapper.set(User::getGender, user.getGender());
            hasUpdate = true;
        }
        if (!hasUpdate) {
            return true;
        }
        updateWrapper.set(User::getUpdateTime, LocalDateTime.now());
        boolean success = userMapper.update(null, updateWrapper) > 0;
        
        // 同步更新 doctor_profile.name（如果该用户是医生）
        if (success && user.getRealName() != null && !user.getRealName().isEmpty() && user.getId() != null) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, user.getId());
            wrapper.last("LIMIT 1");
            Doctor doctor = doctorMapper.selectOne(wrapper);
            if (doctor != null) {
                LambdaUpdateWrapper<Doctor> doctorUpdateWrapper = new LambdaUpdateWrapper<>();
                doctorUpdateWrapper.eq(Doctor::getId, doctor.getId());
                doctorUpdateWrapper.set(Doctor::getName, user.getRealName());
                doctorUpdateWrapper.set(Doctor::getUpdateTime, LocalDateTime.now());
                doctorMapper.update(null, doctorUpdateWrapper);
            }
        }
        
        return success;
    }

    @Override
    public boolean changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null || !EncryptUtil.matchesPassword(oldPassword, user.getPassword())) {
            return false;
        }
        user.setPassword(EncryptUtil.encodePassword(newPassword));
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean bindPhone(Long userId, String phone, String code) {
        User user = new User();
        user.setId(userId);
        user.setPhone(phone);
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }

    @Override
    public boolean bindEmail(Long userId, String email, String code) {
        User user = new User();
        user.setId(userId);
        user.setEmail(email);
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
    }
}