package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.domain.system.SystemConfig;
import com.server.internshipserver.mapper.system.SystemConfigMapper;
import com.server.internshipserver.service.system.SystemConfigService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 系统配置管理Service实现类
 */
@Service
public class SystemConfigServiceImpl extends ServiceImpl<SystemConfigMapper, SystemConfig> implements SystemConfigService {
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemConfig addConfig(SystemConfig config) {
        // 参数校验
        if (!StringUtils.hasText(config.getConfigKey())) {
            throw new BusinessException("配置键不能为空");
        }
        
        // 检查配置键是否已存在
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, config.getConfigKey())
               .eq(SystemConfig::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        SystemConfig existConfig = this.getOne(wrapper);
        if (existConfig != null) {
            throw new BusinessException("配置键已存在");
        }
        
        // 设置默认值
        config.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(config);
        return config;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SystemConfig updateConfig(SystemConfig config) {
        if (config.getConfigId() == null) {
            throw new BusinessException("配置ID不能为空");
        }
        
        // 检查配置是否存在
        SystemConfig existConfig = this.getById(config.getConfigId());
        EntityValidationUtil.validateEntityExists(existConfig, "配置");
        
        // 如果修改了配置键，检查新键是否已存在
        if (StringUtils.hasText(config.getConfigKey()) 
                && !config.getConfigKey().equals(existConfig.getConfigKey())) {
            LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(SystemConfig::getConfigKey, config.getConfigKey())
                   .ne(SystemConfig::getConfigId, config.getConfigId())
                   .eq(SystemConfig::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            SystemConfig keyExistConfig = this.getOne(wrapper);
            if (keyExistConfig != null) {
                throw new BusinessException("配置键已存在");
            }
        }
        
        // 更新
        this.updateById(config);
        return this.getById(config.getConfigId());
    }
    
    @Override
    public SystemConfig getConfigById(Long configId) {
        EntityValidationUtil.validateIdNotNull(configId, "配置ID");
        
        SystemConfig config = this.getById(configId);
        EntityValidationUtil.validateEntityExists(config, "配置");
        
        return config;
    }
    
    @Override
    public SystemConfig getConfigByKey(String configKey) {
        if (!StringUtils.hasText(configKey)) {
            throw new BusinessException("配置键不能为空");
        }
        
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .eq(SystemConfig::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public Page<SystemConfig> getConfigPage(Page<SystemConfig> page, String configKey, String configType) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(SystemConfig::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(configKey)) {
            wrapper.like(SystemConfig::getConfigKey, configKey);
        }
        if (StringUtils.hasText(configType)) {
            wrapper.eq(SystemConfig::getConfigType, configType);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(SystemConfig::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteConfig(Long configId) {
        EntityValidationUtil.validateIdNotNull(configId, "配置ID");
        
        SystemConfig config = this.getById(configId);
        EntityValidationUtil.validateEntityExists(config, "配置");
        
        // 软删除
        config.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(config);
    }
}

