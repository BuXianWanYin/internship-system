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
 */
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        
        Result<Object> result = Result.success("登录成功");
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
