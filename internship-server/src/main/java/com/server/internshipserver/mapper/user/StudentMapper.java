package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.Student;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生信息Mapper接口
 * 提供学生信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持学生的增删改查等基本操作
 */
@Mapper
public interface StudentMapper extends BaseMapper<Student> {
}

