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
 * Spring Security安全配置类
 * 配置JWT认证、权限控制、CORS等安全相关功能
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    /**
     * JWT认证过滤器
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * 自定义认证成功处理器
     */
    @Autowired
    private CustomAuthenticationSuccessHandler authenticationSuccessHandler;
    
    /**
     * 自定义认证失败处理器
     */
    @Autowired
    private CustomAuthenticationFailureHandler authenticationFailureHandler;
    
    /**
     * 自定义访问拒绝处理器
     */
    @Autowired
    private CustomAccessDeniedHandler accessDeniedHandler;

    /**
     * 配置安全过滤器链
     * 设置请求权限、JWT过滤器、异常处理等
     * 
     * @param http HttpSecurity对象
     * @return SecurityFilterChain 安全过滤器链
     * @throws Exception 配置异常
     */
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
                    "/user/enterprise/register",  // 企业注册
                    "/user/student/register",     // 学生注册
                    "/system/school/public/list", // 公开获取学校列表（用于企业注册）
                    "/system/class/share-code/validate", // 验证分享码（用于学生注册）
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

    /**
     * 配置认证管理器
     * 
     * @param authConfig 认证配置
     * @return AuthenticationManager 认证管理器
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * 配置密码编码器
     * 使用BCrypt算法对密码进行加密
     * 
     * @return PasswordEncoder 密码编码器实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
