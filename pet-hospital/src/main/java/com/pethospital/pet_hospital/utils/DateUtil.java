package com.pethospital.pet_hospital.utils;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * 日期时间工具类
 * 提供日期格式化、计算、转换等常用功能
 */
public class DateUtil {

    // 常用日期格式
    public static final String PATTERN_DATE = "yyyy-MM-dd";
    public static final String PATTERN_TIME = "HH:mm:ss";
    public static final String PATTERN_DATETIME = "yyyy-MM-dd HH:mm:ss";
    public static final String PATTERN_DATETIME_CN = "yyyy年MM月dd日 HH:mm:ss";
    public static final String PATTERN_DATE_CN = "yyyy年MM月dd日";

    // FORMATTERS removed: use inline formatters to avoid unused field warnings

    /**
     * Date转String（yyyy-MM-dd）
     */
    public static String formatDate(Date date) {
        if (date == null) return null;
        return format(date, PATTERN_DATE);
    }

    /**
     * Date转String（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(Date date) {
        if (date == null) return null;
        return format(date, PATTERN_DATETIME);
    }

    /**
     * Date转String（自定义格式）
     */
    public static String format(Date date, String pattern) {
        if (date == null) return null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return date.toInstant().atZone(ZoneId.systemDefault()).format(formatter);
    }

    /**
     * String转Date（yyyy-MM-dd）
     */
    public static Date parseDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        return parse(dateStr, PATTERN_DATE);
    }

    /**
     * String转Date（yyyy-MM-dd HH:mm:ss）
     */
    public static Date parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) return null;
        return parse(dateTimeStr, PATTERN_DATETIME);
    }

    /**
     * String转Date（自定义格式）
     */
    public static Date parse(String dateStr, String pattern) {
        if (dateStr == null || dateStr.isEmpty()) return null;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime localDateTime = LocalDateTime.parse(dateStr, formatter);
            return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 获取当前时间（Date）
     */
    public static Date now() {
        return new Date();
    }

    /**
     * 获取当前时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static String nowStr() {
        return formatDateTime(now());
    }

    /**
     * 获取当前日期字符串（yyyy-MM-dd）
     */
    public static String todayStr() {
        return formatDate(now());
    }

    /**
     * 获取当前时间戳（毫秒）
     */
    public static long currentTimestamp() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     */
    public static long currentTimestampSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    /**
     * 计算两个日期相差天数
     */
    public static long daysBetween(Date start, Date end) {
        LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return Duration.between(startDate.atStartOfDay(), endDate.atStartOfDay()).toDays();
    }

    /**
     * 添加天数
     */
    public static Date addDays(Date date, int days) {
        Instant instant = date.toInstant().plus(Duration.ofDays(days));
        return Date.from(instant);
    }

    /**
     * 添加小时
     */
    public static Date addHours(Date date, int hours) {
        Instant instant = date.toInstant().plus(Duration.ofHours(hours));
        return Date.from(instant);
    }

    /**
     * 添加分钟
     */
    public static Date addMinutes(Date date, int minutes) {
        Instant instant = date.toInstant().plus(Duration.ofMinutes(minutes));
        return Date.from(instant);
    }

    /**
     * 判断是否在同一天
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) return false;
        return formatDate(date1).equals(formatDate(date2));
    }

    /**
     * 获取年龄（根据出生日期）
     */
    public static int getAge(Date birthDate) {
        if (birthDate == null) return 0;
        LocalDate birth = birthDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate now = LocalDate.now();
        int age = now.getYear() - birth.getYear();
        if (now.getMonthValue() < birth.getMonthValue() ||
            (now.getMonthValue() == birth.getMonthValue() && now.getDayOfMonth() < birth.getDayOfMonth())) {
            age--;
        }
        return age;
    }

    /**
     * 获取当前年份
     */
    public static int getCurrentYear() {
        return LocalDate.now().getYear();
    }

    /**
     * 获取当前月份
     */
    public static int getCurrentMonth() {
        return LocalDate.now().getMonthValue();
    }
}