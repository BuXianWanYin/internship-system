package com.server.internshipserver.mapper.evaluation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.evaluation.SelfEvaluation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 学生自评Mapper接口
<<<<<<< Current (Your changes)
 * 提供学生自评信息的数据库操作方法
=======
 * 提供学生自评信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持学生自评的增删改查等基本操作
>>>>>>> Incoming (Background Agent changes)
 */
@Mapper
public interface SelfEvaluationMapper extends BaseMapper<SelfEvaluation> {
}

