package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Student;

/**
 * 学生管理Service接口
 */
public interface StudentService extends IService<Student> {
    
    /**
     * 根据用户ID查询学生信息
     * @param userId 用户ID
     * @return 学生信息
     */
    Student getStudentByUserId(Long userId);
    
    /**
     * 根据学号查询学生信息
     * @param studentNo 学号
     * @return 学生信息
     */
    Student getStudentByStudentNo(String studentNo);
    
    /**
     * 添加学生
     * @param student 学生信息
     * @return 添加的学生信息
     */
    Student addStudent(Student student);
    
    /**
     * 更新学生信息
     * @param student 学生信息
     * @return 更新后的学生信息
     */
    Student updateStudent(Student student);
    
    /**
     * 根据ID查询学生详情
     * @param studentId 学生ID
     * @return 学生信息
     */
    Student getStudentById(Long studentId);
    
    /**
     * 分页查询学生列表
     * @param page 分页参数
     * @param studentNo 学号（可选）
     * @param classId 班级ID（可选）
     * @param majorId 专业ID（可选）
     * @param collegeId 学院ID（可选）
     * @param schoolId 学校ID（可选）
     * @return 学生列表
     */
    Page<Student> getStudentPage(Page<Student> page, String studentNo, Long classId, 
                                  Long majorId, Long collegeId, Long schoolId);
    
    /**
     * 停用学生（软删除）
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean deleteStudent(Long studentId);
}

