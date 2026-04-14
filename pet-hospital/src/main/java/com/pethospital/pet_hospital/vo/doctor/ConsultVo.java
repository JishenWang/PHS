package com.pethospital.pet_hospital.vo.doctor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 在线咨询VO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultVo {
    
    private Long consultId;         // 咨询ID
    private String consultNo;       // 咨询单号
    
    // 宠物信息
    private Long petId;
    private String petName;
    
    // 主人信息
    private Long ownerId;
    private String ownerName;
    private String ownerPhone;
    
    // 咨询内容
    private String title;           // 咨询标题
    private String content;         // 咨询内容
    private String images;          // 图片地址（逗号分隔）
    private List<String> imageList; // 图片地址列表
    
    // 回复内容
    private String replyContent;
    private String replyImages;
    private List<String> replyImageList;
    
    // 状态信息
    private Integer replyStatus;    // 回复状态：0-未回复，1-已回复
    private String replyStatusDesc;
    private Integer rating;         // 评价分数 1-5
    private String comment;         // 评价内容
    
    private LocalDateTime consultTime;
    private LocalDateTime replyTime;
}