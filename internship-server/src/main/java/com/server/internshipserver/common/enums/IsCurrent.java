package com.server.internshipserver.common.enums;

/**
 * 是否当前学期枚举
 * 用于标记学期是否为当前学期：是、否
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

    /**
     * 标志代码
     */
    private final int code;
    
    /**
     * 标志描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 标志代码
     * @param desc 标志描述
     */
    IsCurrent(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取标志代码
     * 
     * @return 标志代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取标志描述
     * 
     * @return 标志描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据代码获取枚举值
     * 
     * @param code 标志代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码不存在
     */
    public static IsCurrent fromCode(int code) {
        for (IsCurrent value : IsCurrent.values()) {
            if (value.getCode() == code) {
                return value;
            }
        }
        throw new IllegalArgumentException("Unknown IsCurrent code: " + code);
    }
}

