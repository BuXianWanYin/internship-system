package com.server.internshipserver.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证失败处理器
 */
@Component
public class CustomAuthenticationFailureHandler implements AuthenticationFailureHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Result<Object> result = Result.error(ResultCode.LOGIN_ERROR.getCode(), 
                exception.getMessage() != null ? exception.getMessage() : "用户名或密码错误");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
