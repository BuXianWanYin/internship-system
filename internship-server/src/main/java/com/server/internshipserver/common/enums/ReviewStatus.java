package com.server.internshipserver.common.enums;

/**
 * 审核状态枚举（用于周报、日志、成果等）
 */
public enum ReviewStatus {
    /**
     * 待审核
     */
    PENDING(0, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    ReviewStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReviewStatus fromCode(int code) {
        for (ReviewStatus status : ReviewStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewStatus code: " + code);
    }
}

