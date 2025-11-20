package com.server.internshipserver.common.exception;

/**
 * 业务异常类
 */
public class BusinessException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    private int code;
    private String message;

    public BusinessException(String message) {
        super(message);
        this.message = message;
        this.code = 1000; // 默认业务错误码
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = 1000;
    }

    public BusinessException(int code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

