package com.pethospital.pet_hospital.vo.owner;

import lombok.Data;

/**
 * 预约视图对象
 */
@Data
public class AppointmentVo {

    private Long id;

    private String appointmentNo;

    private String petName;

    private String serviceType;

    private String serviceTypeName;

    private String doctorName;

    private String appointmentTime;

    private String status;

    private String statusName;

    private String statusColor;

    private String remark;

    private String cancelReason;

    private String createTime;
}