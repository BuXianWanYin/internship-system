package com.server.internshipserver.common.enums;

/**
 * 面试结果枚举
 */
public enum InterviewResult {
    /**
     * 通过
     */
    PASSED(1, "通过"),
    
    /**
     * 不通过
     */
    FAILED(2, "不通过"),
    
    /**
     * 待定
     */
    PENDING(3, "待定");

    private final int code;
    private final String desc;

    InterviewResult(int code, String desc) {
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
    public static InterviewResult getByCode(int code) {
        for (InterviewResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        return null;
    }
}

