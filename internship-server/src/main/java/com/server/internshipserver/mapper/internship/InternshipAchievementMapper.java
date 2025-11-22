package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipAchievement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 阶段性成果Mapper接口
 */
@Mapper
public interface InternshipAchievementMapper extends BaseMapper<InternshipAchievement> {
}

