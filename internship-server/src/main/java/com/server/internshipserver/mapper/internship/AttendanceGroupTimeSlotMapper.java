package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.AttendanceGroupTimeSlot;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组时间段Mapper接口
 * 提供考勤组时间段信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持考勤组时间段的增删改查等基本操作
 */
@Mapper
public interface AttendanceGroupTimeSlotMapper extends BaseMapper<AttendanceGroupTimeSlot> {
}

