package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.UserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联Mapper接口
<<<<<<< Current (Your changes)
 * 提供用户和角色关联关系的数据库操作方法
=======
 * 提供用户角色关联关系的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持用户角色关联的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {
}

