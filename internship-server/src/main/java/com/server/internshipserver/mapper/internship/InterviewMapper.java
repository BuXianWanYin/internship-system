package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.Interview;
import org.apache.ibatis.annotations.Mapper;

/**
 * 面试Mapper接口
<<<<<<< Current (Your changes)
<<<<<<< Current (Your changes)
 * 提供面试信息的数据库操作方法
=======
 * 提供面试信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持面试信息的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
=======
 * 提供面试信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持面试信息的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface InterviewMapper extends BaseMapper<Interview> {
}

