package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.EnterpriseEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业评价Mapper接口
 */
@Mapper
public interface EnterpriseEvaluationMapper extends BaseMapper<EnterpriseEvaluation> {
}

