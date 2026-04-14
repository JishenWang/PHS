package com.pethospital.pet_hospital.vo.admin;

import lombok.Data;

@Data
public class DashboardVo {
    private Long userCount;
    private Long doctorCount;
    private Long petCount;
    private Long todayReserve;
}