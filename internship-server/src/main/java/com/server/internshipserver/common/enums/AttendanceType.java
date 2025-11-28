package com.server.internshipserver.common.enums;

/**
 * 考勤类型枚举
 * 定义学生实习期间的考勤状态类型
 */
public enum AttendanceType {
    /**
     * 出勤
     */
    ATTENDANCE(1, "出勤"),
    
    /**
     * 迟到
     */
    LATE(2, "迟到"),
    
    /**
     * 早退
     */
    EARLY_LEAVE(3, "早退"),
    
    /**
     * 请假
     */
    LEAVE(4, "请假"),
    
    /**
     * 缺勤
     */
    ABSENT(5, "缺勤"),
    
    /**
     * 休息
     */
    REST(6, "休息");

    /**
     * 类型代码
     */
    private final int code;
    
    /**
     * 类型描述
     */
    private final String desc;

    /**
     * 构造函数
     * 
     * @param code 类型代码
     * @param desc 类型描述
     */
    AttendanceType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    /**
     * 获取类型代码
     * 
     * @return 类型代码
     */
    public int getCode() {
        return code;
    }

    /**
     * 获取类型描述
     * 
     * @return 类型描述
     */
    public String getDesc() {
        return desc;
    }

    public static AttendanceType fromCode(int code) {
        for (AttendanceType type : AttendanceType.values()) {
            if (type.getCode() == code) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown AttendanceType code: " + code);
    }
}

