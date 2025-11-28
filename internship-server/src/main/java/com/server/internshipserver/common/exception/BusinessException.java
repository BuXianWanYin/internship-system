package com.server.internshipserver.common.exception;

import com.server.internshipserver.common.result.ResultCode;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况，包含错误码和错误消息
 */
public class BusinessException extends RuntimeException {
    
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private int code;
    
    /**
     * 错误消息
     */
    private String message;

    /**
     * 构造业务异常（使用默认错误码）
     * 
     * @param message 错误消息
     */
    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    /**
     * 构造业务异常（指定错误码和消息）
     * 
     * @param code 错误码
     * @param message 错误消息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    /**
     * 构造业务异常（使用默认错误码，包含原因）
     * 
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    /**
     * 构造业务异常（指定错误码和消息，包含原因）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @param cause 异常原因
     */
    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    /**
     * 获取错误码
     * 
     * @return 错误码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置错误码
     * 
     * @param code 错误码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取错误消息
     * 
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 设置错误消息
     * 
     * @param message 错误消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
}

