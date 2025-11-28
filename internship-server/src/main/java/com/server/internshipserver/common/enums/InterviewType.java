package com.server.internshipserver.common.enums;

/**
 * 面试类型枚举
 * 定义面试的方式类型：现场面试、视频面试、电话面试
 */
public enum InterviewType {
    /**
     * 现场面试
     */
    ON_SITE(1, "现场面试"),
    
    /**
     * 视频面试
     */
    VIDEO(2, "视频面试"),
    
    /**
     * 电话面试
     */
    PHONE(3, "电话面试");

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
    InterviewType(int code, String desc) {
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
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static InterviewType getByCode(int code) {
        for (InterviewType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

