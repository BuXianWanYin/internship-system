package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.SchoolAdmin;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校管理员信息Mapper接口
 */
@Mapper
public interface SchoolAdminMapper extends BaseMapper<SchoolAdmin> {
}

