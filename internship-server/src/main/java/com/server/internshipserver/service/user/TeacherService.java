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
     * 添加教师（自动创建用户）
     * @param teacherNo 工号
     * @param realName 真实姓名
     * @param idCard 身份证号
     * @param phone 手机号
     * @param email 邮箱
     * @param collegeId 所属学院ID
     * @param schoolId 所属学校ID
     * @param title 职称
     * @param department 所属部门
     * @param password 初始密码
     * @param status 状态
     * @return 添加的教师信息
     */
    Teacher addTeacherWithUser(String teacherNo, String realName, String idCard, String phone, 
                                String email, Long collegeId, Long schoolId, String title, 
                                String department, String password, Integer status);
    
    /**
     * 更新教师信息
     * @param teacher 教师信息
     * @return 更新后的教师信息
     */
    Teacher updateTeacher(Teacher teacher);
    
    /**
     * 更新教师信息（同时更新用户信息）
     * @param teacherId 教师ID
     * @param userId 用户ID
     * @param teacherNo 工号
     * @param realName 真实姓名
     * @param idCard 身份证号
     * @param phone 手机号
     * @param email 邮箱
     * @param collegeId 所属学院ID
     * @param schoolId 所属学校ID
     * @param title 职称
     * @param department 所属部门
     * @param status 状态
     * @return 更新后的教师信息
     */
    Teacher updateTeacherWithUser(Long teacherId, Long userId, String teacherNo, String realName,
                                  String idCard, String phone, String email, Long collegeId,
                                  Long schoolId, String title, String department, Integer status);
    
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

