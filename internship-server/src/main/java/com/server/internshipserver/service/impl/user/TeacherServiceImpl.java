package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.service.user.TeacherService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 教师管理Service实现类
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
    
    @Override
    public Teacher getTeacherByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getUserId, userId)
               .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public Teacher getTeacherByTeacherNo(String teacherNo) {
        if (!StringUtils.hasText(teacherNo)) {
            return null;
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getTeacherNo, teacherNo)
               .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher addTeacher(Teacher teacher) {
        // 参数校验
        if (teacher.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(teacher.getTeacherNo())) {
            throw new BusinessException("工号不能为空");
        }
        
        // 检查工号是否已存在
        Teacher existTeacher = getTeacherByTeacherNo(teacher.getTeacherNo());
        if (existTeacher != null) {
            throw new BusinessException("工号已存在");
        }
        
        // 检查用户ID是否已被使用
        Teacher existUserTeacher = getTeacherByUserId(teacher.getUserId());
        if (existUserTeacher != null) {
            throw new BusinessException("该用户已经是教师");
        }
        
        // 设置默认值
        if (teacher.getStatus() == null) {
            teacher.setStatus(1); // 默认启用
        }
        teacher.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(teacher);
        return teacher;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher updateTeacher(Teacher teacher) {
        if (teacher.getTeacherId() == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        // 检查教师是否存在
        Teacher existTeacher = this.getById(teacher.getTeacherId());
        if (existTeacher == null || existTeacher.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("教师不存在");
        }
        
        // 如果修改了工号，检查新工号是否已存在
        if (StringUtils.hasText(teacher.getTeacherNo()) 
                && !teacher.getTeacherNo().equals(existTeacher.getTeacherNo())) {
            Teacher teacherNoExist = getTeacherByTeacherNo(teacher.getTeacherNo());
            if (teacherNoExist != null) {
                throw new BusinessException("工号已存在");
            }
        }
        
        // 更新
        this.updateById(teacher);
        return this.getById(teacher.getTeacherId());
    }
    
    @Override
    public Teacher getTeacherById(Long teacherId) {
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        Teacher teacher = this.getById(teacherId);
        if (teacher == null || teacher.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("教师不存在");
        }
        
        return teacher;
    }
    
    @Override
    public Page<Teacher> getTeacherPage(Page<Teacher> page, String teacherNo, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(teacherNo)) {
            wrapper.like(Teacher::getTeacherNo, teacherNo);
        }
        if (collegeId != null) {
            wrapper.eq(Teacher::getCollegeId, collegeId);
        }
        if (schoolId != null) {
            wrapper.eq(Teacher::getSchoolId, schoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Teacher::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacher(Long teacherId) {
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        Teacher teacher = this.getById(teacherId);
        if (teacher == null || teacher.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("教师不存在");
        }
        
        // 软删除
        teacher.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(teacher);
    }
}

