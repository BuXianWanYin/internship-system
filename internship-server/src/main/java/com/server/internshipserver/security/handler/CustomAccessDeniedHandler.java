package com.server.internshipserver.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 访问拒绝处理器
 * 当用户已认证但无权限访问资源时，返回403错误响应
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理访问拒绝异常
     * 返回403状态码和错误信息
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param accessDeniedException 访问拒绝异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        Result<Object> result = Result.error(ResultCode.FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
