package com.server.internshipserver.common.config;

import com.server.internshipserver.security.filter.JwtAuthenticationFilter;
import com.server.internshipserver.security.handler.CustomAccessDeniedHandler;
import com.server.internshipserver.security.handler.CustomAuthenticationFailureHandler;
import com.server.internshipserver.security.handler.CustomAuthenticationSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security配置类
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    
    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF，因为使用JWT
            .csrf().disable()
            // 配置CORS
            .cors()
            .and()
            // 使用无状态session，不使用session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // 配置请求权限
            .authorizeRequests()
                // 允许OPTIONS预检请求
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                // 允许访问的路径 白名单 无需token
                .antMatchers(
                    "/auth/login", 
                    "/auth/register", 
                    "/swagger-ui/**", 
                    "/swagger-ui.html",
                    "/v2/api-docs",
                    "/v2/api-docs/**",
                    "/v3/api-docs/**", 
                    "/swagger-resources/**",
                    "/webjars/**",
                    "/doc.html",
                    "/uploads/**"  // 允许访问上传的文件
                ).permitAll()
                // 其他请求需要认证
                .anyRequest().authenticated()
            .and()
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            // 配置异常处理
            .exceptionHandling()
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("{\"code\":401,\"message\":\"未认证，请先登录\"}");
                })
                .accessDeniedHandler(accessDeniedHandler);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
