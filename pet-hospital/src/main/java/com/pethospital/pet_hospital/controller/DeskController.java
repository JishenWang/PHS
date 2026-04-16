package com.pethospital.pet_hospital.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pethospital.pet_hospital.dto.desk.ChargeQueryDto;
import com.pethospital.pet_hospital.dto.desk.CustomerQueryDto;
import com.pethospital.pet_hospital.dto.desk.RegisterQueryDto;
import com.pethospital.pet_hospital.service.IDeskService;

@RestController
@RequestMapping("/desk")
public class DeskController {
    @Autowired
    private IDeskService deskService;

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
}
