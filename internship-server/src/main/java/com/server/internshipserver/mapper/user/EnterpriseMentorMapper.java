package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业导师信息Mapper接口
 * 提供企业导师信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持企业导师的增删改查等基本操作
 */
@Mapper
public interface EnterpriseMentorMapper extends BaseMapper<EnterpriseMentor> {
}

