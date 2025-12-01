package com.server.internshipserver.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.system.School;

import java.util.List;

/**
 * 学校管理Service接口
 */
public interface SchoolService extends IService<School> {
    
    /**
     * 添加学校
     * @param school 学校信息
     * @return 添加的学校信息
     */
    School addSchool(School school);
    
    /**
     * 更新学校信息
     * @param school 学校信息
     * @return 更新后的学校信息
     */
    School updateSchool(School school);
    
    /**
     * 根据ID查询学校详情
     * @param schoolId 学校ID
     * @return 学校信息
     */
    School getSchoolById(Long schoolId);
    
    /**
     * 分页查询学校列表
     * @param page 分页参数
     * @param schoolName 学校名称
     * @param schoolCode 学校代码
     * @return 学校列表
     */
    Page<School> getSchoolPage(Page<School> page, String schoolName, String schoolCode);
    
    /**
     * 停用学校（软删除）
     * @param schoolId 学校ID
     * @return 是否成功
     */
    boolean deleteSchool(Long schoolId);
    
    /**
     * 查询所有学校列表（用于导出）
     * @param schoolName 学校名称
     * @param schoolCode 学校代码
     * @return 学校列表
     */
    List<School> getAllSchools(String schoolName, String schoolCode);
    
    /**
     * 公开查询所有学校列表（用于企业注册等公开场景，不进行权限过滤）
     * @return 学校列表
     */
    List<School> getPublicSchoolList();
}

