package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.AttendanceGroupRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 考勤组规则Mapper接口
 */
@Mapper
public interface AttendanceGroupRuleMapper extends BaseMapper<AttendanceGroupRule> {
}

