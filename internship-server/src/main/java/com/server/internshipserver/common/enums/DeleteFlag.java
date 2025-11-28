package com.server.internshipserver.common.enums;

/**
 * 删除标志枚举
 * 用于标记数据是否被逻辑删除，0表示未删除，1表示已删除
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
    DeleteFlag(int code, String desc) {
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
}

