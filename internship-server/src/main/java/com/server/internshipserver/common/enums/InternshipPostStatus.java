package com.server.internshipserver.common.enums;

/**
 * 实习岗位状态枚举
 */
public enum InternshipPostStatus {
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
    REJECTED(2, "已拒绝"),
    
    /**
     * 已发布
     */
    PUBLISHED(3, "已发布"),
    
    /**
     * 已关闭
     */
    CLOSED(4, "已关闭");

    private final int code;
    private final String desc;

    InternshipPostStatus(int code, String desc) {
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
    public static InternshipPostStatus getByCode(int code) {
        for (InternshipPostStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

