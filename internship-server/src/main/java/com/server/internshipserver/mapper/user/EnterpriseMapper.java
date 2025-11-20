package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Enterprise;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业信息Mapper接口
 */
@Mapper
public interface EnterpriseMapper extends BaseMapper<Enterprise> {
}

