package com.server.internshipserver.common.enums;

/**
 * 实习计划状态枚举
 * 定义实习计划的状态：草稿、待审核、已通过、已拒绝、已发布
 */
public enum InternshipPlanStatus {
    /**
     * 草稿
     */
    DRAFT(0, "草稿"),
    
    /**
     * 待审核
     */
    PENDING(1, "待审核"),
    
    /**
     * 已通过
     */
    APPROVED(2, "已通过"),
    
    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝"),
    
    /**
     * 已发布
     */
    PUBLISHED(4, "已发布");

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
    InternshipPlanStatus(int code, String desc) {
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
    public static InternshipPlanStatus getByCode(int code) {
        for (InternshipPlanStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

