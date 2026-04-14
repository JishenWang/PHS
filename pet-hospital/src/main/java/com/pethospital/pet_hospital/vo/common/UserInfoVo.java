package com.pethospital.pet_hospital.vo.common;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.util.List;

/**
 * 用户信息VO
 * 登录成功后返回的用户信息
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoVo {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 真实姓名
     */
    private String realName;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 角色编码
     */
    private String roleCode;
    
    /**
     * 角色名称
     */
    private String roleName;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 状态：0禁用，1启用
     */
    private Integer status;
    
    /**
     * 可访问路由列表
     */
    private List<String> routes;
    
    /**
     * 按钮权限标识列表
     */
    private List<String> permissions;
    
    /**
     * 医生专属字段（仅当角色为医生时返回）
     */
    private Long doctorId;
    private String title;        // 职称
    private String department;   // 科室
    private String statusDesc;   // 接诊状态描述
    
    /**
     * 前台专属字段（仅当角色为前台时返回）
     */
    private Long deskId;
    
    /**
     * 宠物主人专属字段（仅当角色为宠物主人时返回）
     */
    private Long ownerId;
}