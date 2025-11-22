package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.Interview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试Mapper接口
 */
@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {
}

