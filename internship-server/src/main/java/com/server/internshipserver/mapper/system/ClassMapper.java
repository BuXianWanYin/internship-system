package com.server.internshipserver.mapper.system;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.system.Class;
import org.apache.ibatis.annotations.Mapper;

/**
 * 班级信息Mapper接口
 */
@Mapper
public interface ClassMapper extends BaseMapper<Class> {
}

