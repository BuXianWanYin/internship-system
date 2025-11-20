package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Role;
import org.apache.ibatis.annotations.Mapper;

/**
 * 角色信息Mapper接口
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}

