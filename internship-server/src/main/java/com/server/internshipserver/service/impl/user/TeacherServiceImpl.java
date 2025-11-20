package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.service.user.TeacherService;
import com.server.internshipserver.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 教师管理Service实现类
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserService userService;
    
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
        
        // 分配指导教师角色
        userService.assignRoleToUser(teacher.getUserId(), "ROLE_INSTRUCTOR");
        
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
    public Page<Teacher> getTeacherPage(Page<Teacher> page, String teacherNo, Long collegeId, Long schoolId, Integer status) {
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
        if (status != null) {
            wrapper.eq(Teacher::getStatus, status);
        }
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID
        // 学院负责人：添加 college_id = 当前用户学院ID
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的教师
            wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的教师
            wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
        }
        // 系统管理员不添加限制
        
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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher addTeacherWithUser(String teacherNo, String realName, String idCard, String phone,
                                      String email, Long collegeId, Long schoolId, String title,
                                      String department, String password, Integer status) {
        // 参数校验
        if (!StringUtils.hasText(teacherNo)) {
            throw new BusinessException("工号不能为空");
        }
        if (!StringUtils.hasText(realName)) {
            throw new BusinessException("真实姓名不能为空");
        }
        if (!StringUtils.hasText(password)) {
            throw new BusinessException("初始密码不能为空");
        }
        if (collegeId == null) {
            throw new BusinessException("所属学院ID不能为空");
        }
        
        // 检查工号是否已存在
        Teacher existTeacher = getTeacherByTeacherNo(teacherNo);
        if (existTeacher != null) {
            throw new BusinessException("工号已存在");
        }
        
        // 生成用户名（使用工号）
        String username = teacherNo;
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名（工号）已存在");
        }
        
        // 创建用户
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(password); // UserService会自动加密
        user.setRealName(realName);
        user.setIdCard(idCard);
        user.setPhone(phone);
        user.setEmail(email);
        if (status == null) {
            user.setStatus(1); // 默认启用
        } else {
            user.setStatus(status);
        }
        user = userService.addUser(user);
        
        // 创建教师记录
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getUserId());
        teacher.setTeacherNo(teacherNo);
        teacher.setCollegeId(collegeId);
        teacher.setSchoolId(schoolId);
        teacher.setTitle(title);
        teacher.setDepartment(department);
        if (status == null) {
            teacher.setStatus(1); // 默认启用
        } else {
            teacher.setStatus(status);
        }
        teacher.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存教师
        this.save(teacher);
        
        // 分配指导教师角色
        userService.assignRoleToUser(teacher.getUserId(), "ROLE_INSTRUCTOR");
        
        return teacher;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher updateTeacherWithUser(Long teacherId, Long userId, String teacherNo, String realName,
                                         String idCard, String phone, String email, Long collegeId,
                                         Long schoolId, String title, String department, Integer status) {
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 检查教师是否存在
        Teacher existTeacher = this.getById(teacherId);
        if (existTeacher == null || existTeacher.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("教师不存在");
        }
        
        // 如果修改了工号，检查新工号是否已存在
        if (StringUtils.hasText(teacherNo) && !teacherNo.equals(existTeacher.getTeacherNo())) {
            Teacher teacherNoExist = getTeacherByTeacherNo(teacherNo);
            if (teacherNoExist != null) {
                throw new BusinessException("工号已存在");
            }
        }
        
        // 更新用户信息
        UserInfo user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(realName)) {
            user.setRealName(realName);
        }
        if (StringUtils.hasText(idCard)) {
            user.setIdCard(idCard);
        }
        if (StringUtils.hasText(phone)) {
            user.setPhone(phone);
        }
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        }
        if (status != null) {
            user.setStatus(status);
        }
        userService.updateUser(user);
        
        // 更新教师信息
        Teacher teacher = new Teacher();
        teacher.setTeacherId(teacherId);
        if (StringUtils.hasText(teacherNo)) {
            teacher.setTeacherNo(teacherNo);
        }
        if (collegeId != null) {
            teacher.setCollegeId(collegeId);
        }
        if (schoolId != null) {
            teacher.setSchoolId(schoolId);
        }
        if (StringUtils.hasText(title)) {
            teacher.setTitle(title);
        }
        if (StringUtils.hasText(department)) {
            teacher.setDepartment(department);
        }
        if (status != null) {
            teacher.setStatus(status);
        }
        this.updateById(teacher);
        
        return this.getById(teacherId);
    }
}

