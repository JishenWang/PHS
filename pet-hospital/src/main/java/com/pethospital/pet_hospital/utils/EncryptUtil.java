package com.pethospital.pet_hospital.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 加密工具类
 * 提供密码加密、MD5、Base64等常用加密功能
 */
@Component
public class EncryptUtil {

    private static final BCryptPasswordEncoder BCryptEncoder = new BCryptPasswordEncoder();
    private static final SecureRandom secureRandom = new SecureRandom();

    /**
     * 生成随机盐值（16字节，Base64编码）
     * @return 随机盐值字符串
     */
    public static String generateSalt() {
        byte[] salt = new byte[16];
        secureRandom.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 使用盐值加密密码（SHA-256 + Salt）
     * @param password 原始密码
     * @param salt 盐值
     * @return 加密后的密码
     */
    public static String encryptPassword(String password, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String saltedPassword = password + salt;
            byte[] digest = md.digest(saltedPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("密码加密失败", e);
        }
    }

    /**
     * BCrypt加密密码
     */
    public static String encodePassword(String rawPassword) {
        return BCryptEncoder.encode(rawPassword);
    }

    /**
     * BCrypt验证密码
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return BCryptEncoder.matches(rawPassword, encodedPassword);
    }

    /**
     * MD5加密
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }

    /**
     * SHA-256加密
     */
    public static String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA-256加密失败", e);
        }
    }

    /**
     * Base64编码
     */
    public static String base64Encode(String input) {
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     */
    public static String base64Decode(String input) {
        byte[] decoded = Base64.getDecoder().decode(input);
        return new String(decoded, StandardCharsets.UTF_8);
    }

    /**
     * 生成简单随机密码
     */
    public static String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}