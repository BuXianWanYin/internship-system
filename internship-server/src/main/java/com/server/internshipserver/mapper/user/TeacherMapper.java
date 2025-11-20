package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Teacher;
import org.apache.ibatis.annotations.Mapper;

/**
 * 教师信息Mapper接口
 */
@Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
}

