package com.pethospital.pet_hospital.utils;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT工具类
 * 用于生成、解析、验证JWT令牌
 * 
 * @author YangXinxin
 * @date 2026-04-05
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret:pethospital-secret-key-2024-springboot-vue-123456}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private Long expiration;  // 默认24小时
    
    @Value("${jwt.remember-expiration:604800000}")
    private Long rememberExpiration;  // 记住我7天

    /**
     * 生成密钥
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 从令牌中获取用户名
     */
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    /**
     * 从令牌中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("userId", Long.class);
    }

    /**
     * 从令牌中获取角色
     */
    public String getRoleFromToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.get("role", String.class);
    }

    /**
     * 从令牌中获取过期时间
     */
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    /**
     * 从令牌中获取指定字段
     */
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    /**
     * 解析令牌获取所有声明
     */
    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 检查令牌是否过期
     */
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    /**
     * 生成令牌（普通登录）
     */
    public String generateToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return doGenerateToken(claims, username, expiration);
    }

    /**
     * 生成令牌（记住我登录）
     */
    public String generateRememberToken(String username, Long userId, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("role", role);
        return doGenerateToken(claims, username, rememberExpiration);
    }

    /**
     * 执行令牌生成
     */
    private String doGenerateToken(Map<String, Object> claims, String subject, Long expirationTime) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expirationTime);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 验证令牌
     */
    public Boolean validateToken(String token, String username) {
        final String tokenUsername = getUsernameFromToken(token);
        return (tokenUsername.equals(username) && !isTokenExpired(token));
    }

    /**
     * 验证令牌（不验证用户名）
     */
    public Boolean validateToken(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 刷新令牌
     */
    public String refreshToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(claims.getSubject())
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 获取令牌剩余有效时间（毫秒）
     * @param token JWT令牌
     * @return 剩余有效时间（毫秒），负数表示已过期
     */
    public Long getRemainingTime(String token) {
        try {
            Date expirationDate = getExpirationDateFromToken(token);
            Date now = new Date();
            return expirationDate.getTime() - now.getTime();
        } catch (Exception e) {
            return -1L;
        }
    }

    /**
     * 获取令牌剩余有效时间（秒）
     * @param token JWT令牌
     * @return 剩余有效时间（秒），负数表示已过期
     */
    public Long getRemainingTimeSeconds(String token) {
        return TimeUnit.MILLISECONDS.toSeconds(getRemainingTime(token));
    }

    /**
     * 检查令牌是否即将过期（剩余时间小于指定分钟数）
     * @param token JWT令牌
     * @param minutes 分钟数
     * @return true表示即将过期
     */
    public Boolean isTokenExpiringSoon(String token, int minutes) {
        Long remainingMillis = getRemainingTime(token);
        long thresholdMillis = TimeUnit.MINUTES.toMillis(minutes);
        return remainingMillis > 0 && remainingMillis < thresholdMillis;
    }
}