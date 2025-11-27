package com.server.internshipserver.common.constant;

/**
 * 系统常量定义
 */
public class Constants {
    
    /**
     * Token相关常量
     */
    public static final String TOKEN_HEADER = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_KEY_PREFIX = "jwt:token:";
    
    /**
     * Redis Key前缀
     */
    public static final String REDIS_PERMISSION_PREFIX = "permission:";
    
    /**
     * 班级分享码相关
     */
    public static final int SHARE_CODE_LENGTH = 8;
    public static final int SHARE_CODE_EXPIRE_DAYS = 30;
    
    /**
     * 角色常量
     */
    public static final String ROLE_SYSTEM_ADMIN = "ROLE_SYSTEM_ADMIN";
    public static final String ROLE_SCHOOL_ADMIN = "ROLE_SCHOOL_ADMIN";
    public static final String ROLE_COLLEGE_LEADER = "ROLE_COLLEGE_LEADER";
    public static final String ROLE_CLASS_TEACHER = "ROLE_CLASS_TEACHER";
    public static final String ROLE_ENTERPRISE_ADMIN = "ROLE_ENTERPRISE_ADMIN";
    public static final String ROLE_ENTERPRISE_MENTOR = "ROLE_ENTERPRISE_MENTOR";
    public static final String ROLE_STUDENT = "ROLE_STUDENT";
    
    /**
     * 默认密码
     */
    public static final String DEFAULT_PASSWORD = "123456";
    
    /**
     * 密码长度限制
     */
    public static final int PASSWORD_MIN_LENGTH = 6;
    public static final int PASSWORD_MAX_LENGTH = 20;
    
    /**
     * 评分范围
     */
    public static final int SCORE_MIN = 0;
    public static final int SCORE_MAX = 100;
    
    /**
     * 日期时间格式
     */
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    
    private Constants() {
        // 工具类，禁止实例化
    }
}

