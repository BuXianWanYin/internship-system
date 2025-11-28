package com.server.internshipserver.common.enums;

/**
 * 解绑状态枚举
 * 定义学生与企业解绑的状态：未解绑、已申请解绑、已解绑、已拒绝、企业管理员已审批
 */
public enum UnbindStatus {
    /**
     * 未解绑
     */
    NOT_APPLIED(0, "未解绑"),
    
    /**
     * 已申请解绑（待企业管理员审批）
     */
    APPLIED(1, "已申请解绑"),
    
    /**
     * 已解绑（离职审批通过）
     */
    UNBOUND(2, "已解绑"),
    
    /**
     * 已拒绝
     */
    REJECTED(3, "已拒绝"),
    
    /**
     * 企业管理员已审批（待学校审批）
     */
    ENTERPRISE_APPROVED(4, "企业管理员已审批");

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
    UnbindStatus(int code, String desc) {
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
    public static UnbindStatus getByCode(int code) {
        for (UnbindStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

