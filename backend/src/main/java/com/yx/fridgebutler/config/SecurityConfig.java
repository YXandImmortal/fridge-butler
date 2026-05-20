package com.yx.fridgebutler.config;

import com.yx.fridgebutler.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.DispatcherType;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security 安全配置类
 * <p>
 * 配置系统的安全策略，包括：
 * <ul>
 *     <li>跨域（CORS）配置</li>
 *     <li>密码加密器</li>
 *     <li>请求授权规则</li>
 *     <li>JWT 认证过滤器集成</li>
 *     <li>会话管理策略（无状态）</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 配置密码编码器
     *
     * @return BCrypt 密码编码器实例，用于用户密码的加密和校验
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置跨域资源共享（CORS）
     *
     * @return CORS 配置源，定义了允许的来源、方法、请求头和暴露的响应头
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", "Origin", "X-Requested-With", "X-Captcha-Id"));
        configuration.setExposedHeaders(List.of("X-Captcha-Id"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 配置安全过滤器链
     * <p>
     * 定义系统的安全规则：
     * <ul>
     *     <li>启用 CORS 支持</li>
     *     <li>禁用 CSRF 防护（适用于前后端分离的无状态认证）</li>
     *     <li>使用无状态会话管理</li>
     *     <li>允许认证、系统信息、验证码相关接口匿名访问</li>
     *     <li>其他所有请求需要认证</li>
     *     <li>在用户名密码认证过滤器之前添加 JWT 认证过滤器</li>
     * </ul>
     * </p>
     *
     * @param http HTTP 安全构建器
     * @return 配置完成的安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth ->
                        auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR).permitAll()
                                .requestMatchers("/auth/**", "/system/**", "/captcha/generate", "/captcha/verify").permitAll()
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}