package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipAchievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 阶段性成果Mapper接口
<<<<<<< Current (Your changes)
 * 提供实习阶段性成果信息的数据库操作方法
=======
 * 提供阶段性成果信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持阶段性成果的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface InternshipAchievementMapper extends BaseMapper<InternshipAchievement> {
}

