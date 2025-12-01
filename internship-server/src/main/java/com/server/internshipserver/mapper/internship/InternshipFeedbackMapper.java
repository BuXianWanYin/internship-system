package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipFeedback;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问题反馈Mapper接口
<<<<<<< Current (Your changes)
 * 提供实习问题反馈信息的数据库操作方法
=======
 * 提供问题反馈信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持问题反馈的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface InternshipFeedbackMapper extends BaseMapper<InternshipFeedback> {
}

