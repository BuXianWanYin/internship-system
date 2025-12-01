package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.Semester;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学期信息Mapper接口
<<<<<<< Current (Your changes)
 * 提供学期信息的数据库操作方法
=======
 * 提供学期信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持学期的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface SemesterMapper extends BaseMapper<Semester> {
}

