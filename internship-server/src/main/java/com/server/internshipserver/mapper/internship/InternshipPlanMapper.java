package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipPlan;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习计划Mapper接口
<<<<<<< Current (Your changes)
<<<<<<< Current (Your changes)
 * 提供实习计划信息的数据库操作方法
=======
 * 提供实习计划信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持实习计划的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
=======
 * 提供实习计划信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持实习计划的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface InternshipPlanMapper extends BaseMapper<InternshipPlan> {
}

