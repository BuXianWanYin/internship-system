package com.server.internshipserver.common.enums;

/**
 * 合作关系状态枚举
 * 定义学校与企业之间的合作关系状态：进行中、已结束
 */
public enum CooperationStatus {
    /**
     * 进行中
     */
    IN_PROGRESS(1, "进行中"),
    
    /**
     * 已结束
     */
    ENDED(2, "已结束");

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
    CooperationStatus(int code, String desc) {
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
    public static CooperationStatus getByCode(int code) {
        for (CooperationStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

