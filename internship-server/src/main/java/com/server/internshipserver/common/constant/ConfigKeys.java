package com.server.internshipserver.common.constant;

/**
 * 系统配置键常量类
 * 定义系统中使用的所有配置键
 */
public class ConfigKeys {
    
    /**
     * 分享码有效期（天）
     */
    public static final String SHARE_CODE_EXPIRE_DAYS = "share_code_expire_days";
    
    /**
     * 分享码长度
     */
    public static final String SHARE_CODE_LENGTH = "share_code_length";
    
    /**
     * 企业相关配置前缀
     */
    public static final String ENTERPRISE_PREFIX = "enterprise_";
    
    /**
     * 学校相关配置前缀
     */
    public static final String SCHOOL_PREFIX = "school_";
    
    /**
     * 学生相关配置前缀
     */
    public static final String STUDENT_PREFIX = "student_";
    
    /**
     * 企业评价权重（综合成绩计算用，小数形式，如0.4表示40%）
     */
    public static final String ENTERPRISE_EVALUATION_WEIGHT = "enterprise_evaluation_weight";
    
    /**
     * 学校评价权重（综合成绩计算用，小数形式，如0.4表示40%）
     */
    public static final String SCHOOL_EVALUATION_WEIGHT = "school_evaluation_weight";
    
    /**
     * 学生自评权重（综合成绩计算用，小数形式，如0.2表示20%）
     */
    public static final String STUDENT_SELF_EVALUATION_WEIGHT = "student_self_evaluation_weight";
    
    private ConfigKeys() {
        // 工具类，禁止实例化
    }
}

