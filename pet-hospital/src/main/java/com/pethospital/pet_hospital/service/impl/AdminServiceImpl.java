package com.pethospital.pet_hospital.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IAdminService;
import com.pethospital.pet_hospital.vo.admin.DashboardVo;
import com.pethospital.pet_hospital.vo.admin.DoctorVo;
import com.pethospital.pet_hospital.vo.admin.PetVo;
import com.pethospital.pet_hospital.vo.admin.UserVo;

@Service
@SuppressWarnings("null")
public class AdminServiceImpl implements IAdminService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private PetMapper petMapper;

    @Override
    public DashboardVo getDashboardData() {
        DashboardVo vo = new DashboardVo();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setDoctorCount(doctorMapper.selectCount(null));
        vo.setPetCount(petMapper.selectCount(null));
        vo.setTodayReserve(0L);
        return vo;
    }

    //用户管理
    @Override
    public List<UserVo> getUserList(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getRealName, keyword)
                   .or()
                   .like(User::getPhone, keyword);
        }
        
        List<User> users = userMapper.selectList(wrapper);
        
        return users.stream().map(user -> {
            UserVo vo = new UserVo();
            BeanUtils.copyProperties(user, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public UserVo addUser(UserVo userVo) {
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        
        // 设置默认值（数据库字段要求非空）
        LocalDateTime now = LocalDateTime.now();
        user.setStatus(1);
        user.setCreateTime(now);
        user.setUpdateTime(now);
        user.setCreatedTime(now);
        user.setUpdatedTime(now);
        
        // 如果有密码，设置密码（生产环境需要加密）
        if (StringUtils.hasText(userVo.getPassword())) {
            //添加 BCrypt 加密
            user.setPassword(com.pethospital.pet_hospital.utils.EncryptUtil.encodePassword(userVo.getPassword()));
        }
        
        userMapper.insert(user);
        userVo.setId(user.getId());
        return userVo;
    }
    
    @Override
    public UserVo updateUser(UserVo userVo) {
        User user = new User();
        BeanUtils.copyProperties(userVo, user);
        
        // 更新时间
        LocalDateTime now = LocalDateTime.now();
        user.setUpdateTime(now);
        user.setUpdatedTime(now);
        
        // 编辑时不更新密码和时间字段（由数据库管理）
        user.setPassword(null);
        user.setCreateTime(null);
        user.setCreatedTime(null);
        
        userMapper.updateById(user);
        return userVo;
    }
    
    @Override
    public void deleteUser(Long id) {
        userMapper.deleteById(id);
    }
    
    @Override
    public void updateUserStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        user.setUpdateTime(LocalDateTime.now());
        user.setUpdatedTime(LocalDateTime.now());
        userMapper.updateById(user);
    }

    //  医生管理
    @Override
    public List<DoctorVo> getDoctorList(String name, String department, String title) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        
        if (StringUtils.hasText(name)) {
            wrapper.like(Doctor::getName, name);
        }
        if (StringUtils.hasText(department)) {
            wrapper.eq(Doctor::getDepartment, department);
        }
        if (StringUtils.hasText(title)) {
            wrapper.eq(Doctor::getTitle, title);
        }
        
        List<Doctor> doctors = doctorMapper.selectList(wrapper);
        
        return doctors.stream().map(doctor -> {
            DoctorVo vo = new DoctorVo();
            BeanUtils.copyProperties(doctor, vo);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public DoctorVo addDoctor(DoctorVo doctorVo) {
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(doctorVo, doctor);
        
        // 设置默认值
        LocalDateTime now = LocalDateTime.now();
        doctor.setStatus(1);
        // 使用 workStatus 而非 workStatusCode
        doctor.setWorkStatus(1); 
        doctor.setAuthStatus(1);
        doctor.setPatientCount(0);
        doctor.setRating(BigDecimal.valueOf(5.0));
        doctor.setCreateTime(now);
        doctor.setUpdateTime(now);
        doctor.setCreatedTime(now);
        doctor.setUpdatedTime(now);
        doctor.setIsDeleted(0);
        
        doctorMapper.insert(doctor);
        doctorVo.setId(doctor.getId());
        return doctorVo;
    }
    
    @Override
    public DoctorVo updateDoctor(DoctorVo doctorVo) {
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(doctorVo, doctor);
        
        LocalDateTime now = LocalDateTime.now();
        doctor.setUpdateTime(now);
        doctor.setUpdatedTime(now);
        
        // 不更新创建时间
        doctor.setCreateTime(null);
        doctor.setCreatedTime(null);
        
        doctorMapper.updateById(doctor);
        return doctorVo;
    }
    
    @Override
    public void updateDoctorStatus(Long id, Integer status) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setStatus(status);
        doctor.setUpdateTime(LocalDateTime.now());
        doctor.setUpdatedTime(LocalDateTime.now());
        doctorMapper.updateById(doctor);
    }
    
    @Override
    public void deleteDoctor(Long id) {
        doctorMapper.deleteById(id);
    }

    //宠物管理
    @Override
    public List<PetVo> getPetList(String keyword) {
        LambdaQueryWrapper<Pet> wrapper = new LambdaQueryWrapper<>();
    
    // 关键：过滤软删除的数据
        wrapper.eq(Pet::getIsDeleted, 0);
    
    // 状态正常的宠物
        wrapper.eq(Pet::getStatus, 1);
    
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(Pet::getName, keyword)
                .or()
                .like(Pet::getOwnerName, keyword)
                .or()
                .like(Pet::getOwnerPhone, keyword)
            );
        }
    
    // 按创建时间倒序
        wrapper.orderByDesc(Pet::getCreateTime);
    
        List<Pet> pets = petMapper.selectList(wrapper);
    
        return pets.stream().map(pet -> {
            PetVo vo = new PetVo();
            BeanUtils.copyProperties(pet, vo);
        
        // 处理疫苗字符串转数组（数据库是逗号分隔的字符串）
            if (StringUtils.hasText(pet.getVaccines())) {
                vo.setVaccines(List.of(pet.getVaccines().split(",")));
            } else {
                vo.setVaccines(new ArrayList<>()); // 空数组而非null
            }
        
        // 处理健康状态映射（数据库可能是英文，前端需要映射）
            if (StringUtils.hasText(pet.getHealthStatus())) {
                vo.setHealthStatus(pet.getHealthStatus());
            } else {
                vo.setHealthStatus("healthy"); // 默认健康
            }
        
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public PetVo addPet(PetVo petVo) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petVo, pet);
        
        LocalDateTime now = LocalDateTime.now();
        pet.setStatus(1);
        pet.setCreateTime(now);
        pet.setUpdateTime(now);
        pet.setCreatedTime(now);
        pet.setUpdatedTime(now);
        
        // 处理疫苗数组转字符串（数据库存储）
        if (petVo.getVaccines() != null && !petVo.getVaccines().isEmpty()) {
            pet.setVaccines(String.join(",", petVo.getVaccines()));
        }
        
        petMapper.insert(pet);
        petVo.setId(pet.getId());
        return petVo;
    }
    
    @Override
    public PetVo updatePet(PetVo petVo) {
        Pet pet = new Pet();
        BeanUtils.copyProperties(petVo, pet);
        
        LocalDateTime now = LocalDateTime.now();
        pet.setUpdateTime(now);
        pet.setUpdatedTime(now);
        
        pet.setCreateTime(null);
        pet.setCreatedTime(null);
        
        // 处理疫苗数组转字符串
        if (petVo.getVaccines() != null && !petVo.getVaccines().isEmpty()) {
            pet.setVaccines(String.join(",", petVo.getVaccines()));
        }
        
        petMapper.updateById(pet);
        return petVo;
    }
    
    @Override
    public void deletePet(Long id) {
        petMapper.deleteById(id);
    }
}