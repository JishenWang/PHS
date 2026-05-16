package com.pethospital.pet_hospital.vo.admin;

import lombok.Data;

@Data
public class DashboardVo {
    private Long userCount;
    private Long doctorCount;
    private Long petCount;
    private Long todayReserve;
    private Double income;
    private Long pendingReserve;
    private Long medicineCount;
    private Long newUserThisMonth;
}
