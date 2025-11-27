package com.server.internshipserver.common.enums;

/**
 * 实习申请状态枚举
 */
public enum InternshipApplyStatus {
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
     * 已录用
     */
    ACCEPTED(3, "已录用"),
    
    /**
     * 已拒绝录用
     */
    REJECTED_ACCEPTANCE(4, "已拒绝录用");

    private final int code;
    private final String desc;

    InternshipApplyStatus(int code, String desc) {
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
    public static InternshipApplyStatus getByCode(int code) {
        for (InternshipApplyStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

