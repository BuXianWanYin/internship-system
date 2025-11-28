package com.server.internshipserver.common.enums;

/**
 * 申请类型枚举
 * 定义实习申请的类型：合作企业实习或自主实习
 */
public enum ApplyType {
    /**
     * 合作企业
     */
    COOPERATION(1, "合作企业"),
    
    /**
     * 自主实习
     */
    SELF(2, "自主实习");

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
    ApplyType(int code, String desc) {
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
    
    /**
     * 根据代码获取枚举值
     * 
     * @param code 类型代码
     * @return 对应的枚举值，如果不存在则返回null
     */
    public static ApplyType getByCode(int code) {
        for (ApplyType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        return null;
    }
}

