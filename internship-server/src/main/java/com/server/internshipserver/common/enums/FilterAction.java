package com.server.internshipserver.common.enums;

/**
 * 筛选操作类型枚举
 */
public enum FilterAction {
    /**
     * 标记感兴趣
     */
    INTERESTED(1, "标记感兴趣"),
    
    /**
     * 安排面试
     */
    ARRANGE_INTERVIEW(2, "安排面试"),
    
    /**
     * 录用
     */
    ACCEPT(3, "录用"),
    
    /**
     * 拒绝
     */
    REJECT(4, "拒绝");

    private final int code;
    private final String desc;

    FilterAction(int code, String desc) {
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
    public static FilterAction getByCode(int code) {
        for (FilterAction action : values()) {
            if (action.code == code) {
                return action;
            }
        }
        return null;
    }
}

