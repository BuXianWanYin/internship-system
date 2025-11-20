package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业导师信息Mapper接口
 */
@Mapper
public interface EnterpriseMentorMapper extends BaseMapper<EnterpriseMentor> {
}

