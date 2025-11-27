package com.server.internshipserver.common.enums;

/**
 * 学生实习状态枚举
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
     * 已离职
     */
    RESIGNED(2, "已离职"),
    
    /**
     * 已结束
     */
    COMPLETED(3, "已结束");

    private final int code;
    private final String desc;

    StudentInternshipStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
    
    /**
     * 根据code获取枚举
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

