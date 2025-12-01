package com.server.internshipserver.mapper.cooperation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业学校合作关系Mapper接口
 * 提供企业学校合作关系信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持合作关系的增删改查等基本操作
 */
@Mapper
public interface EnterpriseSchoolCooperationMapper extends BaseMapper<EnterpriseSchoolCooperation> {
}

