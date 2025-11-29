package com.server.internshipserver.common.enums;

/**
 * 自主实习申请状态枚举
 * 定义自主实习申请的状态：待审核、实习中、审核拒绝、实习结束、已取消
 * 状态码范围：10-19（与合作企业状态 0-9 区分）
 */
public enum SelfInternshipApplyStatus {
    /**
     * 待审核
     * 学生提交自主实习申请后，等待班主任审核
     */
    PENDING(10, "待审核"),
    
    /**
     * 实习中
     * 班主任审核通过后，自动确认上岗，学生开始实习
     * 注意：自主实习审核通过后自动确认，无需学生手动确认
     */
    IN_PROGRESS(11, "实习中"),
    
    /**
     * 审核拒绝
     * 班主任审核拒绝
     */
    REJECTED(12, "审核拒绝"),
    
    /**
     * 实习结束
     * 实习到期或学生主动离职
     */
    COMPLETED(13, "实习结束"),
    
    /**
     * 已取消
     * 学生取消申请
     */
    CANCELLED(14, "已取消");

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
    SelfInternshipApplyStatus(int code, String desc) {
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
    public static SelfInternshipApplyStatus getByCode(int code) {
        for (SelfInternshipApplyStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
    
    /**
     * 判断状态码是否属于自主实习状态范围（10-19）
     * 
     * @param code 状态代码
     * @return 如果是自主实习状态范围，返回true
     */
    public static boolean isSelfInternshipStatus(int code) {
        return code >= 10 && code <= 19;
    }
    
    /**
     * 将旧状态码（0-9）转换为新状态码（10-19）
     * 用于兼容历史数据
     * 
     * @param oldCode 旧状态码
     * @return 新状态码，如果无法转换则返回原值
     */
    public static int convertOldStatusToNew(int oldCode) {
        switch (oldCode) {
            case 0: // 待审核
                return PENDING.getCode();
            case 1: // 已通过
            case 3: // 已录用
                return IN_PROGRESS.getCode();
            case 2: // 已拒绝
                return REJECTED.getCode();
            case 5: // 已取消
                return CANCELLED.getCode();
            default:
                return oldCode;
        }
    }
}

