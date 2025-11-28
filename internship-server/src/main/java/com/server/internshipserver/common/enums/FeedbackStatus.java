package com.server.internshipserver.common.enums;

/**
 * 反馈状态枚举
 * 定义反馈处理的状态：待处理、处理中（已回复）、已解决、已关闭
 */
public enum FeedbackStatus {
    /**
     * 待处理
     */
    PENDING(0, "待处理"),
    
    /**
     * 处理中（已回复）
     */
    REPLIED(1, "处理中"),
    
    /**
     * 已解决
     */
    SOLVED(2, "已解决"),
    
    /**
     * 已关闭
     */
    CLOSED(3, "已关闭");

    /**
     * 状态代码
     */
    private final int code;
    
    /**
     * 状态描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 状态代码
     * @param desc 状态描述
     */
    FeedbackStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取状态代码
     * 
     * @return 状态代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取状态描述
     * 
     * @return 状态描述
     */
    public String getDesc() {
        return desc;
    }

    /**
     * 根据代码获取枚举值
     * 
     * @param code 状态代码
     * @return 对应的枚举值
     * @throws IllegalArgumentException 如果代码不存在
     */
    public static FeedbackStatus fromCode(int code) {
        for (FeedbackStatus status : FeedbackStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown FeedbackStatus code: " + code);
    }
}

