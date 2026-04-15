package com.pethospital.pet_hospital.controller;

import com.pethospital.pet_hospital.dto.common.LoginDto;
import com.pethospital.pet_hospital.service.ICommonService;
import com.pethospital.pet_hospital.utils.JwtUtil;
import com.pethospital.pet_hospital.utils.RedisUtil;
import com.pethospital.pet_hospital.vo.common.ResultVo;
import com.pethospital.pet_hospital.vo.common.UserInfoVo;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * 登录认证控制器
 * 全系统统一登录入口
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@RestController
@RequestMapping({"", "/api"})
public class LoginController {
    
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);
    
    private final ICommonService commonService;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    
    public LoginController(ICommonService commonService, JwtUtil jwtUtil, RedisUtil redisUtil) {
        this.commonService = commonService;
        this.jwtUtil = jwtUtil;
        this.redisUtil = redisUtil;
    }
    
    /**
     * 发送验证码
     */
    @PostMapping("/auth/sendCode")
    public ResultVo<String> sendCode(@RequestParam String phone) {
        log.info("发送验证码，手机号: {}", phone);
        String code = commonService.sendSmsCode(phone);
        // 验证码存入Redis，5分钟有效
        redisUtil.set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);
        return ResultVo.success(code);
    }
    
    /**
     * 账号密码登录
     */
    @PostMapping("/auth/login")
    public ResultVo<UserInfoVo> login(@Valid @RequestBody LoginDto loginDto) {
        log.info("用户登录，手机号: {}, 角色: {}", loginDto.getPhone(), loginDto.getRole());
        
        // 校验登录方式
        if ("password".equals(loginDto.getLoginType())) {
            // 密码登录
            if (loginDto.getPassword() == null || loginDto.getPassword().isEmpty()) {
                return ResultVo.badRequest("密码不能为空");
            }
            return commonService.loginByPassword(loginDto);
        } else if ("code".equals(loginDto.getLoginType())) {
            // 验证码登录
            if (loginDto.getCode() == null || loginDto.getCode().isEmpty()) {
                return ResultVo.badRequest("验证码不能为空");
            }
            // 校验验证码
            String cachedCode = redisUtil.get("sms:code:" + loginDto.getPhone(), String.class);
            if (cachedCode == null || !cachedCode.equals(loginDto.getCode())) {
                return ResultVo.badRequest("验证码错误或已过期");
            }
            // 登录成功，删除验证码
            redisUtil.delete("sms:code:" + loginDto.getPhone());
            return commonService.loginByCode(loginDto);
        } else {
            return ResultVo.badRequest("不支持的登录方式");
        }
    }
    
    /**
     * 刷新令牌
     */
    @PostMapping("/auth/refresh")
    public ResultVo<String> refreshToken(@RequestHeader("Authorization") String token) {
        log.info("刷新令牌");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return ResultVo.unauthorized("无效的令牌");
        }
        String newToken = commonService.refreshToken(userId);
        return ResultVo.success(newToken);
    }
    
    /**
     * 获取当前用户信息
     */
    @GetMapping("/auth/userInfo")
    public ResultVo<UserInfoVo> getUserInfo(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            return ResultVo.unauthorized("无效的令牌");
        }
        UserInfoVo userInfo = commonService.getUserInfo(userId);
        return ResultVo.success(userInfo);
    }
    
    /**
     * 退出登录
     */
    @PostMapping("/auth/logout")
    public ResultVo<String> logout(@RequestHeader("Authorization") String token) {
        log.info("用户退出登录");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        // 将令牌加入黑名单
        Long expiration = jwtUtil.getRemainingTime(token);
        if (expiration > 0) {
            redisUtil.set("blacklist:token:" + token, "1", expiration, TimeUnit.MILLISECONDS);
        }
        return ResultVo.success("退出成功");
    }
    
    /**
     * 找回密码
     */
    @PostMapping("/auth/resetPassword")
    public ResultVo<String> resetPassword(@RequestParam String phone,
                                        @RequestParam String code,
                                        @RequestParam String newPassword) {
        log.info("找回密码，手机号: {}", phone);
        
        // 校验验证码
        String cachedCode = redisUtil.get("sms:code:" + phone, String.class);
        if (cachedCode == null || !cachedCode.equals(code)) {
            return ResultVo.badRequest("验证码错误或已过期");
        }
        
        boolean success = commonService.resetPassword(phone, newPassword);
        if (success) {
            redisUtil.delete("sms:code:" + phone);
            return ResultVo.success("密码重置成功");
        } else {
            return ResultVo.error("密码重置失败，用户不存在");
        }
    }
}