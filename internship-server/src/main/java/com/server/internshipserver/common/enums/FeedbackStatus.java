package com.server.internshipserver.common.enums;

/**
 * 反馈状态枚举
 */
public enum FeedbackStatus {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    
    /**
     * 处理中（已回复）
     */
    REPLIED(1, "处理中"),
    
    /**
     * 已解决
     */
    SOLVED(2, "已解决"),
    
    /**
     * 已关闭
     */
    CLOSED(3, "已关闭");

    private final int code;
    private final String desc;

    FeedbackStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static FeedbackStatus fromCode(int code) {
        for (FeedbackStatus status : FeedbackStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FeedbackStatus code: " + code);
    }
}

