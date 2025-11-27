package com.server.internshipserver.common.enums;

/**
 * 是否当前学期枚举
 */
public enum IsCurrent {
    /**
     * 否
     */
    NO(0, "否"),
    
    /**
     * 是
     */
    YES(1, "是");

    private final int code;
    private final String desc;

    IsCurrent(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    public static IsCurrent fromCode(int code) {
        for (IsCurrent value : IsCurrent.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown IsCurrent code: " + code);
    }
}

