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
 */
@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        
        Result<Object> result = Result.error(ResultCode.FORBIDDEN);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
