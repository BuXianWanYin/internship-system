package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.Attendance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤Mapper接口
 * 提供考勤信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持考勤记录的增删改查等基本操作
 */
@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
}

