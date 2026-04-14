package com.pethospital.pet_hospital.vo.owner;

import lombok.Data;

/**
 * 咨询视图对象
 */
@Data
public class ConsultVo {

    private Long id;

    private String consultationNo;

    private String title;

    private String content;

    private String petName;

    private String doctorName;

    private String status;

    private String statusName;

    private Integer replyCount;

    private String createTime;
}
