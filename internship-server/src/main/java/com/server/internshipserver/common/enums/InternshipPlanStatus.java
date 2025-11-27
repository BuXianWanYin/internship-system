package com.server.internshipserver.common.enums;

/**
 * 实习计划状态枚举
 */
public enum InternshipPlanStatus {
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),
    
    /**
     * 待审核
     */
    PENDING(1, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(2, "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝"),
    
    /**
     * 已发布
     */
    PUBLISHED(4, "已发布");

    private final int code;
    private final String desc;

    InternshipPlanStatus(int code, String desc) {
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
    public static InternshipPlanStatus getByCode(int code) {
        for (InternshipPlanStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

