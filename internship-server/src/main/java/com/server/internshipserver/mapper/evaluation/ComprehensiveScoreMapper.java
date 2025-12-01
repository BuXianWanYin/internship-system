package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;
import org.apache.ibatis.annotations.Mapper;

/**
 * 综合成绩Mapper接口
 * 提供综合成绩信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持综合成绩的增删改查等基本操作
 */
@Mapper
public interface ComprehensiveScoreMapper extends BaseMapper<ComprehensiveScore> {
}

