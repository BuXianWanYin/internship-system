package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.Semester;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学期信息Mapper接口
 */
@Mapper
public interface SemesterMapper extends BaseMapper<Semester> {
}

