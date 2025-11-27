package com.server.internshipserver.common.enums;

/**
 * 回复用户类型枚举
 */
public enum ReplyUserType {
    /**
     * 企业导师
     */
    ENTERPRISE_MENTOR(1, "企业导师"),
    
    /**
     * 班主任
     */
    CLASS_TEACHER(2, "班主任");

    private final int code;
    private final String desc;

    ReplyUserType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static ReplyUserType fromCode(int code) {
        for (ReplyUserType type : ReplyUserType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ReplyUserType code: " + code);
    }
}

