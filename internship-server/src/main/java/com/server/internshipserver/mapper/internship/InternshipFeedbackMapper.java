package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问题反馈Mapper接口
 */
@Mapper
public interface InternshipFeedbackMapper extends BaseMapper<InternshipFeedback> {
}

