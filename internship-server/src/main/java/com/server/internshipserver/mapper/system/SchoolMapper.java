package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.School;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校信息Mapper接口
 */
@Mapper
public interface SchoolMapper extends BaseMapper<School> {
}

