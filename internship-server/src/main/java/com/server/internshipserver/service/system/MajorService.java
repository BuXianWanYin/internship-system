package com.server.internshipserver.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.system.Major;

import java.util.List;

/**
 * 专业管理Service接口
 * 提供专业信息的增删改查等业务功能
 */
public interface MajorService extends IService<Major> {
    
    /**
     * 添加专业
     * @param major 专业信息
     * @return 添加的专业信息
     */
    Major addMajor(Major major);
    
    /**
     * 更新专业信息
     * @param major 专业信息
     * @return 更新后的专业信息
     */
    Major updateMajor(Major major);
    
    /**
     * 根据ID查询专业详情
     * @param majorId 专业ID
     * @return 专业信息
     */
    Major getMajorById(Long majorId);
    
    /**
     * 分页查询专业列表
     * @param page 分页参数
     * @param majorName 专业名称
     * @param collegeId 学院ID（可选，用于数据权限过滤）
     * @param schoolId 学校ID（可选，用于筛选）
     * @return 专业列表
     */
    Page<Major> getMajorPage(Page<Major> page, String majorName, Long collegeId, Long schoolId);
    
    /**
     * 停用专业（软删除）
     * @param majorId 专业ID
     * @return 是否成功
     */
    boolean deleteMajor(Long majorId);
    
    /**
     * 查询所有专业列表（用于导出）
     * @param majorName 专业名称
     * @param collegeId 学院ID
     * @param schoolId 学校ID
     * @return 专业列表
     */
    List<Major> getAllMajors(String majorName, Long collegeId, Long schoolId);
}

