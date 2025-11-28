package com.server.internshipserver.common.enums;

/**
 * 确认状态枚举
 * 定义确认操作的状态：待确认、已确认、已拒绝
 */
public enum ConfirmStatus {
    /**
     * 待确认
     */
    PENDING(0, "待确认"),
    
    /**
     * 已确认
     */
    CONFIRMED(1, "已确认"),
    
    /**
     * 已拒绝
     */
    REJECTED(2, "已拒绝");

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
    ConfirmStatus(int code, String desc) {
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
    public static ConfirmStatus fromCode(int code) {
        for (ConfirmStatus status : ConfirmStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ConfirmStatus code: " + code);
    }
}

