package com.pethospital.pet_hospital.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

import com.pethospital.pet_hospital.entity.Feedback;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.FeedbackMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private FeedbackMapper feedbackMapper;
    
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/dashboard")
    public ResultVo<DashboardVo> getDashboardData() {
        return ResultVo.success(adminService.getDashboardData());
    }

    @GetMapping("/dashboard/trend")
    public ResultVo<List<Map<String, Object>>> getTrendData(@RequestParam String period) {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            String sql;
            if ("week".equals(period)) {
                sql = "SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, COUNT(*) as count " +
                      "FROM register_record WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND is_deleted = 0 " +
                      "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date";
            } else if ("month".equals(period)) {
                sql = "SELECT DATE_FORMAT(create_time, '%Y-%m-%d') as date, COUNT(*) as count " +
                      "FROM register_record WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 29 DAY) AND is_deleted = 0 " +
                      "GROUP BY DATE_FORMAT(create_time, '%Y-%m-%d') ORDER BY date";
            } else {
                sql = "SELECT DATE_FORMAT(create_time, '%Y-%m') as date, COUNT(*) as count " +
                      "FROM register_record WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 11 MONTH) AND is_deleted = 0 " +
                      "GROUP BY DATE_FORMAT(create_time, '%Y-%m') ORDER BY date";
            }
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("date", row.get("date"));
                item.put("count", row.get("count"));
                list.add(item);
            }
        } catch (Exception e) {
            // 出错时返回空列表，前端会处理
        }
        return ResultVo.success(list);
    }

    @GetMapping("/dashboard/pet-distribution")
    public ResultVo<List<Map<String, Object>>> getPetTypeDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT type as name, COUNT(*) as value FROM pet WHERE is_deleted = 0 GROUP BY type ORDER BY value DESC"
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                String name = row.get("name") != null ? String.valueOf(row.get("name")) : "Unknown";
                item.put("name", name);
                item.put("value", row.get("value"));
                list.add(item);
            }
        } catch (Exception e) {
            // 出错时返回空列表
        }
        return ResultVo.success(list);
    }

    @GetMapping("/dashboard/activities")
    public ResultVo<List<Map<String, Object>>> getRecentActivities() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT id, operation as content, user_name as user, create_time as time, module as type " +
                "FROM sys_operation_log ORDER BY create_time DESC LIMIT 8"
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row.get("id"));
                String content = String.valueOf(row.get("user") != null ? row.get("user") : "System") + " " + String.valueOf(row.get("content"));
                item.put("content", content);
                item.put("time", row.get("time"));
                String type = String.valueOf(row.get("type"));
                if (type.contains("新增") || type.contains("添加") || type.contains("创建")) {
                    item.put("type", "primary");
                } else if (type.contains("删除") || type.contains("取消")) {
                    item.put("type", "danger");
                } else if (type.contains("更新") || type.contains("修改") || type.contains("完成")) {
                    item.put("type", "success");
                } else {
                    item.put("type", "info");
                }
                list.add(item);
            }
        } catch (Exception e) {
            // 出错时返回空列表
        }
        return ResultVo.success(list);
    }
    
    @GetMapping("/dashboard/dept-distribution")
    public ResultVo<List<Map<String, Object>>> getDeptDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT department as name, COUNT(*) as value FROM doctor_profile WHERE is_deleted = 0 GROUP BY department ORDER BY value DESC"
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                String name = row.get("name") != null && !String.valueOf(row.get("name")).isEmpty() 
                    ? String.valueOf(row.get("name")) : "Uncategorized";
                item.put("name", name);
                item.put("value", row.get("value"));
                list.add(item);
            }
        } catch (Exception e) {
            // 出错时返回空列表
        }
        return ResultVo.success(list);
    }
    
    @GetMapping("/dashboard/appointment-status")
    public ResultVo<List<Map<String, Object>>> getAppointmentStatusDistribution() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT status_text as name, COUNT(*) as value FROM appointment WHERE is_deleted = 0 GROUP BY status_text"
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                String name = row.get("name") != null ? String.valueOf(row.get("name")) : "Unknown";
                item.put("name", name);
                item.put("value", row.get("value"));
                list.add(item);
            }
        } catch (Exception e) {
            // 出错时返回空列表
        }
        return ResultVo.success(list);
    }
    
    @GetMapping("/dashboard/today-appointments")
    public ResultVo<List<Map<String, Object>>> getTodayAppointments() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT a.id, a.appointment_no as appointmentNo, p.name as petName, u.real_name as ownerName, " +
                "d.name as doctorName, a.service_type as serviceType, a.status_text as status, " +
                "DATE_FORMAT(a.appointment_time, '%H:%i') as time " +
                "FROM appointment a " +
                "LEFT JOIN pet p ON a.pet_id = p.id " +
                "LEFT JOIN sys_user u ON a.owner_user_id = u.id " +
                "LEFT JOIN doctor_profile d ON a.doctor_id = d.id " +
                "WHERE DATE(a.appointment_time) = CURDATE() AND a.is_deleted = 0 " +
                "ORDER BY a.appointment_time LIMIT 10"
            );
            for (Map<String, Object> row : rows) {
                Map<String, Object> item = new HashMap<>();
                item.put("id", row.get("appointmentNo") != null ? row.get("appointmentNo") : ("A" + row.get("id")));
                item.put("petName", row.get("petName") != null ? row.get("petName") : "Unknown");
                item.put("ownerName", row.get("ownerName") != null ? row.get("ownerName") : "Unknown");
                item.put("doctorName", row.get("doctorName") != null ? row.get("doctorName") : "Unassigned");
                item.put("type", row.get("serviceType") != null ? row.get("serviceType") : "General");
                item.put("time", row.get("time") != null ? row.get("time") : "--:--");
                item.put("status", row.get("status") != null ? row.get("status") : "Pending");
                list.add(item);
            }
        } catch (Exception e) {
            // 出错时返回空列表
        }
        return ResultVo.success(list);
    }
    
    @GetMapping("/dashboard/notices")
    public ResultVo<List<Map<String, Object>>> getNotices() {
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            // 库存预警：库存低于 warning_stock_qty 的药品
            List<Map<String, Object>> lowStock = jdbcTemplate.queryForList(
                "SELECT medicine_name as name FROM medicine_item WHERE stock_qty <= warning_stock_qty AND is_deleted = 0 LIMIT 3"
            );
            for (Map<String, Object> row : lowStock) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "lowStock");
                item.put("params", Map.of("name", row.get("name")));
                item.put("timeType", "justNow");
                item.put("read", false);
                list.add(item);
            }
            
            // 今日待处理预约数
            Long pendingCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE DATE(appointment_time) = CURDATE() AND (status = '0' OR status_text = 'pending') AND is_deleted = 0",
                Long.class);
            if (pendingCount != null && pendingCount > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "pendingAppointments");
                item.put("params", Map.of("count", pendingCount));
                item.put("timeType", "today");
                item.put("read", false);
                list.add(item);
            }
            
            // 本周新增用户数
            Long newUsers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE create_time >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)",
                Long.class);
            if (newUsers != null && newUsers > 0) {
                Map<String, Object> item = new HashMap<>();
                item.put("type", "newUsers");
                item.put("params", Map.of("count", newUsers));
                item.put("timeType", "thisWeek");
                item.put("read", true);
                list.add(item);
            }
            
            // 系统运行状态
            Map<String, Object> sysItem = new HashMap<>();
            sysItem.put("type", "systemRunning");
            sysItem.put("params", new HashMap<>());
            sysItem.put("timeType", "system");
            sysItem.put("read", true);
            list.add(sysItem);
            
        } catch (Exception e) {
            // 出错时返回空列表
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

    // ==================== 药品管理 ====================
    @GetMapping("/medicine/list")
    public ResultVo<List<com.pethospital.pet_hospital.entity.MedicineItem>> getMedicineList(
            @RequestParam(required = false) String keyword) {
        return ResultVo.success(adminService.getMedicineList(keyword));
    }

    @GetMapping("/medicine/detail/{id}")
    public ResultVo<com.pethospital.pet_hospital.entity.MedicineItem> getMedicineDetail(@PathVariable Long id) {
        return ResultVo.success(adminService.getMedicineDetail(id));
    }

    @PostMapping("/medicine")
    public ResultVo<com.pethospital.pet_hospital.entity.MedicineItem> addMedicine(
            @RequestBody com.pethospital.pet_hospital.entity.MedicineItem medicine) {
        return ResultVo.success(adminService.addMedicine(medicine));
    }

    @PutMapping("/medicine/{id}")
    public ResultVo<com.pethospital.pet_hospital.entity.MedicineItem> updateMedicine(
            @PathVariable Long id,
            @RequestBody com.pethospital.pet_hospital.entity.MedicineItem medicine) {
        medicine.setId(id);
        return ResultVo.success(adminService.updateMedicine(medicine));
    }

    @DeleteMapping("/medicine/{id}")
    public ResultVo<Void> deleteMedicine(@PathVariable Long id) {
        adminService.deleteMedicine(id);
        return ResultVo.success();
    }

    @PutMapping("/medicine/status")
    public ResultVo<Void> updateMedicineStatus(@RequestParam Long id, @RequestParam Integer status) {
        adminService.updateMedicineStatus(id, status);
        return ResultVo.success();
    }

    // ==================== 系统配置 ====================

    @GetMapping("/system/config/basic")
    public ResultVo<Map<String, Object>> getBasicConfig() {
        return ResultVo.success(adminService.getBasicConfig());
    }

    @PutMapping("/system/config/basic")
    public ResultVo<Void> saveBasicConfig(@RequestBody Map<String, Object> config) {
        adminService.saveBasicConfig(config, getCurrentUsername());
        return ResultVo.success();
    }

    @GetMapping("/system/config/business")
    public ResultVo<Map<String, Object>> getBusinessConfig() {
        return ResultVo.success(adminService.getBusinessConfig());
    }

    @PutMapping("/system/config/business")
    public ResultVo<Void> saveBusinessConfig(@RequestBody Map<String, Object> config) {
        adminService.saveBusinessConfig(config, getCurrentUsername());
        return ResultVo.success();
    }

    @GetMapping("/system/config/notification")
    public ResultVo<Map<String, Object>> getNotificationConfig() {
        return ResultVo.success(adminService.getNotificationConfig());
    }

    @PutMapping("/system/config/notification")
    public ResultVo<Void> saveNotificationConfig(@RequestBody Map<String, Object> config) {
        adminService.saveNotificationConfig(config, getCurrentUsername());
        return ResultVo.success();
    }

    @GetMapping("/system/logs")
    public ResultVo<Map<String, Object>> getOperationLogs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        return ResultVo.success(adminService.getOperationLogs(page, size));
    }

    @PostMapping("/system/backup")
    public ResultVo<Void> backupData() {
        adminService.backupData(getCurrentUsername());
        return ResultVo.success();
    }

    @DeleteMapping("/system/cache")
    public ResultVo<Void> clearCache() {
        adminService.clearCache();
        adminService.addOperationLog("Clear cache", "warning", "Clear system cache", getCurrentUsername(), "127.0.0.1");
        return ResultVo.success();
    }

    @PostMapping("/system/reset")
    public ResultVo<Void> resetSystem() {
        adminService.resetSystem(getCurrentUsername());
        return ResultVo.success();
    }

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "admin";
    }
    
    // ==================== 系统设置 ====================
    
    @PostMapping("/feedback")
    public ResultVo<String> submitFeedback(@RequestBody Map<String, String> body) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : "admin";
        
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
