package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习申请Mapper接口
 * 提供实习申请信息的数据库操作方法，继承MyBatis Plus的BaseMapper
 * 支持实习申请的增删改查等基本操作
 */
@Mapper
public interface InternshipApplyMapper extends BaseMapper<InternshipApply> {
}

