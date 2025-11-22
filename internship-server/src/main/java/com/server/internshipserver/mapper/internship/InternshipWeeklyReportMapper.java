package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import org.apache.ibatis.annotations.Mapper;

/**
 * 周报Mapper接口
 */
@Mapper
public interface InternshipWeeklyReportMapper extends BaseMapper<InternshipWeeklyReport> {
}

