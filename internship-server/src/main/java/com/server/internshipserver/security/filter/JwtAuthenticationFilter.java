package com.server.internshipserver.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.common.utils.RedisUtil;
import com.server.internshipserver.common.utils.TokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT认证过滤器
 * 用于拦截HTTP请求，验证JWT Token并设置Spring Security认证信息
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    private final ObjectMapper objectMapper;
    
    /**
     * 构造函数
     * 初始化ObjectMapper并注册JavaTimeModule以支持LocalDateTime序列化
     */
    public JwtAuthenticationFilter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    /**
     * 执行JWT认证过滤
     * 从请求头中提取Token，验证Token有效性，并将用户信息设置到SecurityContext中
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param filterChain 过滤器链
     * @throws ServletException Servlet异常
     * @throws IOException IO异常
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 跳过OPTIONS预检请求
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String token = TokenUtil.getTokenFromRequest(request);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token, 
                    userDetailsService.loadUserByUsername(jwtUtil.getUsernameFromToken(token)))) {
                
                String username = jwtUtil.getUsernameFromToken(token);
                
                // 检查Token是否在Redis中（用于支持登出功能）
                String redisKey = Constants.TOKEN_KEY_PREFIX + username;
                Boolean exists = redisUtil.exists(redisKey);
                // 如果Redis连接失败，exists可能返回null，此时允许请求通过（容错处理）
                if (exists != null && !exists) {
                    // Token已被注销
                    sendErrorResponse(response, ResultCode.TOKEN_INVALID);
                    return;
                }
                
                // 加载用户详情并设置认证信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("JWT认证失败", e);
            sendErrorResponse(response, ResultCode.TOKEN_INVALID);
            return;
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 发送错误响应
     * 将错误信息以JSON格式写入HTTP响应
     * 
     * @param response HTTP响应
     * @param resultCode 结果码
     * @throws IOException IO异常
     */
    private void sendErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Object> result = Result.error(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

