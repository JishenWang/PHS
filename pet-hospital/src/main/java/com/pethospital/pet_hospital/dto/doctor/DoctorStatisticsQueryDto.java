package com.pethospital.pet_hospital.dto.doctor;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 医生统计查询DTO
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatisticsQueryDto {
    
    /**
     * 医生ID
     */
    @NotNull(message = "Doctor ID cannot be empty")
    private Long doctorId;
    
    /**
     * 统计类型：day-日，month-月，year-年
     */
    @NotNull(message = "Statistics type cannot be empty")
    private String statType;
    
    /**
     * 开始时间
     */
    private String startDate;
    
    /**
     * 结束时间
     */
    private String endDate;
}