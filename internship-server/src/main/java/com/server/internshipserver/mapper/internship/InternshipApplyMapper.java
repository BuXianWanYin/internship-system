package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipApply;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习申请Mapper接口
 */
@Mapper
public interface InternshipApplyMapper extends BaseMapper<InternshipApply> {
}

