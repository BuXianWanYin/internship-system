package com.server.internshipserver.common.enums;

/**
 * 审核状态枚举
 * 用于周报、日志、成果等内容的审核状态：待审核、已通过、已拒绝
 */
public enum ReviewStatus {
    /**
     * 待审核
     */
    PENDING(0, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(1, "已通过"),
    
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
    ReviewStatus(int code, String desc) {
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
    public static ReviewStatus fromCode(int code) {
        for (ReviewStatus status : ReviewStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown ReviewStatus code: " + code);
    }
}

