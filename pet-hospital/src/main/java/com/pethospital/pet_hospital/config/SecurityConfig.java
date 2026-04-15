package com.pethospital.pet_hospital.config;

import com.pethospital.pet_hospital.filter.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 关键：启用 CORS！！！
            .cors().and()
            // 禁用 CSRF
            .csrf(csrf -> csrf.disable())
            // 无状态会话
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 授权配置
            .authorizeHttpRequests(auth -> auth
                // 关键：修正登录路径！！！
                .requestMatchers(
                    "/auth/**",           // ← 添加这个，匹配 /auth/login
                    "/api/auth/**",// ← 添加这个，匹配 /api/auth/login
                    "/api/login/**",
                    "/api/register/**",
                    "/api/common/**",
                    "/desk/**",
                    "/register/**",       // ← 添加这个，挂号相关
                    "/order/**",          // ← 添加这个，订单相关
                    "/charge/**",         // ← 添加这个，收费相关
                    "/reserve/**",        // ← 添加这个，预约相关
                    "/pet/**",            // ← 添加这个，宠物相关
                    "/doctor/**",         // ← 添加这个，医生相关
                    "/admin/**", 
                    "/doc.html",
                    "/swagger-ui/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/webjars/**"
                ).permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}