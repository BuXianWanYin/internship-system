package com.server.internshipserver.common.enums;

/**
 * 面试结果枚举
 * 定义面试的结果：通过、不通过、待定
 */
public enum InterviewResult {
    /**
     * 通过
     */
    PASSED(1, "通过"),
    
    /**
     * 不通过
     */
    FAILED(2, "不通过"),
    
    /**
     * 待定
     */
    PENDING(3, "待定");

    /**
     * 结果代码
     */
    private final int code;
    
    /**
     * 结果描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 结果代码
     * @param desc 结果描述
     */
    InterviewResult(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取结果代码
     * 
     * @return 结果代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取结果描述
     * 
     * @return 结果描述
     */
    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 结果代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static InterviewResult getByCode(int code) {
        for (InterviewResult result : values()) {
            if (result.code == code) {
                return result;
            }
        }
        return null;
    }
}

