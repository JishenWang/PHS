package com.pethospital.pet_hospital.dto.owner;

import java.time.LocalDateTime;

import lombok.Data;

/**
 * 预约数据传输对象
 */
@Data
public class AppointmentDto {

    private Long petId;

    private Long doctorId;

    private String serviceType;  // consultation, vaccine, exam, grooming

    private LocalDateTime appointmentTime;

    private String remark;
}
