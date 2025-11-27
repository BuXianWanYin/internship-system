package com.server.internshipserver.common.enums;

/**
 * 面试状态枚举
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

    private final int code;
    private final String desc;

    InterviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据code获取枚举
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

