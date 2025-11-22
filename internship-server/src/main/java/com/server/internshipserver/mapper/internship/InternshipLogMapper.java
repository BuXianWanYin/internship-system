package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习日志Mapper接口
 */
@Mapper
public interface InternshipLogMapper extends BaseMapper<InternshipLog> {
}

