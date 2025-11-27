package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.dto.TeacherAddDTO;
import com.server.internshipserver.domain.user.dto.TeacherUpdateDTO;

import java.util.List;

/**
 * 教师管理Service接口
 */
public interface TeacherService extends IService<Teacher> {
    
    /**
     * 根据用户ID查询教师信息
     * @param userId 用户ID
     * @return 教师信息
     */
    Teacher getTeacherByUserId(Long userId);
    
    /**
     * 根据工号查询教师信息
     * @param teacherNo 工号
     * @return 教师信息
     */
    Teacher getTeacherByTeacherNo(String teacherNo);
    
    /**
     * 添加教师
     * @param teacher 教师信息
     * @return 添加的教师信息
     */
    Teacher addTeacher(Teacher teacher);
    
    /**
     * 添加教师（自动创建用户）
     * @param addDTO 教师添加信息
     * @return 添加的教师信息
     */
    Teacher addTeacherWithUser(TeacherAddDTO addDTO);
    
    /**
     * 更新教师信息
     * @param teacher 教师信息
     * @return 更新后的教师信息
     */
    Teacher updateTeacher(Teacher teacher);
    
    /**
     * 更新教师信息（同时更新用户信息）
     * @param updateDTO 教师更新信息
     * @return 更新后的教师信息
     */
    Teacher updateTeacherWithUser(TeacherUpdateDTO updateDTO);
    
    /**
     * 根据ID查询教师详情
     * @param teacherId 教师ID
     * @return 教师信息
     */
    Teacher getTeacherById(Long teacherId);
    
    /**
     * 分页查询教师列表
     * @param page 分页参数
     * @param teacherNo 工号（可选）
     * @param collegeId 学院ID（可选）
     * @param schoolId 学校ID（可选）
     * @param status 状态：1-启用，0-禁用（可选）
     * @return 教师列表
     */
    Page<Teacher> getTeacherPage(Page<Teacher> page, String teacherNo, Long collegeId, Long schoolId, Integer status);
    
    /**
     * 停用教师（软删除）
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean deleteTeacher(Long teacherId);
    
    /**
     * 根据学校ID查询教师列表（不分页，用于下拉选择）
     * @param schoolId 学校ID
     * @param collegeId 学院ID（可选）
     * @return 教师列表
     */
    List<Teacher> getTeacherListBySchool(Long schoolId, Long collegeId);
}

