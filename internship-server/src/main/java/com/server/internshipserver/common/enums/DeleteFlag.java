package com.server.internshipserver.common.enums;

/**
 * 删除标志枚举
 */
public enum DeleteFlag {
    /**
     * 未删除
     */
    NORMAL(0, "未删除"),
    
    /**
     * 已删除
     */
    DELETED(1, "已删除");

    private final int code;
    private final String desc;

    DeleteFlag(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}

