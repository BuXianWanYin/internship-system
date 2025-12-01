package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习日志Mapper接口
<<<<<<< Current (Your changes)
<<<<<<< Current (Your changes)
 * 提供实习日志信息的数据库操作方法
=======
 * 提供实习日志信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持实习日志的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
=======
 * 提供实习日志信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持实习日志的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface InternshipLogMapper extends BaseMapper<InternshipLog> {
}

