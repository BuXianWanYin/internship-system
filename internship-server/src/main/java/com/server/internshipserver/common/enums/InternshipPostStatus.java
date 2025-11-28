package com.server.internshipserver.common.enums;

/**
 * 实习岗位状态枚举
 * 定义实习岗位的状态：待审核、已通过、已拒绝、已发布、已关闭
 */
public enum InternshipPostStatus {
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
    REJECTED(2, "已拒绝"),
    
    /**
     * 已发布
     */
    PUBLISHED(3, "已发布"),
    
    /**
     * 已关闭
     */
    CLOSED(4, "已关闭");

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
    InternshipPostStatus(int code, String desc) {
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
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static InternshipPostStatus getByCode(int code) {
        for (InternshipPostStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

