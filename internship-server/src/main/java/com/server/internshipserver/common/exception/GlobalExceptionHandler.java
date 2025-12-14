package com.server.internshipserver.common.exception;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中抛出的各种异常，返回统一的响应格式
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 日志记录器
     */
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleBusinessException(BusinessException e, HttpServletRequest request) {
        logger.error("业务异常: {} - {}", request.getRequestURI(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /**
     * 处理参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.error("参数校验失败: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理参数绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.error("参数绑定失败: {}", message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<?> handleMissingServletRequestParameterException(MissingServletRequestParameterException e, HttpServletRequest request) {
        String message = String.format("缺少必需的请求参数: %s", e.getParameterName());
        logger.error("缺少请求参数: {} - {}", request.getRequestURI(), message);
        return Result.error(ResultCode.PARAM_ERROR.getCode(), message);
    }

    /**
     * 处理权限异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<?> handleAccessDeniedException(AccessDeniedException e) {
        logger.error("权限不足: {}", e.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }

    /**
     * 处理认证异常（登录相关异常）
     * 包括 UsernameNotFoundException、BadCredentialsException 等
     * 
     * 按照异常类型返回对应的中文提示：
     * - UsernameNotFoundException 且消息为"用户不存在" → 返回"用户不存在"
     * - BadCredentialsException → 返回"用户名或密码错误"
     * - 其他 UsernameNotFoundException → 返回原消息（如"账号已被禁用"等）
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.OK)
    public Result<?> handleAuthenticationException(AuthenticationException e, HttpServletRequest request) {
        String errorMessage;
        
        // 根据异常类型返回对应的中文提示
        if (e instanceof UsernameNotFoundException) {
            // 如果是 UsernameNotFoundException，检查消息内容
            String message = e.getMessage();
            if (message != null && message.contains("用户不存在")) {
                errorMessage = "用户不存在";
            } else {
                // 其他 UsernameNotFoundException（如"账号已被禁用"、"账号未审核通过"等）
                errorMessage = message != null ? message : "用户不存在";
            }
        } else if (e instanceof BadCredentialsException) {
            // 密码错误
            errorMessage = "用户名或密码错误";
        } else {
            // 其他认证异常
            errorMessage = e.getMessage() != null ? e.getMessage() : "用户名或密码错误";
        }
        
        logger.error("认证失败: {} - {}", request.getRequestURI(), errorMessage);
        return Result.error(ResultCode.LOGIN_ERROR.getCode(), errorMessage);
    }

    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<?> handleException(Exception e, HttpServletRequest request) {
        logger.error("系统异常: {} - {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(ResultCode.SERVER_ERROR);
    }
}

