package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.AttendanceGroupTimeSlot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组时间段Mapper接口
 */
@Mapper
public interface AttendanceGroupTimeSlotMapper extends BaseMapper<AttendanceGroupTimeSlot> {
}

