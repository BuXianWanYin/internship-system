package com.server.internshipserver.common.result;

/**
 * 响应码枚举类
 * 定义系统中所有可能的响应码和对应的消息
 */
public enum ResultCode {
    /**
     * 操作成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),
    
    /**
     * 未认证
     */
    UNAUTHORIZED(401, "未认证，请先登录"),
    
    /**
     * 无权限
     */
    FORBIDDEN(403, "无权限访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 服务器错误
     */
    SERVER_ERROR(500, "服务器错误"),
    
    /**
     * 业务错误码：1000-9999
     */
    BUSINESS_ERROR(1000, "业务处理失败"),
    
    /**
     * 用户名或密码错误
     */
    LOGIN_ERROR(1001, "用户名或密码错误"),
    
    /**
     * Token已过期
     */
    TOKEN_EXPIRED(1002, "Token已过期"),
    
    /**
     * Token无效
     */
    TOKEN_INVALID(1003, "Token无效"),
    
    /**
     * 用户已被禁用
     */
    USER_DISABLED(1004, "用户已被禁用"),
    
    /**
     * 数据已存在
     */
    DATA_EXISTS(1005, "数据已存在"),
    
    /**
     * 数据不存在
     */
    DATA_NOT_EXISTS(1006, "数据不存在"),
    
    /**
     * 操作失败
     */
    OPERATION_FAILED(1007, "操作失败");

    /**
     * 响应码
     */
    private final int code;
    
    /**
     * 响应消息
     */
    private final String message;

    /**
     * 构造函数
     * 
     * @param code 响应码
     * @param message 响应消息
     */
    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 获取响应码
     * 
     * @return 响应码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取响应消息
     * 
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }
}

