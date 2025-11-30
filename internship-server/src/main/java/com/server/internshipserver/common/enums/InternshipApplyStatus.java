package com.server.internshipserver.common.enums;

/**
 * 实习申请状态枚举
 * 定义学生实习申请的状态：待审核、已通过、已拒绝、已录用、已拒绝录用、已取消
 * 注意：已删除"已离职"状态，解绑后申请状态保持为"已录用"，通过解绑状态标识
 */
public enum InternshipApplyStatus {
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
     * 已录用
     */
    ACCEPTED(3, "已录用"),
    
    /**
     * 已拒绝录用
     */
    REJECTED_ACCEPTANCE(4, "已拒绝录用"),
    
    /**
     * 已取消
     */
    CANCELLED(5, "已取消"),
    
    /**
     * 实习结束
     * 合作企业实习已结束
     */
    COMPLETED(7, "实习结束"),
    
    /**
     * 已评价
     * 所有评价已完成，综合成绩已计算
     */
    EVALUATED(8, "已评价");

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
    InternshipApplyStatus(int code, String desc) {
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
    public static InternshipApplyStatus getByCode(int code) {
        for (InternshipApplyStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

