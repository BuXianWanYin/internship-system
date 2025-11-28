package com.server.internshipserver.common.enums;

/**
 * 面试状态枚举
 * 定义面试的状态：待确认、已确认、已完成、已取消
 */
public enum InterviewStatus {
    /**
     * 待确认
     */
    PENDING(0, "待确认"),
    
    /**
     * 已确认
     */
    CONFIRMED(1, "已确认"),
    
    /**
     * 已完成
     */
    COMPLETED(2, "已完成"),
    
    /**
     * 已取消
     */
    CANCELLED(3, "已取消");

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
    InterviewStatus(int code, String desc) {
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
    public static InterviewStatus getByCode(int code) {
        for (InterviewStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

