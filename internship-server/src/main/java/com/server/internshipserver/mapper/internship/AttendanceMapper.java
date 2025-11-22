package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.Attendance;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤Mapper接口
 */
@Mapper
public interface AttendanceMapper extends BaseMapper<Attendance> {
}

