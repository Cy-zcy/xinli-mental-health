package com.example.xinli.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            
            // 配置CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置请求授权
            .authorizeHttpRequests(auth -> auth
                // 允许管理员认证相关接口无需认证
                .requestMatchers("/api/admin/auth/**").permitAll()
                // 允许用户认证相关接口无需认证
                .requestMatchers("/api/auth/**").permitAll()
                // 允许论坛公开接口无需认证
                .requestMatchers("/api/forum/categories").permitAll()
                .requestMatchers("/api/forum/posts").permitAll()
                .requestMatchers("/api/forum/posts/*").permitAll()
                // ===== 心理测评模块接口 =====
                // 问卷列表和详情：公开访问，无需登录
                .requestMatchers("/api/assessment/list").permitAll()
                .requestMatchers("/api/assessment/{id}").permitAll()
                // 提交答卷和历史记录：由 Controller 内部自行校验 JWT（与论坛模块保持一致）
                .requestMatchers("/api/assessment/submit").permitAll()
                .requestMatchers("/api/assessment/history").permitAll()
                // 允许OPTIONS请求
                .requestMatchers("OPTIONS", "/**").permitAll()
                // 允许静态资源访问
                .requestMatchers("/static/**", "/public/**", "/assets/**").permitAll()
                // 其他管理员接口需要认证
                .requestMatchers("/api/admin/**").authenticated()
                // 用户相关接口需要认证
                .requestMatchers("/api/user/**").authenticated()
                // 其他请求允许访问
                .anyRequest().permitAll()
            )
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
