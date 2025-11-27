package com.server.internshipserver.common.utils;

import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;

import java.lang.reflect.Method;

/**
 * 实体默认值设置工具类
 * 用于封装设置实体默认值的重复逻辑
 */
public final class EntityDefaultValueUtil {
    
    private EntityDefaultValueUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 设置实体的默认值
     * 包括：deleteFlag设置为NORMAL，如果status为null则设置默认status
     * 
     * @param entity 实体对象
     * @param defaultStatus 默认状态值（如果status为null时使用），可以为null表示不设置
     * @param <T> 实体类型
     */
    public static <T> void setDefaultValues(T entity, Integer defaultStatus) {
        if (entity == null) {
            return;
        }
        
        Class<?> entityClass = entity.getClass();
        
        try {
            // 设置deleteFlag为NORMAL
            try {
                Method setDeleteFlagMethod = entityClass.getMethod("setDeleteFlag", Integer.class);
                setDeleteFlagMethod.invoke(entity, DeleteFlag.NORMAL.getCode());
            } catch (NoSuchMethodException e) {
                // 如果实体没有deleteFlag字段，忽略
            }
            
            // 如果提供了默认状态且status为null，设置默认状态
            if (defaultStatus != null) {
                try {
                    Method getStatusMethod = entityClass.getMethod("getStatus");
                    Integer currentStatus = (Integer) getStatusMethod.invoke(entity);
                    if (currentStatus == null) {
                        Method setStatusMethod = entityClass.getMethod("setStatus", Integer.class);
                        setStatusMethod.invoke(entity, defaultStatus);
                    }
                } catch (NoSuchMethodException e) {
                    // 如果实体没有status字段，忽略
                }
            }
        } catch (Exception e) {
            // 反射调用失败，忽略（不应该发生）
            throw new RuntimeException("设置实体默认值失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 设置实体的默认值（不设置status）
     * 只设置deleteFlag为NORMAL
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     */
    public static <T> void setDefaultValues(T entity) {
        setDefaultValues(entity, null);
    }
    
    /**
     * 设置实体的默认值，使用UserStatus.ENABLED作为默认状态
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     */
    public static <T> void setDefaultValuesWithEnabledStatus(T entity) {
        setDefaultValues(entity, UserStatus.ENABLED.getCode());
    }
    
    /**
     * 设置实体的默认值，使用UserStatus.DISABLED作为默认状态
     * 
     * @param entity 实体对象
     * @param <T> 实体类型
     */
    public static <T> void setDefaultValuesWithDisabledStatus(T entity) {
        setDefaultValues(entity, UserStatus.DISABLED.getCode());
    }
}

