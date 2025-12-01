package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.SchoolEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校评价Mapper接口
 * 提供学校评价信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持学校评价的增删改查等基本操作
 */
@Mapper
public interface SchoolEvaluationMapper extends BaseMapper<SchoolEvaluation> {
}

