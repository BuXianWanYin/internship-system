package com.server.internshipserver.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.system.SystemConfig;

/**
 * 系统配置管理Service接口
 * 提供系统配置信息的增删改查等业务功能
 */
public interface SystemConfigService extends IService<SystemConfig> {
    
    /**
     * 添加系统配置
     * @param config 配置信息
     * @return 添加的配置信息
     */
    SystemConfig addConfig(SystemConfig config);
    
    /**
     * 更新系统配置
     * @param config 配置信息
     * @return 更新后的配置信息
     */
    SystemConfig updateConfig(SystemConfig config);
    
    /**
     * 根据ID查询配置详情
     * @param configId 配置ID
     * @return 配置信息
     */
    SystemConfig getConfigById(Long configId);
    
    /**
     * 根据配置键查询配置
     * @param configKey 配置键
     * @return 配置信息
     */
    SystemConfig getConfigByKey(String configKey);
    
    /**
     * 分页查询配置列表
     * @param page 分页参数
     * @param configKey 配置键
     * @param configType 配置类型
     * @return 配置列表
     */
    Page<SystemConfig> getConfigPage(Page<SystemConfig> page, String configKey, String configType);
    
    /**
     * 删除配置（软删除）
     * @param configId 配置ID
     * @return 是否成功
     */
    boolean deleteConfig(Long configId);
}

