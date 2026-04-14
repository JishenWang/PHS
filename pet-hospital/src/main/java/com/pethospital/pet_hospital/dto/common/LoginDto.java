package com.pethospital.pet_hospital.dto.common;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * 登录请求DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
public class LoginDto {
    
    /**
     * 手机号
     */
    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 密码（密码登录时使用）
     */
    private String password;
    
    /**
     * 验证码（验证码登录时使用）
     */
    private String code;
    
    /**
     * 登录方式：password-密码登录，code-验证码登录
     */
    @NotBlank(message = "登录方式不能为空")
    private String loginType;
    
    /**
     * 角色类型：ADMIN/DOCTOR/DESK/OWNER
     */
    @NotBlank(message = "角色类型不能为空")
    private String role;
    
    // 无参构造
    public LoginDto() {}
    
    // 全参构造
    public LoginDto(String phone, String password, String code, String loginType, String role) {
        this.phone = phone;
        this.password = password;
        this.code = code;
        this.loginType = loginType;
        this.role = role;
    }
    
    // Getter 和 Setter
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getCode() {
        return code;
    }
    
    public void setCode(String code) {
        this.code = code;
    }
    
    public String getLoginType() {
        return loginType;
    }
    
    public void setLoginType(String loginType) {
        this.loginType = loginType;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
}