package com.pethospital.pet_hospital.service;

import java.util.List;
import java.util.Map;

import com.pethospital.pet_hospital.vo.admin.DashboardVo;
import com.pethospital.pet_hospital.vo.admin.DoctorVo;
import com.pethospital.pet_hospital.vo.admin.PetVo;
import com.pethospital.pet_hospital.vo.admin.UserVo;

public interface IAdminService {
    
    DashboardVo getDashboardData();
    
    // 改为 List，不分页
    List<UserVo> getUserList(String keyword);
    
    UserVo addUser(UserVo userVo);
    
    UserVo updateUser(UserVo userVo);
    
    void deleteUser(Long id);
    
    void updateUserStatus(Long id, Integer status);
    
    List<DoctorVo> getDoctorList(String name, String department, String title);
    
    DoctorVo addDoctor(DoctorVo doctorVo);
    
    DoctorVo updateDoctor(DoctorVo doctorVo);
    
    void updateDoctorStatus(Long id, Integer status);
    
    void deleteDoctor(Long id);  // 新增删除医生
    
    // 改为 List，不分页
    List<PetVo> getPetList(String keyword);
    
    PetVo addPet(PetVo petVo);      // 新增添加宠物
    
    PetVo updatePet(PetVo petVo);   // 新增更新宠物
    
    void deletePet(Long id);        // 新增删除宠物

    // ==================== 系统配置 ====================
    Map<String, Object> getBasicConfig();

    void saveBasicConfig(Map<String, Object> config, String operator);

    Map<String, Object> getBusinessConfig();

    void saveBusinessConfig(Map<String, Object> config, String operator);

    Map<String, Object> getNotificationConfig();

    void saveNotificationConfig(Map<String, Object> config, String operator);

    // ==================== 操作日志 ====================
    Map<String, Object> getOperationLogs(Integer page, Integer size);

    void addOperationLog(String action, String actionType, String detail, String operator, String ip);

    // ==================== 药品管理 ====================
    List<com.pethospital.pet_hospital.entity.MedicineItem> getMedicineList(String keyword);

    com.pethospital.pet_hospital.entity.MedicineItem getMedicineDetail(Long id);

    com.pethospital.pet_hospital.entity.MedicineItem addMedicine(com.pethospital.pet_hospital.entity.MedicineItem medicine);

    com.pethospital.pet_hospital.entity.MedicineItem updateMedicine(com.pethospital.pet_hospital.entity.MedicineItem medicine);

    void deleteMedicine(Long id);

    void updateMedicineStatus(Long id, Integer status);

    // ==================== 数据维护 ====================
    void backupData(String operator);

    void clearCache();

    void resetSystem(String operator);
}