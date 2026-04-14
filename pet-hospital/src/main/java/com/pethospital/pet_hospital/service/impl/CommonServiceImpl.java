package com.pethospital.pet_hospital.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.pethospital.pet_hospital.common.constant.RoleConstant;
import com.pethospital.pet_hospital.common.enums.RoleEnum;
import com.pethospital.pet_hospital.common.exception.BusinessException;
import com.pethospital.pet_hospital.dto.common.LoginDto;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.ICommonService;
import com.pethospital.pet_hospital.utils.EncryptUtil;
import com.pethospital.pet_hospital.utils.JwtUtil;
import com.pethospital.pet_hospital.utils.RedisUtil;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import com.pethospital.pet_hospital.vo.common.UserInfoVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 公共服务实现类
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Service
public class CommonServiceImpl implements ICommonService {
    
    private static final Logger log = LoggerFactory.getLogger(CommonServiceImpl.class);
    
    private final UserMapper userMapper;
    private final DoctorMapper doctorMapper;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    public CommonServiceImpl(UserMapper userMapper, DoctorMapper doctorMapper, 
                             JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.doctorMapper = doctorMapper;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    // ==================== 登录认证相关实现 ====================
    
    @Override
    public String sendSmsCode(String phone) {
        // 生成6位随机验证码
        String code = String.valueOf(ThreadLocalRandom.current().nextInt(100000, 999999));
        log.info("验证码: {} 发送给手机号: {}", code, phone);
        // 实际项目中需要调用短信服务商接口发送短信
        // 这里直接返回验证码，实际使用时需要注释掉
        return code;
    }
    
    @Override
    public ResultVo<UserInfoVo> loginByPassword(LoginDto loginDto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, loginDto.getPhone());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            return ResultVo.error("用户不存在");
        }
        
        // 校验密码
        String encryptedPassword = EncryptUtil.encryptPassword(loginDto.getPassword(), user.getSalt());
        if (!encryptedPassword.equals(user.getPassword())) {
            return ResultVo.error("密码错误");
        }
        
        // 校验角色
        if (!loginDto.getRole().equals(user.getRole())) {
            return ResultVo.error("角色不匹配，请使用正确的账号类型登录");
        }
        
        // 校验账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            return ResultVo.error("账号已被禁用，请联系管理员");
        }
        
        // 生成令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 构建用户信息
        UserInfoVo userInfo = buildUserInfo(user, token);
        
        return ResultVo.success(userInfo);
    }
    
    @Override
    public ResultVo<UserInfoVo> loginByCode(LoginDto loginDto) {
        // 查询用户
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, loginDto.getPhone());
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            // 验证码登录，如果用户不存在，自动注册
            user = autoRegister(loginDto);
        }
        
        // 校验角色
        if (!loginDto.getRole().equals(user.getRole())) {
            return ResultVo.error("角色不匹配，请使用正确的账号类型登录");
        }
        
        // 校验账号状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            return ResultVo.error("账号已被禁用，请联系管理员");
        }
        
        // 生成令牌
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        // 构建用户信息
        UserInfoVo userInfo = buildUserInfo(user, token);
        
        return ResultVo.success(userInfo);
    }
    
    /**
     * 自动注册（验证码登录时用户不存在）
     */
    private User autoRegister(LoginDto loginDto) {
        User user = new User();
        user.setPhone(loginDto.getPhone());
        user.setUsername("用户" + loginDto.getPhone().substring(7));
        user.setRole(loginDto.getRole());
        user.setStatus(1);
        
        // 生成随机密码（用户后续可通过找回密码设置）
        String defaultPassword = "123456";
        String salt = EncryptUtil.generateSalt();
        user.setSalt(salt);
        user.setPassword(EncryptUtil.encryptPassword(defaultPassword, salt));
        
        userMapper.insert(user);
        log.info("自动注册用户成功，手机号: {}, 角色: {}", loginDto.getPhone(), loginDto.getRole());
        return user;
    }
    
    @Override
    public String refreshToken(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
    }
    
    @Override
    public UserInfoVo getUserInfo(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return buildUserInfo(user, null);
    }
    
    /**
     * 构建用户信息VO
     */
    private UserInfoVo buildUserInfo(User user, String token) {
        UserInfoVo userInfo = new UserInfoVo();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setRealName(user.getRealName());
        userInfo.setPhone(user.getPhone());
        userInfo.setEmail(user.getEmail());
        userInfo.setRoleCode(user.getRole());
        userInfo.setStatus(user.getStatus());
        
        // 根据角色获取角色名称
        RoleEnum roleEnum = RoleEnum.getByCode(user.getRole());
        if (roleEnum != null) {
            userInfo.setRoleName(roleEnum.getName());
        }
        
        // 如果是医生角色，获取医生专属信息
        if (RoleConstant.ROLE_DOCTOR.equals(user.getRole())) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, user.getId());
            Doctor doctor = doctorMapper.selectOne(wrapper);
            if (doctor != null) {
                userInfo.setDoctorId(doctor.getId());
                userInfo.setTitle(doctor.getTitle());
                userInfo.setDepartment(doctor.getDepartment());
                if (doctor.getStatus() != null) {
                    switch (doctor.getStatus()) {
                        case 0: userInfo.setStatusDesc("离线"); break;
                        case 1: userInfo.setStatusDesc("空闲"); break;
                        case 2: userInfo.setStatusDesc("接诊中"); break;
                        case 3: userInfo.setStatusDesc("休息"); break;
                        default: userInfo.setStatusDesc("未知");
                    }
                }
            }
        }
        
        // 设置路由和权限（根据角色）
        userInfo.setRoutes(getRoutesByRole(user.getRole()));
        userInfo.setPermissions(getPermissionsByRole(user.getRole()));
        
        return userInfo;
    }
    
    /**
     * 根据角色获取可访问路由
     */
    private List<String> getRoutesByRole(String role) {
        List<String> routes = new ArrayList<>();
        switch (role) {
            case RoleConstant.ROLE_ADMIN:
                routes.addAll(Arrays.asList("dashboard", "user", "doctor", "pet", "system"));
                break;
            case RoleConstant.ROLE_DOCTOR:
                routes.addAll(Arrays.asList("accept", "pet", "record", "prescription", "consult"));
                break;
            case RoleConstant.ROLE_DESK:
                routes.addAll(Arrays.asList("customer", "register", "charge"));
                break;
            case RoleConstant.ROLE_OWNER:
                routes.addAll(Arrays.asList("pet", "health", "reserve", "consult"));
                break;
            default:
                break;
        }
        return routes;
    }
    
    /**
     * 根据角色获取按钮权限
     */
    private List<String> getPermissionsByRole(String role) {
        List<String> permissions = new ArrayList<>();
        switch (role) {
            case RoleConstant.ROLE_ADMIN:
                permissions.addAll(Arrays.asList("user:add", "user:edit", "user:delete",
                        "doctor:add", "doctor:edit", "doctor:delete",
                        "pet:view", "system:config"));
                break;
            case RoleConstant.ROLE_DOCTOR:
                permissions.addAll(Arrays.asList("accept:view", "accept:update",
                        "record:add", "record:edit", "record:view",
                        "prescription:add", "prescription:edit", "prescription:view",
                        "consult:reply", "consult:view"));
                break;
            case RoleConstant.ROLE_DESK:
                permissions.addAll(Arrays.asList("customer:view", "register:add", "register:edit",
                        "charge:view", "charge:pay"));
                break;
            case RoleConstant.ROLE_OWNER:
                permissions.addAll(Arrays.asList("pet:add", "pet:edit", "pet:view",
                        "health:view", "reserve:add", "consult:add"));
                break;
            default:
                break;
        }
        return permissions;
    }
    
    @Override
    public boolean resetPassword(String phone, String newPassword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        User user = userMapper.selectOne(wrapper);
        
        if (user == null) {
            return false;
        }
        
        String salt = EncryptUtil.generateSalt();
        user.setSalt(salt);
        user.setPassword(EncryptUtil.encryptPassword(newPassword, salt));
        userMapper.updateById(user);
        
        log.info("重置密码成功，手机号: {}", phone);
        return true;
    }
    
    // ==================== 文件上传相关实现（简化版） ====================
    
    @Override
    public String uploadFile(MultipartFile file, String type) {
        // TODO: 实际项目中需要实现文件上传到本地或OSS
        log.info("上传文件，文件名: {}, 类型: {}", file.getOriginalFilename(), type);
        return "/uploads/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
    }
    
    @Override
    public List<String> uploadFiles(MultipartFile[] files, String type) {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(uploadFile(file, type));
        }
        return urls;
    }
    
    @Override
    public byte[] downloadFile(String fileUrl) {
        // TODO: 实际项目中需要实现文件下载
        log.info("下载文件: {}", fileUrl);
        return new byte[0];
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        // TODO: 实际项目中需要实现文件删除
        log.info("删除文件: {}", fileUrl);
        return true;
    }
    
    // ==================== 数据字典相关实现 ====================
    
    @Override
    public List<Map<String, Object>> getDataDict(String dictType) {
        // TODO: 实际项目中需要从数据库查询数据字典
        List<Map<String, Object>> result = new ArrayList<>();
        log.info("获取数据字典: {}", dictType);
        return result;
    }
    
    @Override
    public List<Map<String, Object>> getPetBreeds() {
        // TODO: 实际项目中需要从数据库查询
        List<Map<String, Object>> breeds = new ArrayList<>();
        Map<String, Object> dog = new HashMap<>();
        dog.put("value", "dog");
        dog.put("label", "犬");
        breeds.add(dog);
        
        Map<String, Object> cat = new HashMap<>();
        cat.put("value", "cat");
        cat.put("label", "猫");
        breeds.add(cat);
        return breeds;
    }
    
    @Override
    public List<Map<String, Object>> getDiseaseTypes() {
        List<Map<String, Object>> diseases = new ArrayList<>();
        // TODO: 实际项目中需要从数据库查询
        return diseases;
    }
    
    @Override
    public List<Map<String, Object>> getDrugTypes() {
        List<Map<String, Object>> drugTypes = new ArrayList<>();
        // TODO: 实际项目中需要从数据库查询
        return drugTypes;
    }
}