package com.server.internshipserver.common.enums;

/**
 * 学生确认状态枚举
 * 定义学生对实习岗位的确认状态：未确认、已确认上岗、已拒绝
 */
public enum StudentConfirmStatus {
    /**
     * 未确认
     */
    NOT_CONFIRMED(0, "未确认"),
    
    /**
     * 已确认上岗
     */
    CONFIRMED(1, "已确认上岗"),
    
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
    StudentConfirmStatus(int code, String desc) {
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
    public static StudentConfirmStatus getByCode(int code) {
        for (StudentConfirmStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

