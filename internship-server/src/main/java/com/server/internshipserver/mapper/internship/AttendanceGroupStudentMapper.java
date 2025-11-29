package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.AttendanceGroupStudent;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组学生关联Mapper接口
 */
@Mapper
public interface AttendanceGroupStudentMapper extends BaseMapper<AttendanceGroupStudent> {
}

