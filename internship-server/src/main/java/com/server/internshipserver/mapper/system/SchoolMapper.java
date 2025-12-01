package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校信息Mapper接口
 * 提供学校信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持学校的增删改查等基本操作
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {
}

