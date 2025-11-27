package com.server.internshipserver.common.enums;

/**
 * 合作关系状态枚举
 */
public enum CooperationStatus {
    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),
    
    /**
     * 已结束
     */
    ENDED(2, "已结束");

    private final int code;
    private final String desc;

    CooperationStatus(int code, String desc) {
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
    public static CooperationStatus getByCode(int code) {
        for (CooperationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

