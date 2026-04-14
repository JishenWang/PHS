package com.pethospital.pet_hospital.service;

import com.pethospital.pet_hospital.dto.common.LoginDto;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import com.pethospital.pet_hospital.vo.common.UserInfoVo;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 公共服务接口
 */
public interface ICommonService {
    
    // ==================== 登录认证相关 ====================
    
    /**
     * 发送短信验证码
     */
    String sendSmsCode(String phone);
    
    /**
     * 账号密码登录
     */
    ResultVo<UserInfoVo> loginByPassword(LoginDto loginDto);
    
    /**
     * 验证码登录
     */
    ResultVo<UserInfoVo> loginByCode(LoginDto loginDto);
    
    /**
     * 刷新JWT令牌
     */
    String refreshToken(Long userId);
    
    /**
     * 获取用户信息
     */
    UserInfoVo getUserInfo(Long userId);
    
    /**
     * 重置密码
     */
    boolean resetPassword(String phone, String newPassword);
    
    // ==================== 文件上传相关 ====================
    
    /**
     * 单文件上传
     */
    String uploadFile(MultipartFile file, String type);
    
    /**
     * 多文件上传
     */
    List<String> uploadFiles(MultipartFile[] files, String type);
    
    /**
     * 文件下载
     */
    byte[] downloadFile(String fileUrl);
    
    /**
     * 文件删除
     */
    boolean deleteFile(String fileUrl);
    
    // ==================== 数据字典相关 ====================
    
    /**
     * 获取数据字典
     */
    List<Map<String, Object>> getDataDict(String dictType);
    
    /**
     * 获取宠物品种列表
     */
    List<Map<String, Object>> getPetBreeds();
    
    /**
     * 获取疾病类型列表
     */
    List<Map<String, Object>> getDiseaseTypes();
    
    /**
     * 获取药品类型列表
     */
    List<Map<String, Object>> getDrugTypes();
}