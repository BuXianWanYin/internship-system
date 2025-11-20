package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Permission;
import org.apache.ibatis.annotations.Mapper;

/**
 * 权限信息Mapper接口
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {
}

