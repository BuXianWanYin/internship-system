package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.SystemConfig;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统配置Mapper接口
 * 提供系统配置信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持系统配置的增删改查等基本操作
 */
@Mapper
public interface SystemConfigMapper extends BaseMapper<SystemConfig> {
}

