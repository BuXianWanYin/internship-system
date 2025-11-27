package com.server.internshipserver.common.enums;

/**
 * 考勤类型枚举
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

    private final int code;
    private final String desc;

    AttendanceType(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

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

