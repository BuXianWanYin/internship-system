package com.server.internshipserver.common.enums;

/**
 * 面试类型枚举
 */
public enum InterviewType {
    /**
     * 现场面试
     */
    ON_SITE(1, "现场面试"),
    
    /**
     * 视频面试
     */
    VIDEO(2, "视频面试"),
    
    /**
     * 电话面试
     */
    PHONE(3, "电话面试");

    private final int code;
    private final String desc;

    InterviewType(int code, String desc) {
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
    public static InterviewType getByCode(int code) {
        for (InterviewType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

