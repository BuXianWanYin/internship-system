package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.EnterpriseRegisterSchool;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业注册申请院校关联Mapper接口
 */
@Mapper
public interface EnterpriseRegisterSchoolMapper extends BaseMapper<EnterpriseRegisterSchool> {
}

