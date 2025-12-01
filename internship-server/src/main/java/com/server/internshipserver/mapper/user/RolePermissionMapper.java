package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.RolePermission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色权限关联Mapper接口
<<<<<<< Current (Your changes)
 * 提供角色和权限关联关系的数据库操作方法
=======
 * 提供角色权限关联关系的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持角色权限关联的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {
}

