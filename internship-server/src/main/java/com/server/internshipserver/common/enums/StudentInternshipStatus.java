package com.server.internshipserver.common.enums;

/**
 * 学生实习状态枚举
 * 定义学生实习的状态：未实习、实习中、已结束
 * 注意：已删除"已离职"状态，解绑后学生实习状态恢复为"未实习"
 */
public enum StudentInternshipStatus {
    /**
     * 未实习
     */
    NOT_STARTED(0, "未实习"),
    
    /**
     * 实习中
     */
    IN_PROGRESS(1, "实习中"),
    
    /**
     * 已结束
     */
    COMPLETED(3, "已结束");

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
    StudentInternshipStatus(int code, String desc) {
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
    public static StudentInternshipStatus getByCode(int code) {
        for (StudentInternshipStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

