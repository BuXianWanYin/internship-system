package com.server.internshipserver.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.system.College;

import java.util.List;

/**
 * 学院管理Service接口
 */
public interface CollegeService extends IService<College> {
    
    /**
     * 添加学院
     * @param college 学院信息
     * @return 添加的学院信息
     */
    College addCollege(College college);
    
    /**
     * 更新学院信息
     * @param college 学院信息
     * @return 更新后的学院信息
     */
    College updateCollege(College college);
    
    /**
     * 根据ID查询学院详情
     * @param collegeId 学院ID
     * @return 学院信息
     */
    College getCollegeById(Long collegeId);
    
    /**
     * 分页查询学院列表
     * @param page 分页参数
     * @param collegeName 学院名称
     * @param schoolId 学校ID（可选，用于数据权限过滤）
     * @return 学院列表
     */
    Page<College> getCollegePage(Page<College> page, String collegeName, Long schoolId);
    
    /**
     * 停用学院（软删除）
     * @param collegeId 学院ID
     * @return 是否成功
     */
    boolean deleteCollege(Long collegeId);
    
    /**
     * 查询所有学院列表（用于导出）
     * @param collegeName 学院名称
     * @param schoolId 学校ID
     * @return 学院列表
     */
    List<College> getAllColleges(String collegeName, Long schoolId);
}

