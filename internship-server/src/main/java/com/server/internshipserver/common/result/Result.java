package com.server.internshipserver.common.result;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应结果封装
 */
@ApiModel(description = "统一响应结果")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "响应码", example = "200")
    private int code;

    @ApiModelProperty(value = "响应消息", example = "操作成功")
    private String message;

    @ApiModelProperty(value = "响应数据")
    private T data;

    @ApiModelProperty(value = "响应时间戳")
    private LocalDateTime timestamp;

    public Result() {
        this.timestamp = LocalDateTime.now();
    }

    public Result(int code, String message) {
        this.code = code;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

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

    // Getters and Setters
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

