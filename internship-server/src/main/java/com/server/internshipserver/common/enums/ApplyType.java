package com.server.internshipserver.common.enums;

/**
 * 申请类型枚举
 */
public enum ApplyType {
    /**
     * 合作企业
     */
    COOPERATION(1, "合作企业"),
    
    /**
     * 自主实习
     */
    SELF(2, "自主实习");

    private final int code;
    private final String desc;

    ApplyType(int code, String desc) {
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
    public static ApplyType getByCode(int code) {
        for (ApplyType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

