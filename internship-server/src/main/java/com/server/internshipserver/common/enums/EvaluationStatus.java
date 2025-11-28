package com.server.internshipserver.common.enums;

/**
 * 评价状态枚举
 * 用于评价的状态：草稿、已提交
 */
public enum EvaluationStatus {
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),
    
    /**
     * 已提交
     */
    SUBMITTED(1, "已提交");

    /**
     * 状态代码
     */
    private final int code;
    
    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param desc 状态描述
     */
    EvaluationStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据代码获取枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static EvaluationStatus getByCode(int code) {
        for (EvaluationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

