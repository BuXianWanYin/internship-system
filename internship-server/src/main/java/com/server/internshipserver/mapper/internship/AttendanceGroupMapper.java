package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.AttendanceGroup;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组Mapper接口
<<<<<<< Current (Your changes)
 * 提供考勤组信息的数据库操作方法
=======
 * 提供考勤组信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持考勤组的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface AttendanceGroupMapper extends BaseMapper<AttendanceGroup> {
}

