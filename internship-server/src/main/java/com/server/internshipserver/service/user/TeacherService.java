package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Teacher;

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
     * 更新教师信息
     * @param teacher 教师信息
     * @return 更新后的教师信息
     */
    Teacher updateTeacher(Teacher teacher);
    
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
     * @return 教师列表
     */
    Page<Teacher> getTeacherPage(Page<Teacher> page, String teacherNo, Long collegeId, Long schoolId);
    
    /**
     * 停用教师（软删除）
     * @param teacherId 教师ID
     * @return 是否成功
     */
    boolean deleteTeacher(Long teacherId);
}

