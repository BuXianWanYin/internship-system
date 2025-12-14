package com.server.internshipserver.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败处理器
 * 当用户登录失败时，返回错误信息响应
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理认证失败
     * 返回200状态码和错误信息（前端统一处理错误码）
     * 
     * 按照异常类型返回对应的中文提示：
     * - UsernameNotFoundException 且消息为"用户不存在" → 返回"用户不存在"
     * - BadCredentialsException → 返回"用户名或密码错误"
     * - 其他 UsernameNotFoundException → 返回原消息（如"账号已被禁用"等）
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param exception 认证异常
     * @throws IOException IO异常
     * @throws ServletException Servlet异常
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        String errorMessage;
        
        // 根据异常类型返回对应的中文提示
        if (exception instanceof UsernameNotFoundException) {
            // 如果是 UsernameNotFoundException，检查消息内容
            String message = exception.getMessage();
            if (message != null && message.contains("用户不存在")) {
                errorMessage = "用户不存在";
            } else {
                // 其他 UsernameNotFoundException（如"账号已被禁用"、"账号未审核通过"等）
                errorMessage = message != null ? message : "用户不存在";
            }
        } else if (exception instanceof BadCredentialsException) {
            // 密码错误
            errorMessage = "用户名或密码错误";
        } else {
            // 其他认证异常
            errorMessage = exception.getMessage() != null ? exception.getMessage() : "用户名或密码错误";
        }
        
        Result<Object> result = Result.error(ResultCode.LOGIN_ERROR.getCode(), errorMessage);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
