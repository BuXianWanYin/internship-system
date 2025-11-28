package com.server.internshipserver.common.enums;

/**
 * 筛选操作类型枚举
 * 定义企业对实习申请进行筛选时的操作类型：标记感兴趣、安排面试、录用、拒绝
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

    /**
     * 操作代码
     */
    private final int code;
    
    /**
     * 操作描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 操作代码
     * @param desc 操作描述
     */
    FilterAction(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取操作代码
     * 
     * @return 操作代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取操作描述
     * 
     * @return 操作描述
     */
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 操作代码
     * @return 对应的枚举值，如果不存在则返回null
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

