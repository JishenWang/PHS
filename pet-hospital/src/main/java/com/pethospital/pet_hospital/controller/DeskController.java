package com.pethospital.pet_hospital.controller;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pethospital.pet_hospital.dto.desk.ChargeQueryDto;
import com.pethospital.pet_hospital.dto.desk.CustomerQueryDto;
import com.pethospital.pet_hospital.dto.desk.RegisterQueryDto;
import com.pethospital.pet_hospital.entity.Feedback;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.FeedbackMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IDeskService;
import com.pethospital.pet_hospital.vo.common.ResultVo;

@RestController
@RequestMapping("/desk")
public class DeskController {
    @Autowired
    private IDeskService deskService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private FeedbackMapper feedbackMapper;

    /**
     * 前台端健康检查接口（占位）
     */
    @GetMapping("/ping")
    public Map<String, Object> ping() {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("module", "desk");
        result.put("message", deskService.ping());
        return result;
    }

    @GetMapping("/customers")
    public Map<String, Object> customers(CustomerQueryDto queryDto) {
        return deskService.queryCustomers(queryDto);
    }

    @GetMapping("/registers")
    public Map<String, Object> registers(RegisterQueryDto queryDto) {
        return deskService.queryRegisters(queryDto);
    }

    @GetMapping("/charges")
    public Map<String, Object> charges(ChargeQueryDto queryDto) {
        return deskService.queryCharges(queryDto);
    }

    @GetMapping("/debug/table-stats")
    public Map<String, Object> debugTableStats() {
        return deskService.debugTableStats();
    }

    @PutMapping("/registers/{id}/status")
    public Map<String, Object> updateRegisterStatus(@PathVariable("id") Long id,
                                                    @RequestBody Map<String, Object> body) {
        String status = body == null ? null : String.valueOf(body.get("status"));
        return deskService.updateRegisterStatus(id, status);
    }

    @PostMapping("/charges/{id}/confirm")
    public Map<String, Object> confirmCharge(@PathVariable("id") Long id,
                                             @RequestBody Map<String, Object> body) {
        String payMethod = body == null ? null : String.valueOf(body.get("payMethod"));
        return deskService.confirmCharge(id, payMethod);
    }

    @PostMapping("/customers/temp")
    public Map<String, Object> createTempCustomer(@RequestBody Map<String, Object> body) {
        return deskService.createTempCustomer(body);
    }

    @GetMapping("/reserves")
    public Map<String, Object> reserves(@RequestParam Map<String, Object> params) {
        return deskService.getReserveList(params);
    }

    @PutMapping("/reserves/{id}/confirm")
    public Map<String, Object> confirmReserve(@PathVariable("id") Long id) {
        return deskService.confirmReserve(id);
    }

    @PostMapping("/reserves/{id}/verify")
    public Map<String, Object> verifyReserve(@PathVariable("id") Long id) {
        return deskService.verifyReserve(id);
    }

    @PostMapping("/registers")
    public Map<String, Object> createRegister(@RequestBody Map<String, Object> body) {
        return deskService.createRegister(body);
    }

    @PutMapping("/charges/{id}/pricing")
    public Map<String, Object> updateChargePricing(@PathVariable("id") Long id,
                                                   @RequestBody Map<String, Object> body) {
        return deskService.updateChargePricing(id, body);
    }

    @PostMapping("/charges/{id}/refund")
    public Map<String, Object> applyRefund(@PathVariable("id") Long id,
                                           @RequestBody Map<String, Object> body) {
        String reason = body == null ? "" : String.valueOf(body.get("reason"));
        return deskService.applyRefund(id, reason);
    }

    @GetMapping("/orders")
    public Map<String, Object> orders(@RequestParam Map<String, Object> params) {
        return deskService.getOrderList(params);
    }

    @GetMapping("/doctors/status")
    public Map<String, Object> doctorStatus() {
        return deskService.getDoctorStatusList();
    }

    @GetMapping("/today-stats")
    public Map<String, Object> todayStats() {
        return deskService.getTodayStats();
    }

    // ==================== 系统设置 ====================

    @PostMapping("/feedback")
    public ResultVo<String> submitFeedback(@RequestBody Map<String, String> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "desk";

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        wrapper.last("LIMIT 1");
        User user = userMapper.selectOne(wrapper);

        Feedback feedback = new Feedback();
        feedback.setUserId(user != null ? user.getId() : null);
        feedback.setUserName(user != null ? user.getRealName() : username);
        feedback.setType(body.get("type"));
        feedback.setContent(body.get("content"));
        feedback.setContact(body.get("contact"));
        feedback.setCreateTime(LocalDateTime.now());
        feedbackMapper.insert(feedback);

        return ResultVo.success("Feedback submitted");
    }

    @GetMapping("/version")
    public ResultVo<Map<String, Object>> checkVersion() {
        Map<String, Object> data = new HashMap<>();
        data.put("version", "v1.0.0");
        data.put("buildDate", "2026-04-12");
        data.put("forceUpdate", false);
        data.put("updateUrl", "");
        data.put("updateLog", "Already up to date");
        return ResultVo.success(data);
    }
}
