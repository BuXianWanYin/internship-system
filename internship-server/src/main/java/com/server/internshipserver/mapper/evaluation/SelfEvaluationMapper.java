package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.SelfEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生自评Mapper接口
 */
@Mapper
public interface SelfEvaluationMapper extends BaseMapper<SelfEvaluation> {
}

