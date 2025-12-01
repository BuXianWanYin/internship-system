package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.EnterpriseEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业评价Mapper接口
<<<<<<< Current (Your changes)
<<<<<<< Current (Your changes)
 * 提供企业评价信息的数据库操作方法
=======
 * 提供企业评价信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持企业评价的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
=======
 * 提供企业评价信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持企业评价的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface EnterpriseEvaluationMapper extends BaseMapper<EnterpriseEvaluation> {
}

