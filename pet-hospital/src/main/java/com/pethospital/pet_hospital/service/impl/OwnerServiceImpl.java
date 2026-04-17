package com.pethospital.pet_hospital.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.entity.Appointment;
import com.pethospital.pet_hospital.entity.Consultation;
import com.pethospital.pet_hospital.entity.ConsultationReply;
import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.OrderInfo;
import com.pethospital.pet_hospital.entity.OrderItem;
import com.pethospital.pet_hospital.entity.OwnerHealthRecord;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.AppointmentMapper;
import com.pethospital.pet_hospital.mapper.ConsultationMapper;
import com.pethospital.pet_hospital.mapper.ConsultationReplyMapper;
import com.pethospital.pet_hospital.mapper.MedicalRecordMapper;
import com.pethospital.pet_hospital.mapper.OrderInfoMapper;
import com.pethospital.pet_hospital.mapper.OrderItemMapper;
import com.pethospital.pet_hospital.mapper.OwnerHealthRecordMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IOwnerService;
import com.pethospital.pet_hospital.utils.EncryptUtil;

@Service
public class OwnerServiceImpl implements IOwnerService {

    @Autowired
    private PetMapper petMapper;
    @Autowired
    private MedicalRecordMapper medicalRecordMapper;
    @Autowired
    private OwnerHealthRecordMapper ownerHealthRecordMapper;
    @Autowired
    private AppointmentMapper appointmentMapper;
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
        return petMapper.insert(pet) > 0;
    }

    @Override
    public boolean updatePet(Pet pet, Long userId) {
        Pet existPet = getPetDetail(pet.getId(), userId);
        if (existPet == null) {
            return false;
        }
        pet.setUpdateTime(LocalDateTime.now());
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
        pet.setStatus(0);
        pet.setUpdateTime(LocalDateTime.now());
        return petMapper.updateById(pet) > 0;
    }

    // ==================== 健康记录（就诊记录） ====================
    @Override
    public Page<MedicalRecord> getHealthRecords(Page<MedicalRecord> page, Long userId, Long petId, String type) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getOwnerId, userId);
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
        if (appointment == null || !"pending".equals(appointment.getStatus())) {
            return false;
        }
        appointment.setStatus("cancelled");
        appointment.setCancelReason(cancelReason);
        appointment.setUpdateTime(LocalDateTime.now());
        return appointmentMapper.updateById(appointment) > 0;
    }

    @Override
    public List<Map<String, Object>> getAvailableDoctors(String serviceType, String date) {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> doctor1 = new HashMap<>();
        doctor1.put("id", 1L);
        doctor1.put("name", "张医生");
        doctor1.put("title", "主治医师");
        result.add(doctor1);
        
        Map<String, Object> doctor2 = new HashMap<>();
        doctor2.put("id", 2L);
        doctor2.put("name", "李医生");
        doctor2.put("title", "执业医师");
        result.add(doctor2);
        
        Map<String, Object> doctor3 = new HashMap<>();
        doctor3.put("id", 3L);
        doctor3.put("name", "王医生");
        doctor3.put("title", "主任医师");
        result.add(doctor3);
        return result;
    }

    @Override
    public List<Map<String, Object>> getAvailableTimeSlots(Long doctorId, String date) {
        List<Map<String, Object>> slots = new ArrayList<>();
        String[] times = {"09:00", "10:00", "11:00", "14:00", "15:00", "16:00"};
        for (String time : times) {
            Map<String, Object> slot = new HashMap<>();
            slot.put("time", time);
            slot.put("available", true);
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
        return consultationMapper.selectPage(page, wrapper);
    }

    @Override
    public Consultation getConsultDetail(Long consultId, Long userId) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Consultation::getId, consultId);
        wrapper.eq(Consultation::getOwnerId, userId);
        Consultation consultation = consultationMapper.selectOne(wrapper);
        
        if (consultation != null) {
            LambdaQueryWrapper<ConsultationReply> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(ConsultationReply::getConsultationId, consultId);
            replyWrapper.orderByAsc(ConsultationReply::getCreateTime);
            List<ConsultationReply> replies = consultationReplyMapper.selectList(replyWrapper);
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
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.updateById(user) > 0;
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