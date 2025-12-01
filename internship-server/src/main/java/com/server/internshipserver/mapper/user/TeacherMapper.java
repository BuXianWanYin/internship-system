package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师信息Mapper接口
 * 提供教师信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持教师的增删改查等基本操作
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}

