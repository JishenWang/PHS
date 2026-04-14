package com.pethospital.pet_hospital.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.common.constant.HttpStatus;
import com.pethospital.pet_hospital.entity.Appointment;
import com.pethospital.pet_hospital.entity.Consultation;
import com.pethospital.pet_hospital.entity.MedicalRecord;
import com.pethospital.pet_hospital.entity.Order;
import com.pethospital.pet_hospital.entity.OwnerHealthRecord;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.service.IOwnerService;
import com.pethospital.pet_hospital.vo.common.PageResultVo;
import com.pethospital.pet_hospital.vo.common.ResultVo;

/**
 * 客户自助端控制器
 */
@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    @Autowired
    private IOwnerService ownerService;

    /**
     * 获取当前登录用户ID
     */
    private Long getCurrentUserId() {
        // 临时返回，实际应从token中获取
        return 1L;
    }

    // ==================== 宠物管理 ====================

    @GetMapping("/pet/list")
    public ResultVo<PageResultVo<Pet>> getPetList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Long userId = getCurrentUserId();
        Page<Pet> pageParam = new Page<>(page, pageSize);
        Page<Pet> result = ownerService.getPetList(pageParam, userId, keyword);
        return ResultVo.success(PageResultVo.success(
            result.getRecords(), 
            result.getTotal(), 
            (long)page, 
            (long)pageSize));
    }

    @GetMapping("/pet/{petId}")
    public ResultVo<Pet> getPetDetail(@PathVariable Long petId) {
        Long userId = getCurrentUserId();
        Pet pet = ownerService.getPetDetail(petId, userId);
        if (pet == null) {
            return ResultVo.error(HttpStatus.NOT_FOUND, "宠物不存在");
        }
        return ResultVo.success(pet);
    }

    @PostMapping("/pet")
    public ResultVo<Void> addPet(@RequestBody Pet pet) {
        Long userId = getCurrentUserId();
        pet.setOwnerId(userId);
        boolean success = ownerService.addPet(pet);
        return success ? ResultVo.success() : ResultVo.error("添加失败");
    }

    @PutMapping("/pet/{petId}")
    public ResultVo<Void> updatePet(@PathVariable Long petId, @RequestBody Pet pet) {
        Long userId = getCurrentUserId();
        pet.setId(petId);
        boolean success = ownerService.updatePet(pet, userId);
        return success ? ResultVo.success() : ResultVo.error("更新失败");
    }

    @DeleteMapping("/pet/{petId}")
    public ResultVo<Void> deletePet(@PathVariable Long petId) {
        Long userId = getCurrentUserId();
        boolean success = ownerService.deletePet(petId, userId);
        return success ? ResultVo.success() : ResultVo.error("删除失败");
    }

    // ==================== 健康记录（就诊记录） ====================

    @GetMapping("/health/list")
    public ResultVo<PageResultVo<MedicalRecord>> getHealthRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long petId,
            @RequestParam(required = false) String type) {
        Long userId = getCurrentUserId();
        Page<MedicalRecord> pageParam = new Page<>(page, pageSize);
        Page<MedicalRecord> result = ownerService.getHealthRecords(pageParam, userId, petId, type);
        return ResultVo.success(PageResultVo.success(
            result.getRecords(), 
            result.getTotal(), 
            (long)page, 
            (long)pageSize));
    }

    @GetMapping("/health/{recordId}")
    public ResultVo<MedicalRecord> getHealthRecordDetail(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        MedicalRecord record = ownerService.getHealthRecordDetail(recordId, userId);
        if (record == null) {
            return ResultVo.error(HttpStatus.NOT_FOUND, "记录不存在");
        }
        return ResultVo.success(record);
    }

    // ==================== 自填记录 ====================

    @GetMapping("/owner-health/list")
    public ResultVo<PageResultVo<OwnerHealthRecord>> getOwnerHealthRecords(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long petId) {
        Long userId = getCurrentUserId();
        Page<OwnerHealthRecord> pageParam = new Page<>(page, pageSize);
        Page<OwnerHealthRecord> result = ownerService.getOwnerHealthRecords(pageParam, userId, petId);
        return ResultVo.success(PageResultVo.success(
            result.getRecords(), 
            result.getTotal(), 
            (long)page, 
            (long)pageSize));
    }

    @PostMapping("/owner-health")
    public ResultVo<Void> addOwnerHealthRecord(@RequestBody OwnerHealthRecord record) {
        Long userId = getCurrentUserId();
        record.setOwnerId(userId);
        boolean success = ownerService.addOwnerHealthRecord(record);
        return success ? ResultVo.success() : ResultVo.error("添加失败");
    }

    @DeleteMapping("/owner-health/{recordId}")
    public ResultVo<Void> deleteOwnerHealthRecord(@PathVariable Long recordId) {
        Long userId = getCurrentUserId();
        boolean success = ownerService.deleteOwnerHealthRecord(recordId, userId);
        return success ? ResultVo.success() : ResultVo.error("删除失败");
    }

    // ==================== 预约管理 ====================

    @GetMapping("/reserve/list")
    public ResultVo<PageResultVo<Appointment>> getReserveList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        Page<Appointment> pageParam = new Page<>(page, pageSize);
        Page<Appointment> result = ownerService.getReserveList(pageParam, userId, status);
        return ResultVo.success(PageResultVo.success(
            result.getRecords(), 
            result.getTotal(), 
            (long)page, 
            (long)pageSize));
    }

    @GetMapping("/reserve/{reserveId}")
    public ResultVo<Appointment> getReserveDetail(@PathVariable Long reserveId) {
        Long userId = getCurrentUserId();
        Appointment appointment = ownerService.getReserveDetail(reserveId, userId);
        if (appointment == null) {
            return ResultVo.error(HttpStatus.NOT_FOUND, "预约不存在");
        }
        return ResultVo.success(appointment);
    }

    @PostMapping("/reserve")
    public ResultVo<Void> createReserve(@RequestBody Appointment appointment) {
        Long userId = getCurrentUserId();
        appointment.setOwnerId(userId);
        boolean success = ownerService.createReserve(appointment);
        return success ? ResultVo.success() : ResultVo.error("创建失败");
    }

    @PutMapping("/reserve/{reserveId}/cancel")
    public ResultVo<Void> cancelReserve(@PathVariable Long reserveId, @RequestBody(required = false) Map<String, String> body) {
        Long userId = getCurrentUserId();
        String cancelReason = body != null ? body.get("cancelReason") : null;
        boolean success = ownerService.cancelReserve(reserveId, userId, cancelReason);
        return success ? ResultVo.success() : ResultVo.error("取消失败");
    }

    @GetMapping("/reserve/doctors")
    public ResultVo<Object> getAvailableDoctors(
            @RequestParam(required = false) String serviceType,
            @RequestParam(required = false) String date) {
        return ResultVo.success(ownerService.getAvailableDoctors(serviceType, date));
    }

    @GetMapping("/reserve/timeslots")
    public ResultVo<Object> getAvailableTimeSlots(
            @RequestParam Long doctorId,
            @RequestParam String date) {
        return ResultVo.success(ownerService.getAvailableTimeSlots(doctorId, date));
    }

    // ==================== 在线咨询 ====================

    @GetMapping("/consult/list")
    public ResultVo<PageResultVo<Consultation>> getConsultList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Long userId = getCurrentUserId();
        Page<Consultation> pageParam = new Page<>(page, pageSize);
        Page<Consultation> result = ownerService.getConsultList(pageParam, userId, status);
        return ResultVo.success(PageResultVo.success(
            result.getRecords(), 
            result.getTotal(), 
            (long)page, 
            (long)pageSize));
    }

    @GetMapping("/consult/{consultId}")
    public ResultVo<Consultation> getConsultDetail(@PathVariable Long consultId) {
        Long userId = getCurrentUserId();
        Consultation consultation = ownerService.getConsultDetail(consultId, userId);
        if (consultation == null) {
            return ResultVo.error(HttpStatus.NOT_FOUND, "咨询不存在");
        }
        return ResultVo.success(consultation);
    }

    @PostMapping("/consult")
    public ResultVo<Void> createConsult(@RequestBody Consultation consultation) {
        Long userId = getCurrentUserId();
        consultation.setOwnerId(userId);
        boolean success = ownerService.createConsult(consultation);
        return success ? ResultVo.success() : ResultVo.error("发起失败");
    }

    @PostMapping("/consult/{consultId}/reply")
    public ResultVo<Void> replyConsult(@PathVariable Long consultId, @RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String content = body.get("content");
        boolean success = ownerService.replyConsult(consultId, userId, content);
        return success ? ResultVo.success() : ResultVo.error("回复失败");
    }

    @PostMapping("/consult/{consultId}/rate")
    public ResultVo<Void> rateConsult(@PathVariable Long consultId, @RequestBody Map<String, Object> body) {
        Long userId = getCurrentUserId();
        Integer rating = (Integer) body.get("rating");
        String comment = (String) body.get("comment");
        boolean success = ownerService.rateConsult(consultId, userId, rating, comment);
        return success ? ResultVo.success() : ResultVo.error("评价失败");
    }

    // ==================== 订单管理 ====================

    @GetMapping("/order/list")
    public ResultVo<PageResultVo<Order>> getOrderList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String payStatus) {
        Long userId = getCurrentUserId();
        Page<Order> pageParam = new Page<>(page, pageSize);
        Page<Order> result = ownerService.getOrderList(pageParam, userId, payStatus);
        return ResultVo.success(PageResultVo.success(
            result.getRecords(), 
            result.getTotal(), 
            (long)page, 
            (long)pageSize));
    }

    @GetMapping("/order/{orderId}")
    public ResultVo<Order> getOrderDetail(@PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        Order order = ownerService.getOrderDetail(orderId, userId);
        if (order == null) {
            return ResultVo.error(HttpStatus.NOT_FOUND, "订单不存在");
        }
        return ResultVo.success(order);
    }

    // ==================== 个人中心 ====================

    @GetMapping("/profile")
    public ResultVo<User> getUserInfo() {
        Long userId = getCurrentUserId();
        User user = ownerService.getUserInfo(userId);
        return ResultVo.success(user);
    }

    @PutMapping("/profile")
    public ResultVo<Void> updateUserInfo(@RequestBody User user) {
        Long userId = getCurrentUserId();
        user.setId(userId);
        boolean success = ownerService.updateUserInfo(user);
        return success ? ResultVo.success() : ResultVo.error("更新失败");
    }

    @PostMapping("/profile/password")
    public ResultVo<Void> changePassword(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");
        boolean success = ownerService.changePassword(userId, oldPassword, newPassword);
        return success ? ResultVo.success() : ResultVo.error("修改失败");
    }

    @PostMapping("/profile/phone")
    public ResultVo<Void> bindPhone(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String phone = body.get("phone");
        String code = body.get("code");
        boolean success = ownerService.bindPhone(userId, phone, code);
        return success ? ResultVo.success() : ResultVo.error("绑定失败");
    }

    @PostMapping("/profile/email")
    public ResultVo<Void> bindEmail(@RequestBody Map<String, String> body) {
        Long userId = getCurrentUserId();
        String email = body.get("email");
        String code = body.get("code");
        boolean success = ownerService.bindEmail(userId, email, code);
        return success ? ResultVo.success() : ResultVo.error("绑定失败");
    }
}