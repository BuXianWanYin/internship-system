package com.server.internshipserver.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.common.utils.RedisUtil;
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
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private UserDetailsService userDetailsService;
    
    @Autowired
    private RedisUtil redisUtil;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = getTokenFromRequest(request);
            
            if (StringUtils.hasText(token) && jwtUtil.validateToken(token, 
                    userDetailsService.loadUserByUsername(jwtUtil.getUsernameFromToken(token)))) {
                
                String username = jwtUtil.getUsernameFromToken(token);
                
                // 检查Token是否在Redis中（用于支持登出功能）
                String redisKey = Constants.TOKEN_KEY_PREFIX + username;
                if (!redisUtil.exists(redisKey)) {
                    // Token已被注销
                    sendErrorResponse(response, ResultCode.TOKEN_INVALID);
                    return;
                }
                
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
     * 从请求中获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader(Constants.TOKEN_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 发送错误响应
     */
    private void sendErrorResponse(HttpServletResponse response, ResultCode resultCode) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Object> result = Result.error(resultCode);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}

