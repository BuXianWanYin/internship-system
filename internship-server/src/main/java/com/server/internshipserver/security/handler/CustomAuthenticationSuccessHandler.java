package com.server.internshipserver.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.internshipserver.common.result.Result;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证成功处理器
 * 当用户登录成功时，返回成功响应
 * 实际的Token生成和返回在AuthController中处理
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证成功
     * 返回200状态码和成功信息
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param authentication 认证对象
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Result<Object> result = Result.success("登录成功");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
