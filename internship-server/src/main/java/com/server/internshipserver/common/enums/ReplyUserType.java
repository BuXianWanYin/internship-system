package com.server.internshipserver.common.enums;

/**
 * 回复用户类型枚举
 * 定义可以回复反馈的用户类型：企业导师、班主任
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

    /**
     * 类型代码
     */
    private final int code;
    
    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 类型代码
     * @param desc 类型描述
     */
    ReplyUserType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取类型代码
     * 
     * @return 类型代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据代码获取枚举值
     * 
     * @param code 类型代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码不存在
     */
    public static ReplyUserType fromCode(int code) {
        for (ReplyUserType type : ReplyUserType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown ReplyUserType code: " + code);
    }
}

