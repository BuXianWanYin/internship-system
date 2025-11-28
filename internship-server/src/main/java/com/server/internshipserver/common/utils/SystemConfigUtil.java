package com.server.internshipserver.common.utils;

import com.server.internshipserver.domain.system.SystemConfig;
import com.server.internshipserver.service.system.SystemConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;

/**
 * 系统配置工具类
 * 用于读取系统配置值，并提供默认值支持
 */
@Component
public class SystemConfigUtil {
    
    private static SystemConfigUtil instance;
    
    @Autowired
    private SystemConfigService systemConfigService;
    
    @PostConstruct
    public void init() {
        instance = this;
    }
    
    /**
     * 根据配置键获取配置值（字符串）
     * @param configKey 配置键
     * @param defaultValue 默认值（如果配置不存在或为空时返回）
     * @return 配置值
     */
    public static String getConfigValue(String configKey, String defaultValue) {
        if (instance == null || !StringUtils.hasText(configKey)) {
            return defaultValue;
        }
        
        try {
            SystemConfig config = instance.systemConfigService.getConfigByKey(configKey);
            if (config != null && StringUtils.hasText(config.getConfigValue())) {
                return config.getConfigValue();
            }
        } catch (Exception e) {
            // 配置不存在或其他异常，返回默认值
        }
        
        return defaultValue;
    }
    
    /**
     * 根据配置键获取配置值（整数）
     * @param configKey 配置键
     * @param defaultValue 默认值（如果配置不存在或转换失败时返回）
     * @return 配置值
     */
    public static int getConfigValueInt(String configKey, int defaultValue) {
        String value = getConfigValue(configKey, null);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 根据配置键获取配置值（长整数）
     * @param configKey 配置键
     * @param defaultValue 默认值（如果配置不存在或转换失败时返回）
     * @return 配置值
     */
    public static long getConfigValueLong(String configKey, long defaultValue) {
        String value = getConfigValue(configKey, null);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
    
    /**
     * 根据配置键获取配置值（布尔值）
     * @param configKey 配置键
     * @param defaultValue 默认值（如果配置不存在或转换失败时返回）
     * @return 配置值
     */
    public static boolean getConfigValueBoolean(String configKey, boolean defaultValue) {
        String value = getConfigValue(configKey, null);
        if (!StringUtils.hasText(value)) {
            return defaultValue;
        }
        
        String trimmedValue = value.trim().toLowerCase();
        return "true".equals(trimmedValue) || "1".equals(trimmedValue) || "yes".equals(trimmedValue);
    }
}

