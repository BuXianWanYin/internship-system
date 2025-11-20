package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.Major;
import org.apache.ibatis.annotations.Mapper;

/**
 * 专业信息Mapper接口
 */
@Mapper
public interface MajorMapper extends BaseMapper<Major> {
}

