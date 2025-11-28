package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.SchoolEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学校评价Mapper接口
 */
@Mapper
public interface SchoolEvaluationMapper extends BaseMapper<SchoolEvaluation> {
}

