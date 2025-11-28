package com.server.internshipserver.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应结果封装类
 * 用于封装所有API接口的响应数据，包含响应码、消息、数据和时间戳
 * 
 * @param <T> 响应数据的类型
 */
@ApiModel(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    
    /**
     * 序列化版本号
     */
    private static final long serialVersionUID = 1L;

    /**
     * 响应码
     */
    @ApiModelProperty(value = "响应码", example = "200")
    private int code;

    /**
     * 响应消息
     */
    @ApiModelProperty(value = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据")
    private T data;

    /**
     * 响应时间戳
     */
    @ApiModelProperty(value = "响应时间戳")
    private LocalDateTime timestamp;

    /**
     * 无参构造函数
     * 初始化时间戳为当前时间
     */
    public Result() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数（指定响应码和消息）
     * 
     * @param code 响应码
     * @param message 响应消息
     */
    public Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数（指定响应码、消息和数据）
     * 
     * @param code 响应码
     * @param message 响应消息
     * @param data 响应数据
     */
    public Result(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功响应（自定义消息和数据）
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.SERVER_ERROR.getCode(), ResultCode.SERVER_ERROR.getMessage());
    }

    /**
     * 失败响应（自定义消息）
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.SERVER_ERROR.getCode(), message);
    }

    /**
     * 失败响应（使用ResultCode）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应（自定义码和消息）
     */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message);
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
     * 设置响应码
     * 
     * @param code 响应码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取响应消息
     * 
     * @return 响应消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置响应消息
     * 
     * @param message 响应消息
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * 获取响应数据
     * 
     * @return 响应数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置响应数据
     * 
     * @param data 响应数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取响应时间戳
     * 
     * @return 响应时间戳
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * 设置响应时间戳
     * 
     * @param timestamp 响应时间戳
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

