package com.pethospital.pet_hospital.service;

import java.util.List;
import java.util.Map;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.entity.Appointment;
import com.pethospital.pet_hospital.entity.Consultation;
import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.Order;
import com.pethospital.pet_hospital.entity.OwnerHealthRecord;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.User;

public interface IOwnerService {

    // ==================== 宠物管理 ====================
    Page<Pet> getPetList(Page<Pet> page, Long userId, String keyword);
    Pet getPetDetail(Long petId, Long userId);
    boolean addPet(Pet pet);
    boolean updatePet(Pet pet, Long userId);
    boolean deletePet(Long petId, Long userId);

    // ==================== 健康记录（就诊记录） ====================
    Page<MedicalRecord> getHealthRecords(Page<MedicalRecord> page, Long userId, Long petId, String type);
    MedicalRecord getHealthRecordDetail(Long recordId, Long userId);

    // ==================== 自填记录 ====================
    Page<OwnerHealthRecord> getOwnerHealthRecords(Page<OwnerHealthRecord> page, Long userId, Long petId);
    boolean addOwnerHealthRecord(OwnerHealthRecord record);
    boolean deleteOwnerHealthRecord(Long recordId, Long userId);

    // ==================== 预约管理 ====================
    Page<Appointment> getReserveList(Page<Appointment> page, Long userId, String status);
    Appointment getReserveDetail(Long reserveId, Long userId);
    boolean createReserve(Appointment appointment);
    boolean cancelReserve(Long reserveId, Long userId, String cancelReason);
    List<Map<String, Object>> getAvailableDoctors(String serviceType, String date);
    List<Map<String, Object>> getAvailableTimeSlots(Long doctorId, String date);

    // ==================== 在线咨询 ====================
    Page<Consultation> getConsultList(Page<Consultation> page, Long userId, String status);
    Consultation getConsultDetail(Long consultId, Long userId);
    boolean createConsult(Consultation consultation);
    boolean replyConsult(Long consultId, Long userId, String content);
    boolean rateConsult(Long consultId, Long userId, Integer rating, String comment);

    // ==================== 订单管理 ====================
    Page<Order> getOrderList(Page<Order> page, Long userId, String payStatus);
    Order getOrderDetail(Long orderId, Long userId);

    // ==================== 个人中心 ====================
    User getUserInfo(Long userId);
    boolean updateUserInfo(User user);
    boolean changePassword(Long userId, String oldPassword, String newPassword);
    boolean bindPhone(Long userId, String phone, String code);
    boolean bindEmail(Long userId, String email, String code);
}