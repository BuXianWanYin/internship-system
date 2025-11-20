package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生信息Mapper接口
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}

