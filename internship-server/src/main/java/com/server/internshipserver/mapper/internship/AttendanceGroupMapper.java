package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.AttendanceGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组Mapper接口
 */
@Mapper
public interface AttendanceGroupMapper extends BaseMapper<AttendanceGroup> {
}

