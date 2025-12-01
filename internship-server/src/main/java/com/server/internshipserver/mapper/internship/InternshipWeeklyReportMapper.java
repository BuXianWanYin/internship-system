package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 周报Mapper接口
 * 提供周报信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持周报的增删改查等基本操作
 */
@Mapper
public interface InternshipWeeklyReportMapper extends BaseMapper<InternshipWeeklyReport> {
}

