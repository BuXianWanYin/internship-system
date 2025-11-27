package com.server.internshipserver.common.enums;

/**
 * 确认状态枚举
 */
public enum ConfirmStatus {
    /**
     * 待确认
     */
    PENDING(0, "待确认"),
    
    /**
     * 已确认
     */
    CONFIRMED(1, "已确认"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    ConfirmStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ConfirmStatus fromCode(int code) {
        for (ConfirmStatus status : ConfirmStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ConfirmStatus code: " + code);
    }
}

