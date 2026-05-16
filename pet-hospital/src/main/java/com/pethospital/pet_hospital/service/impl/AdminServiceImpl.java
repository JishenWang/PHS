package com.pethospital.pet_hospital.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.pethospital.pet_hospital.utils.RedisUtil;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pethospital.pet_hospital.entity.Doctor;
import com.pethospital.pet_hospital.entity.OperationLog;
import com.pethospital.pet_hospital.entity.Pet;
import com.pethospital.pet_hospital.entity.SysConfig;
import com.pethospital.pet_hospital.entity.User;
import com.pethospital.pet_hospital.mapper.DoctorMapper;
import com.pethospital.pet_hospital.mapper.OperationLogMapper;
import com.pethospital.pet_hospital.mapper.MedicineItemMapper;
import com.pethospital.pet_hospital.mapper.PetMapper;
import com.pethospital.pet_hospital.mapper.SysConfigMapper;
import com.pethospital.pet_hospital.mapper.UserMapper;
import com.pethospital.pet_hospital.service.IAdminService;
import com.pethospital.pet_hospital.vo.admin.DashboardVo;
import com.pethospital.pet_hospital.vo.admin.DoctorVo;
import com.pethospital.pet_hospital.vo.admin.PetVo;
import com.pethospital.pet_hospital.vo.admin.UserVo;

@Service
@SuppressWarnings("null")
public class AdminServiceImpl implements IAdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DoctorMapper doctorMapper;
    
    @Autowired
    private PetMapper petMapper;

    @Autowired
    private SysConfigMapper sysConfigMapper;

    @Autowired
    private OperationLogMapper operationLogMapper;

    @Autowired
    private MedicineItemMapper medicineItemMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    @PostConstruct
    public void initOperationLogColumn() {
        try {
            // 检查 sys_operation_log 表是否存在 user_name 字段
            List<Map<String, Object>> columns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operation_log LIKE 'user_name'"
            );
            if (columns.isEmpty()) {
                jdbcTemplate.execute(
                    "ALTER TABLE sys_operation_log ADD COLUMN user_name VARCHAR(64) DEFAULT NULL COMMENT '操作用户'"
                );
                log.info("成功为 sys_operation_log 表添加 user_name 字段");
            }
            // 检查是否存在 operation 字段
            List<Map<String, Object>> opColumns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operation_log LIKE 'operation'"
            );
            if (opColumns.isEmpty()) {
                jdbcTemplate.execute(
                    "ALTER TABLE sys_operation_log ADD COLUMN operation VARCHAR(128) DEFAULT NULL COMMENT '操作内容'"
                );
                log.info("成功为 sys_operation_log 表添加 operation 字段");
            }
            // 检查是否存在 module 字段（兼容旧表结构使用 action_type）
            List<Map<String, Object>> moduleColumns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operation_log LIKE 'module'"
            );
            if (moduleColumns.isEmpty()) {
                // 如果 module 不存在，检查是否有 action_type
                List<Map<String, Object>> actionTypeColumns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM sys_operation_log LIKE 'action_type'"
                );
                if (actionTypeColumns.isEmpty()) {
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN module VARCHAR(50) DEFAULT NULL COMMENT '操作模块'"
                    );
                    log.info("成功为 sys_operation_log 表添加 module 字段");
                } else {
                    // 有 action_type 但没有 module，添加 module 并复制数据
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN module VARCHAR(50) DEFAULT NULL COMMENT '操作模块'"
                    );
                    jdbcTemplate.execute(
                        "UPDATE sys_operation_log SET module = action_type"
                    );
                    log.info("成功为 sys_operation_log 表添加 module 字段，并从 action_type 复制数据");
                }
            }
            // 检查是否存在 request_param 字段（兼容旧表结构使用 detail）
            List<Map<String, Object>> reqParamColumns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operation_log LIKE 'request_param'"
            );
            if (reqParamColumns.isEmpty()) {
                List<Map<String, Object>> detailColumns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM sys_operation_log LIKE 'detail'"
                );
                if (detailColumns.isEmpty()) {
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN request_param VARCHAR(500) DEFAULT NULL COMMENT '请求参数'"
                    );
                    log.info("成功为 sys_operation_log 表添加 request_param 字段");
                } else {
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN request_param VARCHAR(500) DEFAULT NULL COMMENT '请求参数'"
                    );
                    jdbcTemplate.execute(
                        "UPDATE sys_operation_log SET request_param = detail"
                    );
                    log.info("成功为 sys_operation_log 表添加 request_param 字段，并从 detail 复制数据");
                }
            }
            // 检查是否存在 ip 字段
            List<Map<String, Object>> ipColumns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operation_log LIKE 'ip'"
            );
            if (ipColumns.isEmpty()) {
                jdbcTemplate.execute(
                    "ALTER TABLE sys_operation_log ADD COLUMN ip VARCHAR(64) DEFAULT NULL COMMENT 'IP地址'"
                );
                log.info("成功为 sys_operation_log 表添加 ip 字段");
            }
            // 检查是否存在 create_time 字段（兼容 createTime / created_time）
            List<Map<String, Object>> createTimeColumns = jdbcTemplate.queryForList(
                "SHOW COLUMNS FROM sys_operation_log LIKE 'create_time'"
            );
            if (createTimeColumns.isEmpty()) {
                List<Map<String, Object>> camelCaseColumns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM sys_operation_log LIKE 'createTime'"
                );
                List<Map<String, Object>> createdTimeColumns = jdbcTemplate.queryForList(
                    "SHOW COLUMNS FROM sys_operation_log LIKE 'created_time'"
                );
                if (camelCaseColumns.isEmpty() && createdTimeColumns.isEmpty()) {
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"
                    );
                    log.info("成功为 sys_operation_log 表添加 create_time 字段");
                } else if (!camelCaseColumns.isEmpty()) {
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"
                    );
                    jdbcTemplate.execute(
                        "UPDATE sys_operation_log SET create_time = createTime"
                    );
                    log.info("成功为 sys_operation_log 表添加 create_time 字段，并从 createTime 复制数据");
                } else {
                    jdbcTemplate.execute(
                        "ALTER TABLE sys_operation_log ADD COLUMN create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'"
                    );
                    jdbcTemplate.execute(
                        "UPDATE sys_operation_log SET create_time = created_time"
                    );
                    log.info("成功为 sys_operation_log 表添加 create_time 字段，并从 created_time 复制数据");
                }
            }
        } catch (Exception e) {
            log.warn("检查或添加 sys_operation_log 字段失败: {}", e.getMessage());
        }
    }

    @Override
    public DashboardVo getDashboardData() {
        DashboardVo vo = new DashboardVo();
        vo.setUserCount(userMapper.selectCount(null));
        vo.setDoctorCount(doctorMapper.selectCount(null));
        vo.setPetCount(petMapper.selectCount(null));
        
        // 今日预约数（appointment 表中预约时间为今天的记录）
        try {
            Long todayReserve = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE DATE(appointment_time) = CURDATE() AND is_deleted = 0",
                Long.class);
            vo.setTodayReserve(todayReserve != null ? todayReserve : 0L);
        } catch (Exception e) {
            vo.setTodayReserve(0L);
        }
        
        // 本月收入（order_info 表中本月已付款订单总额）
        try {
            Double income = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(payable_amount), 0) FROM order_info WHERE pay_status = 1 AND DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')",
                Double.class);
            vo.setIncome(income != null ? income : 0.0);
        } catch (Exception e) {
            vo.setIncome(0.0);
        }
        
        // 待处理预约（appointment 表中状态为 pending/待确认的记录）
        try {
            Long pendingReserve = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM appointment WHERE (status = '0' OR status = 'pending' OR status_text = 'pending') AND is_deleted = 0",
                Long.class);
            vo.setPendingReserve(pendingReserve != null ? pendingReserve : 0L);
        } catch (Exception e) {
            vo.setPendingReserve(0L);
        }
        
        // 药品库存总数
        try {
            Long medicineCount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(stock_qty), 0) FROM medicine_item WHERE is_deleted = 0",
                Long.class);
            vo.setMedicineCount(medicineCount != null ? medicineCount : 0L);
        } catch (Exception e) {
            vo.setMedicineCount(0L);
        }
        
        // 本月新增用户
        try {
            Long newUserThisMonth = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM sys_user WHERE DATE_FORMAT(create_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m')",
                Long.class);
            vo.setNewUserThisMonth(newUserThisMonth != null ? newUserThisMonth : 0L);
        } catch (Exception e) {
            vo.setNewUserThisMonth(0L);
        }
        
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
        
        // 同步更新 doctor_profile.name（如果该用户是医生）
        if (StringUtils.hasText(userVo.getRealName()) && userVo.getId() != null) {
            LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Doctor::getUserId, userVo.getId());
            wrapper.last("LIMIT 1");
            Doctor doctor = doctorMapper.selectOne(wrapper);
            if (doctor != null) {
                Doctor updateDoctor = new Doctor();
                updateDoctor.setId(doctor.getId());
                updateDoctor.setName(userVo.getRealName());
                updateDoctor.setUpdateTime(now);
                doctorMapper.updateById(updateDoctor);
            }
        }
        
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
            // 从 register_record 表统计真实接诊患者数量（status=2 已完成）
            try {
                Long count = jdbcTemplate.queryForObject(
                    "SELECT COUNT(DISTINCT pet_id) FROM register_record WHERE doctor_id = ? AND status = 2 AND is_deleted = 0",
                    Long.class, doctor.getId());
                vo.setPatientCount(count != null ? count.intValue() : 0);
            } catch (Exception e) {
                vo.setPatientCount(0);
            }
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
        
        // 同步更新 sys_user 表中的 real_name，让医生端显示的名字也同步
        if (StringUtils.hasText(doctorVo.getName())) {
            Doctor exist = doctorMapper.selectById(doctorVo.getId());
            if (exist != null && exist.getUserId() != null) {
                User user = new User();
                user.setId(exist.getUserId());
                user.setRealName(doctorVo.getName());
                user.setUpdateTime(now);
                user.setUpdatedTime(now);
                // 不更新密码和创建时间
                user.setPassword(null);
                user.setCreateTime(null);
                user.setCreatedTime(null);
                userMapper.updateById(user);
            }
        }
        
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

            // 若 lastVisit 为空，实时查询最近一次就诊时间
            if (pet.getLastVisit() == null) {
                try {
                    List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                            "SELECT MAX(register_time) as last_visit FROM register_record WHERE pet_id = ? AND is_deleted = 0",
                            pet.getId());
                    if (!rows.isEmpty() && rows.get(0).get("last_visit") != null) {
                        Object lv = rows.get(0).get("last_visit");
                        if (lv instanceof java.sql.Timestamp) {
                            vo.setLastVisit(((java.sql.Timestamp) lv).toLocalDateTime());
                        } else if (lv instanceof LocalDateTime) {
                            vo.setLastVisit((LocalDateTime) lv);
                        }
                    }
                } catch (Exception e) {
                    // ignore
                }
            }

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

        // 同步 type -> species，确保医生端显示一致
        if (StringUtils.hasText(pet.getType()) && !StringUtils.hasText(pet.getSpecies())) {
            pet.setSpecies(pet.getType());
        }

        // 同步 gender -> genderCode，确保医生端显示一致
        if (pet.getGenderCode() == null && StringUtils.hasText(pet.getGender())) {
            String g = pet.getGender().trim().toLowerCase();
            if (g.equals("male") || g.equals("公") || g.equals("1")) {
                pet.setGenderCode(1);
            } else if (g.equals("female") || g.equals("母") || g.equals("2")) {
                pet.setGenderCode(2);
            }
        }

        petMapper.updateById(pet);
        return petVo;
    }
    
    @Override
    public void deletePet(Long id) {
        petMapper.deleteById(id);
    }

    // ==================== 系统配置 ====================

    private String getConfigValue(String group, String key, String defaultValue) {
        // 先按 group+key 查询（正常情况）
        LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysConfig::getConfigGroup, group);
        wrapper.eq(SysConfig::getConfigKey, key);
        List<SysConfig> list = sysConfigMapper.selectList(wrapper);
        if (list != null && !list.isEmpty()) {
            if (list.size() > 1) {
                for (int i = 1; i < list.size(); i++) {
                    SysConfig toDelete = list.get(i);
                    if (toDelete.getId() != null) {
                        sysConfigMapper.deleteById(toDelete.getId());
                    } else {
                        LambdaQueryWrapper<SysConfig> delWrapper = new LambdaQueryWrapper<>();
                        delWrapper.eq(SysConfig::getConfigKey, toDelete.getConfigKey());
                        sysConfigMapper.delete(delWrapper);
                    }
                }
                log.info("清理 sys_config 重复数据: group={}, key={}, 删除{}条", group, key, list.size() - 1);
            }
            return list.get(0).getConfigValue();
        }
        // 兼容历史数据：如果 group+key 查不到，尝试只按 key 查询（可能 config_group 不对）
        LambdaQueryWrapper<SysConfig> fallbackWrapper = new LambdaQueryWrapper<>();
        fallbackWrapper.eq(SysConfig::getConfigKey, key);
        List<SysConfig> fallbackList = sysConfigMapper.selectList(fallbackWrapper);
        if (fallbackList != null && !fallbackList.isEmpty()) {
            SysConfig exist = fallbackList.get(0);
            log.warn("sys_config 数据兼容: key={} 存在但 group 不匹配(期望={}, 实际={})，修正 group", key, group, exist.getConfigGroup());
            // 修正 group 为期望值
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysConfig> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(SysConfig::getConfigKey, key);
            updateWrapper.set(SysConfig::getConfigGroup, group);
            sysConfigMapper.update(updateWrapper);
            return exist.getConfigValue();
        }
        return defaultValue;
    }

    private void setConfigValue(String group, String key, String value, String name) {
        try {
            LambdaQueryWrapper<SysConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SysConfig::getConfigGroup, group);
            wrapper.eq(SysConfig::getConfigKey, key);
            List<SysConfig> list = sysConfigMapper.selectList(wrapper);
            LocalDateTime now = LocalDateTime.now();
            if (list != null && !list.isEmpty()) {
                com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysConfig> updateWrapper =
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
                updateWrapper.eq(SysConfig::getConfigGroup, group);
                updateWrapper.eq(SysConfig::getConfigKey, key);
                updateWrapper.set(SysConfig::getConfigValue, value);
                updateWrapper.set(SysConfig::getUpdateTime, now);
                sysConfigMapper.update(updateWrapper);
                if (list.size() > 1) {
                    for (int i = 1; i < list.size(); i++) {
                        SysConfig toDelete = list.get(i);
                        if (toDelete.getId() != null) {
                            sysConfigMapper.deleteById(toDelete.getId());
                        } else {
                            LambdaQueryWrapper<SysConfig> delWrapper = new LambdaQueryWrapper<>();
                            delWrapper.eq(SysConfig::getConfigKey, toDelete.getConfigKey());
                            sysConfigMapper.delete(delWrapper);
                        }
                    }
                    log.info("清理 sys_config 重复数据: group={}, key={}, 删除{}条", group, key, list.size() - 1);
                }
            } else {
                SysConfig config = new SysConfig();
                config.setConfigKey(key);
                config.setConfigValue(value);
                config.setConfigName(name);
                config.setConfigGroup(group);
                config.setCreateTime(now);
                config.setUpdateTime(now);
                sysConfigMapper.insert(config);
            }
        } catch (DuplicateKeyException e) {
            // 唯一键冲突时，key 已存在但 group 可能不匹配，直接按 key 更新
            log.warn("配置插入冲突，按 key 更新: group={}, key={}", group, key);
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<SysConfig> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
            updateWrapper.eq(SysConfig::getConfigKey, key);
            updateWrapper.set(SysConfig::getConfigValue, value);
            updateWrapper.set(SysConfig::getConfigGroup, group);
            updateWrapper.set(SysConfig::getUpdateTime, LocalDateTime.now());
            sysConfigMapper.update(updateWrapper);
        }
    }

    @Override
    public Map<String, Object> getBasicConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("hospitalName", getConfigValue("basic", "hospital.name", "宠物医院"));
        result.put("contactPhone", getConfigValue("basic", "hospital.phone", ""));
        result.put("address", getConfigValue("basic", "hospital.address", ""));
        result.put("maxAdvanceDays", Integer.parseInt(getConfigValue("basic", "appointment.max_advance_days", "7")));
        result.put("dailyLimit", Integer.parseInt(getConfigValue("basic", "appointment.daily_limit", "50")));
        result.put("announcement", getConfigValue("basic", "system.announcement", ""));
        return result;
    }

    @Override
    public void saveBasicConfig(Map<String, Object> config, String operator) {
        setConfigValue("basic", "hospital.name", str(config.get("hospitalName")), "机构名称");
        setConfigValue("basic", "hospital.phone", str(config.get("contactPhone")), "联系电话");
        setConfigValue("basic", "hospital.address", str(config.get("address")), "机构地址");
        setConfigValue("basic", "appointment.max_advance_days", str(config.get("maxAdvanceDays")), "预约提前天数");
        setConfigValue("basic", "appointment.daily_limit", str(config.get("dailyLimit")), "每日预约上限");
        setConfigValue("basic", "system.announcement", str(config.get("announcement")), "系统公告");
        addOperationLog("Save basic configuration", "success", "Update system basic configuration", operator, "127.0.0.1");
    }

    @Override
    public Map<String, Object> getBusinessConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("normalFee", new BigDecimal(getConfigValue("business", "register.normal_fee", "10.00")));
        result.put("expertFee", new BigDecimal(getConfigValue("business", "register.expert_fee", "30.00")));
        result.put("morningTime", List.of(
            getConfigValue("business", "business.morning_start", "08:00"),
            getConfigValue("business", "business.morning_end", "12:00")
        ));
        result.put("afternoonTime", List.of(
            getConfigValue("business", "business.afternoon_start", "14:00"),
            getConfigValue("business", "business.afternoon_end", "18:00")
        ));
        result.put("consultDuration", Integer.parseInt(getConfigValue("business", "business.consult_duration", "30")));
        result.put("refundDeadline", Integer.parseInt(getConfigValue("business", "business.refund_deadline", "4")));
        return result;
    }

    @Override
    public void saveBusinessConfig(Map<String, Object> config, String operator) {
        setConfigValue("business", "register.normal_fee", str(config.get("normalFee")), "普通挂号费");
        setConfigValue("business", "register.expert_fee", str(config.get("expertFee")), "专家挂号费");
        Object morningTime = config.get("morningTime");
        if (morningTime instanceof List) {
            List<?> times = (List<?>) morningTime;
            if (times.size() >= 2) {
                setConfigValue("business", "business.morning_start", str(times.get(0)), "上午开始时间");
                setConfigValue("business", "business.morning_end", str(times.get(1)), "上午结束时间");
            }
        }
        Object afternoonTime = config.get("afternoonTime");
        if (afternoonTime instanceof List) {
            List<?> times = (List<?>) afternoonTime;
            if (times.size() >= 2) {
                setConfigValue("business", "business.afternoon_start", str(times.get(0)), "下午开始时间");
                setConfigValue("business", "business.afternoon_end", str(times.get(1)), "下午结束时间");
            }
        }
        setConfigValue("business", "business.consult_duration", str(config.get("consultDuration")), "诊疗时长");
        setConfigValue("business", "business.refund_deadline", str(config.get("refundDeadline")), "退号截止时间");
        addOperationLog("Save business configuration", "success", "Update system business configuration", operator, "127.0.0.1");
    }

    @Override
    public Map<String, Object> getNotificationConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("smsReserveSuccess", Boolean.parseBoolean(getConfigValue("notification", "notify.sms_reserve_success", "true")));
        result.put("smsRemind", Boolean.parseBoolean(getConfigValue("notification", "notify.sms_remind", "true")));
        result.put("remindTime", Integer.parseInt(getConfigValue("notification", "notify.remind_time", "60")));
        result.put("emailErrorAlert", Boolean.parseBoolean(getConfigValue("notification", "notify.email_error_alert", "false")));
        result.put("alertEmail", getConfigValue("notification", "notify.alert_email", ""));
        return result;
    }

    @Override
    public void saveNotificationConfig(Map<String, Object> config, String operator) {
        setConfigValue("notification", "notify.sms_reserve_success", str(config.get("smsReserveSuccess")), "预约成功通知");
        setConfigValue("notification", "notify.sms_remind", str(config.get("smsRemind")), "就诊前提醒");
        setConfigValue("notification", "notify.remind_time", str(config.get("remindTime")), "提醒提前时间");
        setConfigValue("notification", "notify.email_error_alert", str(config.get("emailErrorAlert")), "系统异常报警");
        setConfigValue("notification", "notify.alert_email", str(config.get("alertEmail")), "接收邮箱");
        addOperationLog("Save notification configuration", "success", "Update system notification configuration", operator, "127.0.0.1");
    }

    @Override
    public Map<String, Object> getOperationLogs(Integer page, Integer size) {
        int currentPage = page != null ? page : 1;
        int pageSize = size != null ? size : 10;
        int offset = (currentPage - 1) * pageSize;

        String countSql = "SELECT COUNT(*) FROM sys_operation_log";
        Long total = jdbcTemplate.queryForObject(countSql, Long.class);

        // 兼容 user_name 列可能不存在的情况
        String sql;
        try {
            jdbcTemplate.queryForList("SELECT user_name FROM sys_operation_log LIMIT 1");
            sql = "SELECT id, user_name, module, request_param, ip, create_time FROM sys_operation_log ORDER BY create_time DESC LIMIT ? OFFSET ?";
        } catch (Exception e) {
            sql = "SELECT id, module, request_param, ip, create_time FROM sys_operation_log ORDER BY create_time DESC LIMIT ? OFFSET ?";
        }
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql, pageSize, offset);

        List<Map<String, Object>> list = records.stream().map(row -> {
            Map<String, Object> item = new HashMap<>();
            Object createTime = row.get("create_time");
            item.put("time", createTime != null ? createTime.toString() : "");
            item.put("user", row.get("user_name") != null ? row.get("user_name") : "System");
            item.put("action", row.get("module"));
            item.put("type", "");
            item.put("detail", row.get("request_param"));
            item.put("ip", row.get("ip"));
            return item;
        }).collect(Collectors.toList());

        Map<String, Object> data = new HashMap<>();
        data.put("list", list);
        data.put("total", total != null ? total : 0);
        return data;
    }

    @Override
    public void addOperationLog(String action, String actionType, String detail, String operator, String ip) {
        try {
            // 先尝试包含 user_name 的插入
            String sql = "INSERT INTO sys_operation_log (module, request_param, user_name, ip, create_time) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(sql, action, detail, operator, ip, LocalDateTime.now());
        } catch (Exception e) {
            // 如果 user_name 列不存在，降级为不包含该列的插入
            try {
                String sql = "INSERT INTO sys_operation_log (module, request_param, ip, create_time) VALUES (?, ?, ?, ?)";
                jdbcTemplate.update(sql, action, detail, ip, LocalDateTime.now());
            } catch (Exception ex) {
                log.warn("记录操作日志失败: {}", ex.getMessage());
            }
        }
    }

    private String str(Object obj) {
        return obj == null ? "" : String.valueOf(obj);
    }

    // ==================== 药品管理 ====================
    @Override
    public List<com.pethospital.pet_hospital.entity.MedicineItem> getMedicineList(String keyword) {
        LambdaQueryWrapper<com.pethospital.pet_hospital.entity.MedicineItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.pethospital.pet_hospital.entity.MedicineItem::getIsDeleted, 0);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                .like(com.pethospital.pet_hospital.entity.MedicineItem::getMedicineName, keyword)
                .or()
                .like(com.pethospital.pet_hospital.entity.MedicineItem::getMedicineCode, keyword)
                .or()
                .like(com.pethospital.pet_hospital.entity.MedicineItem::getCategory, keyword)
            );
        }
        wrapper.orderByDesc(com.pethospital.pet_hospital.entity.MedicineItem::getCreateTime);
        return medicineItemMapper.selectList(wrapper);
    }

    @Override
    public com.pethospital.pet_hospital.entity.MedicineItem getMedicineDetail(Long id) {
        return medicineItemMapper.selectById(id);
    }

    @Override
    public com.pethospital.pet_hospital.entity.MedicineItem addMedicine(com.pethospital.pet_hospital.entity.MedicineItem medicine) {
        LocalDateTime now = LocalDateTime.now();
        medicine.setCreateTime(now);
        medicine.setUpdateTime(now);
        medicine.setIsDeleted(0);
        if (medicine.getStatus() == null) {
            medicine.setStatus(1);
        }
        if (medicine.getStockQty() == null) {
            medicine.setStockQty(0);
        }
        if (medicine.getWarningStockQty() == null) {
            medicine.setWarningStockQty(0);
        }
        medicineItemMapper.insert(medicine);
        return medicine;
    }

    @Override
    public com.pethospital.pet_hospital.entity.MedicineItem updateMedicine(com.pethospital.pet_hospital.entity.MedicineItem medicine) {
        LocalDateTime now = LocalDateTime.now();
        medicine.setUpdateTime(now);
        medicine.setCreateTime(null);
        medicineItemMapper.updateById(medicine);
        return medicine;
    }

    @Override
    public void deleteMedicine(Long id) {
        // 全局配置了 logic-delete-field，deleteById 会自动做逻辑删除
        medicineItemMapper.deleteById(id);
    }

    @Override
    public void updateMedicineStatus(Long id, Integer status) {
        com.pethospital.pet_hospital.entity.MedicineItem medicine = new com.pethospital.pet_hospital.entity.MedicineItem();
        medicine.setId(id);
        medicine.setStatus(status);
        medicine.setUpdateTime(LocalDateTime.now());
        medicineItemMapper.updateById(medicine);
    }

    // ==================== 数据维护 ====================

    @Override
    public void backupData(String operator) {
        try {
            // 解析数据库名（从 jdbc:mysql://host:port/dbName?params 中提取 dbName）
            String dbName = "pet_hospital";
            if (dbUrl != null && dbUrl.contains("/")) {
                // 去掉协议头，得到 host:port/dbName?params
                String temp = dbUrl.substring(dbUrl.indexOf("://") + 3);
                int slash = temp.indexOf('/');
                if (slash >= 0) {
                    int qmark = temp.indexOf('?', slash);
                    dbName = qmark > 0 ? temp.substring(slash + 1, qmark) : temp.substring(slash + 1);
                }
            }

            // 创建备份目录
            String backupDir = System.getProperty("user.dir") + "/uploads/backup/";
            java.io.File dir = new java.io.File(backupDir);
            if (!dir.exists()) dir.mkdirs();

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = backupDir + "backup_" + dbName + "_" + timestamp + ".sql";

            // 查找 mysqldump 可执行文件
            String mysqldumpPath = findMysqldump();
            if (mysqldumpPath == null) {
                throw new RuntimeException("未找到 mysqldump 工具，请确认 MySQL 客户端已安装并加入系统 PATH");
            }
            log.info("使用 mysqldump: {}", mysqldumpPath);

            // 构建 ProcessBuilder
            java.util.List<String> cmdList = new java.util.ArrayList<>();
            cmdList.add(mysqldumpPath);
            cmdList.add("-h");
            cmdList.add("localhost");
            cmdList.add("-P");
            cmdList.add("3306");
            cmdList.add("-u");
            cmdList.add(dbUsername);
            if (dbPassword != null && !dbPassword.isEmpty()) {
                cmdList.add("-p" + dbPassword);
            }
            cmdList.add("--single-transaction");
            cmdList.add("--no-tablespaces");
            cmdList.add(dbName);

            log.info("mysqldump 命令: {}", String.join(" ", cmdList));

            String errFileName = fileName + ".err";
            ProcessBuilder pb = new ProcessBuilder(cmdList);
            pb.redirectOutput(new java.io.File(fileName));
            pb.redirectError(new java.io.File(errFileName));

            Process process = pb.start();
            boolean finished = process.waitFor(60, java.util.concurrent.TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new RuntimeException("数据库备份超时（60秒）");
            }

            int exitCode = process.exitValue();
            if (exitCode != 0) {
                // 读取错误输出
                java.io.File errFile = new java.io.File(errFileName);
                String errMsg = "";
                if (errFile.exists()) {
                    errMsg = java.nio.file.Files.readString(errFile.toPath());
                    errFile.delete();
                }
                throw new RuntimeException("mysqldump 执行失败，退出码: " + exitCode + ", " + errMsg);
            }
            // 删除临时错误文件
            new java.io.File(errFileName).delete();

            log.info("数据库备份成功: {}", fileName);
            addOperationLog("Data backup", "info", "Manual data backup to " + fileName, operator, "127.0.0.1");
        } catch (Exception e) {
            log.error("数据库备份失败: {}", e.getMessage(), e);
            throw new RuntimeException("数据库备份失败: " + e.getMessage());
        }
    }

    private String findMysqldump() {
        String os = System.getProperty("os.name").toLowerCase();
        String cmdName = os.contains("win") ? "mysqldump.exe" : "mysqldump";

        // 1. 尝试从 PATH 查找
        try {
            String findCmd = os.contains("win") ? "where " + cmdName : "which " + cmdName;
            Process p = Runtime.getRuntime().exec(findCmd);
            p.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            try (java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(p.getInputStream()))) {
                String line = reader.readLine();
                if (line != null && !line.isEmpty() && !line.toLowerCase().contains("not found")
                        && !line.toLowerCase().contains("could not find")) {
                    return line.trim();
                }
            }
        } catch (Exception e) {
            log.debug("从 PATH 查找 mysqldump 失败: {}", e.getMessage());
        }

        // 2. 尝试常见路径（Windows）
        if (os.contains("win")) {
            String[] commonPaths = {
                "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\mysqldump.exe",
                "C:\\Program Files (x86)\\MySQL\\MySQL Server 8.0\\bin\\mysqldump.exe",
                "C:\\Program Files\\MySQL\\MySQL Server 5.7\\bin\\mysqldump.exe",
                "C:\\xampp\\mysql\\bin\\mysqldump.exe",
                "C:\\wamp64\\bin\\mysql\\mysql8.0.31\\bin\\mysqldump.exe",
                "C:\\wamp\\bin\\mysql\\mysql5.7.21\\bin\\mysqldump.exe"
            };
            for (String path : commonPaths) {
                if (new java.io.File(path).exists()) {
                    return path;
                }
            }
        } else {
            // Linux / macOS 常见路径
            String[] commonPaths = {
                "/usr/bin/mysqldump",
                "/usr/local/bin/mysqldump",
                "/usr/local/mysql/bin/mysqldump",
                "/opt/mysql/bin/mysqldump"
            };
            for (String path : commonPaths) {
                if (new java.io.File(path).exists()) {
                    return path;
                }
            }
        }
        return null;
    }

    @Override
    public void clearCache() {
        redisUtil.clearAll();
        log.info("系统缓存已清除");
    }

    @Override
    public void resetSystem(String operator) {
        try {
            // 禁用外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

            // 清空业务数据表（保留 sys_user 管理员、sys_config、medicine_item、cage）
            String[] tables = {
                "pet", "doctor", "register_record", "prescription", "prescription_item",
                "order_info", "order_item", "hospitalization", "medical_record",
                "reserve", "appointment", "consult", "consultation", "consultation_reply",
                "feedback", "owner_health_record", "service_item", "operation_log"
            };

            for (String table : tables) {
                try {
                    jdbcTemplate.execute("TRUNCATE TABLE " + table);
                    log.info("已清空表: {}", table);
                } catch (Exception e) {
                    log.warn("清空表 {} 失败: {}", table, e.getMessage());
                }
            }

            // 重置 cage 表（保留笼子信息，重置状态）
            try {
                jdbcTemplate.execute("UPDATE cage SET current_pet_id = NULL, status = 0 WHERE current_pet_id IS NOT NULL");
                log.info("已重置笼子状态");
            } catch (Exception e) {
                log.warn("重置笼子状态失败: {}", e.getMessage());
            }

            // 恢复外键检查
            jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");

            // 清除缓存
            redisUtil.clearAll();

            log.info("系统已重置，操作人: {}", operator);
            addOperationLog("重置系统", "danger", "重置系统数据", operator, "127.0.0.1");
        } catch (Exception e) {
            // 确保外键检查恢复
            try {
                jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
            } catch (Exception ex) {
                log.warn("恢复外键检查失败: {}", ex.getMessage());
            }
            log.error("重置系统失败: {}", e.getMessage());
            throw new RuntimeException("重置系统失败: " + e.getMessage());
        }
    }
}