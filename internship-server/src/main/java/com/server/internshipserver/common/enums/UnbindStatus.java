package com.server.internshipserver.common.enums;

/**
 * 解绑状态枚举
 */
public enum UnbindStatus {
    /**
     * 未申请
     */
    NOT_APPLIED(0, "未申请"),
    
    /**
     * 申请解绑
     */
    APPLIED(1, "申请解绑"),
    
    /**
     * 已解绑
     */
    UNBOUND(2, "已解绑"),
    
    /**
     * 解绑被拒绝
     */
    REJECTED(3, "解绑被拒绝");

    private final int code;
    private final String desc;

    UnbindStatus(int code, String desc) {
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
    public static UnbindStatus getByCode(int code) {
        for (UnbindStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

