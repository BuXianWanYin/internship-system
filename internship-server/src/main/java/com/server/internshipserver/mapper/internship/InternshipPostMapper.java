package com.server.internshipserver.mapper.internship;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.internship.InternshipPost;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实习岗位Mapper接口
 */
@Mapper
public interface InternshipPostMapper extends BaseMapper<InternshipPost> {
}

