package com.server.internshipserver.common.enums;

/**
 * 学生确认状态枚举
 */
public enum StudentConfirmStatus {
    /**
     * 未确认
     */
    NOT_CONFIRMED(0, "未确认"),
    
    /**
     * 已确认上岗
     */
    CONFIRMED(1, "已确认上岗"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");

    private final int code;
    private final String desc;

    StudentConfirmStatus(int code, String desc) {
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
    public static StudentConfirmStatus getByCode(int code) {
        for (StudentConfirmStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

