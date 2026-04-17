package com.pethospital.pet_hospital.vo.doctor;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorStatisticsVo {
    
    // 总体统计
    private Integer totalConsult;       // 总咨询数
    private Integer totalAccept;        // 总接诊数
    private Integer totalPrescription;  // 总处方数
    private BigDecimal totalAmount;     // 总金额
    private Double avgRating;           // 平均好评率
    
    // 按时间维度的统计（用于图表）
    private List<DailyStat> dailyStats;     // 每日统计
    private List<MonthlyStat> monthlyStats; // 每月统计
    private List<YearlyStat> yearlyStats;   // 每年统计
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyStat {
        private String date;            // 日期
        private Integer acceptCount;    // 接诊数
        private Integer consultCount;   // 咨询数
        private Integer prescriptionCount; // 处方数
        private BigDecimal amount;      // 金额
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyStat {
        private String month;           // 月份
        private Integer acceptCount;
        private Integer consultCount;
        private Integer prescriptionCount;
        private BigDecimal amount;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyStat {
        private String year;            // 年份
        private Integer acceptCount;
        private Integer consultCount;
        private Integer prescriptionCount;
        private BigDecimal amount;
    }
}