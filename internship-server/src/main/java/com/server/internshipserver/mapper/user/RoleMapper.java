package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色信息Mapper接口
 * 提供角色信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持角色的增删改查等基本操作
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}

