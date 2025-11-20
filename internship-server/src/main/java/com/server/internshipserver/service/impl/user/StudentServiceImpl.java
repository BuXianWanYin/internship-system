package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.service.user.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学生管理Service实现类
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    
    @Override
    public Student getStudentByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId)
               .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public Student getStudentByStudentNo(String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return null;
        }
        
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getStudentNo, studentNo)
               .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student addStudent(Student student) {
        // 参数校验
        if (student.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(student.getStudentNo())) {
            throw new BusinessException("学号不能为空");
        }
        if (student.getClassId() == null) {
            throw new BusinessException("班级ID不能为空");
        }
        if (student.getEnrollmentYear() == null) {
            throw new BusinessException("入学年份不能为空");
        }
        
        // 检查学号是否已存在
        Student existStudent = getStudentByStudentNo(student.getStudentNo());
        if (existStudent != null) {
            throw new BusinessException("学号已存在");
        }
        
        // 检查用户ID是否已被使用
        Student existUserStudent = getStudentByUserId(student.getUserId());
        if (existUserStudent != null) {
            throw new BusinessException("该用户已经是学生");
        }
        
        // 设置默认值
        if (student.getStatus() == null) {
            student.setStatus(1); // 默认启用
        }
        student.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(student);
        return student;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student updateStudent(Student student) {
        if (student.getStudentId() == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        // 检查学生是否存在
        Student existStudent = this.getById(student.getStudentId());
        if (existStudent == null || existStudent.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学生不存在");
        }
        
        // 如果修改了学号，检查新学号是否已存在
        if (StringUtils.hasText(student.getStudentNo()) 
                && !student.getStudentNo().equals(existStudent.getStudentNo())) {
            Student studentNoExist = getStudentByStudentNo(student.getStudentNo());
            if (studentNoExist != null) {
                throw new BusinessException("学号已存在");
            }
        }
        
        // 更新
        this.updateById(student);
        return this.getById(student.getStudentId());
    }
    
    @Override
    public Student getStudentById(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        Student student = this.getById(studentId);
        if (student == null || student.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学生不存在");
        }
        
        return student;
    }
    
    @Override
    public Page<Student> getStudentPage(Page<Student> page, String studentNo, Long classId, 
                                         Long majorId, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(studentNo)) {
            wrapper.like(Student::getStudentNo, studentNo);
        }
        if (classId != null) {
            wrapper.eq(Student::getClassId, classId);
        }
        if (majorId != null) {
            wrapper.eq(Student::getMajorId, majorId);
        }
        if (collegeId != null) {
            wrapper.eq(Student::getCollegeId, collegeId);
        }
        if (schoolId != null) {
            wrapper.eq(Student::getSchoolId, schoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Student::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        Student student = this.getById(studentId);
        if (student == null || student.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学生不存在");
        }
        
        // 软删除
        student.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(student);
    }
}

