package com.pethospital.pet_hospital.vo.owner;

import lombok.Data;

/**
 * 自填记录视图对象
 */
@Data
public class OwnerHealthRecordVo {

    private Long id;

    private String type;

    private String typeName;

    private String petName;

    private String title;

    private String content;

    private String recordDate;

    private String createTime;
}
