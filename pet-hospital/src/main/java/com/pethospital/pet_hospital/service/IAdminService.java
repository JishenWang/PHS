package com.pethospital.pet_hospital.service;

import java.util.List;

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
}