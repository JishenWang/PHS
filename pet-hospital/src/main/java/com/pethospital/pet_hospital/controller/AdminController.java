package com.pethospital.pet_hospital.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pethospital.pet_hospital.service.IAdminService;
import com.pethospital.pet_hospital.vo.admin.DashboardVo;
import com.pethospital.pet_hospital.vo.admin.DoctorVo;
import com.pethospital.pet_hospital.vo.admin.PetVo;
import com.pethospital.pet_hospital.vo.admin.UserVo;
import com.pethospital.pet_hospital.vo.common.ResultVo;

@RestController
@RequestMapping("/admin")
@CrossOrigin
public class AdminController {

    @Autowired
    private IAdminService adminService;

    @GetMapping("/dashboard")
    public ResultVo<DashboardVo> getDashboardData() {
        return ResultVo.success(adminService.getDashboardData());
    }

    @GetMapping("/dashboard/trend")
    public ResultVo<List<Map<String, Object>>> getTrendData(@RequestParam String period) {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] dates = {"周一", "周二", "周三", "周四", "周五", "周六", "周日"};
        for (String date : dates) {
            Map<String, Object> item = new HashMap<>();
            item.put("date", date);
            item.put("appointments", (int)(Math.random() * 50 + 20));
            item.put("registrations", (int)(Math.random() * 30 + 10));
            list.add(item);
        }
        return ResultVo.success(list);
    }

    @GetMapping("/dashboard/pet-distribution")
    public ResultVo<List<Map<String, Object>>> getPetTypeDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] types = {"犬类", "猫类", "兔类", "其他"};
        int[] values = {45, 35, 12, 8};
        for (int i = 0; i < types.length; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("name", types[i]);
            item.put("value", values[i]);
            list.add(item);
        }
        return ResultVo.success(list);
    }

    @GetMapping("/dashboard/activities")
    public ResultVo<List<Map<String, Object>>> getRecentActivities() {
        List<Map<String, Object>> list = new ArrayList<>();
        String[] actions = {"新增预约", "完成就诊", "取消预约", "新增挂号", "收费完成"};
        String[] users = {"张三", "李四", "王五", "赵六", "孙七"};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> item = new HashMap<>();
            item.put("id", i + 1);
            item.put("user", users[i]);
            item.put("action", actions[i]);
            item.put("time", "2026-04-14 " + (10 + i) + ":30");
            list.add(item);
        }
        return ResultVo.success(list);
    }

    // ==================== 用户管理（不分页）====================
    @GetMapping("/user/list")
    public ResultVo<List<UserVo>> getUserList(@RequestParam(required = false) String keyword) {
        return ResultVo.success(adminService.getUserList(keyword));
    }

    @PostMapping("/user/add")
    public ResultVo<UserVo> addUser(@RequestBody UserVo userVo) {
        return ResultVo.success(adminService.addUser(userVo));
    }

    @PutMapping("/user/update")
    public ResultVo<UserVo> updateUser(@RequestBody UserVo userVo) {
        return ResultVo.success(adminService.updateUser(userVo));
    }

    @DeleteMapping("/user/delete/{id}")
    public ResultVo<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ResultVo.success();
    }

    @PutMapping("/user/status")
    public ResultVo<Void> updateUserStatus(@RequestParam Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return ResultVo.success();
    }

    // ==================== 医生管理（不分页）====================
    @GetMapping("/doctor/list")
    public ResultVo<List<DoctorVo>> getDoctorList(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String title) {
        return ResultVo.success(adminService.getDoctorList(name, department, title));
    }

    @PostMapping("/doctor/add")
    public ResultVo<DoctorVo> addDoctor(@RequestBody DoctorVo doctorVo) {
        return ResultVo.success(adminService.addDoctor(doctorVo));
    }

    @PutMapping("/doctor/update")
    public ResultVo<DoctorVo> updateDoctor(@RequestBody DoctorVo doctorVo) {
        return ResultVo.success(adminService.updateDoctor(doctorVo));
    }

    @PutMapping("/doctor/status")
    public ResultVo<Void> updateDoctorStatus(@RequestParam Long id, @RequestParam Integer status) {
        adminService.updateDoctorStatus(id, status);
        return ResultVo.success();
    }

    @DeleteMapping("/doctor/delete/{id}")
    public ResultVo<Void> deleteDoctor(@PathVariable Long id) {
        adminService.deleteDoctor(id);
        return ResultVo.success();
    }

    // ==================== 宠物管理（不分页）====================
    @GetMapping("/pet/list")
    public ResultVo<List<PetVo>> getPetList(@RequestParam(required = false) String keyword) {
        return ResultVo.success(adminService.getPetList(keyword));
    }

    @PostMapping("/pet/add")
    public ResultVo<PetVo> addPet(@RequestBody PetVo petVo) {
        return ResultVo.success(adminService.addPet(petVo));
    }

    @PutMapping("/pet/update")
    public ResultVo<PetVo> updatePet(@RequestBody PetVo petVo) {
        return ResultVo.success(adminService.updatePet(petVo));
    }

    @DeleteMapping("/pet/delete/{id}")
    public ResultVo<Void> deletePet(@PathVariable Long id) {
        adminService.deletePet(id);
        return ResultVo.success();
    }
}