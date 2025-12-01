package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限信息Mapper接口
<<<<<<< Current (Your changes)
<<<<<<< Current (Your changes)
 * 提供权限信息的数据库操作方法
=======
 * 提供权限信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持权限的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
=======
 * 提供权限信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持权限的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}

