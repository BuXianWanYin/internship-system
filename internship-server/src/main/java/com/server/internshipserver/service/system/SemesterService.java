package com.server.internshipserver.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.domain.system.dto.SemesterQueryDTO;

import java.util.List;

/**
 * 学期管理Service接口
 */
public interface SemesterService extends IService<Semester> {
    
    /**
     * 添加学期
     * @param semester 学期信息
     * @return 添加的学期信息
     */
    Semester addSemester(Semester semester);
    
    /**
     * 更新学期信息
     * @param semester 学期信息
     * @return 更新后的学期信息
     */
    Semester updateSemester(Semester semester);
    
    /**
     * 根据ID查询学期详情
     * @param semesterId 学期ID
     * @return 学期信息
     */
    Semester getSemesterById(Long semesterId);
    
    /**
     * 分页查询学期列表
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 学期列表
     */
    Page<Semester> getSemesterPage(Page<Semester> page, SemesterQueryDTO queryDTO);
    
    /**
     * 设置当前学期
     * @param semesterId 学期ID
     * @return 是否成功
     */
    boolean setCurrentSemester(Long semesterId);
    
    /**
     * 获取当前学期
     * @return 当前学期信息
     */
    Semester getCurrentSemester();
    
    /**
     * 删除学期（软删除）
     * @param semesterId 学期ID
     * @return 是否成功
     */
    boolean deleteSemester(Long semesterId);
    
    /**
     * 查询所有学期列表（用于导出）
     * @param queryDTO 查询条件
     * @return 学期列表
     */
    List<Semester> getAllSemesters(SemesterQueryDTO queryDTO);
}

