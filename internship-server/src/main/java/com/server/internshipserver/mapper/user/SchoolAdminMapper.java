package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.SchoolAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校管理员信息Mapper接口
<<<<<<< Current (Your changes)
 * 提供学校管理员信息的数据库操作方法
=======
 * 提供学校管理员信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持学校管理员的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface SchoolAdminMapper extends BaseMapper<SchoolAdmin> {
}

