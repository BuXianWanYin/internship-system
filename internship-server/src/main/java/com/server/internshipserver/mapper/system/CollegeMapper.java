package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.College;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学院信息Mapper接口
 */
@Mapper
public interface CollegeMapper extends BaseMapper<College> {
}

