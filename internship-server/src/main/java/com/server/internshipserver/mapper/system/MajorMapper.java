package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.Major;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业信息Mapper接口
<<<<<<< Current (Your changes)
<<<<<<< Current (Your changes)
 * 提供专业信息的数据库操作方法
=======
 * 提供专业信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持专业的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
=======
 * 提供专业信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持专业的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface MajorMapper extends BaseMapper<Major> {
}

