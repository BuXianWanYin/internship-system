package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.SchoolAdminMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.service.user.TeacherService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.domain.user.SchoolAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 教师管理Service实现类
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    @Lazy
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SchoolAdminMapper schoolAdminMapper;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Override
    public Teacher getTeacherByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getUserId, userId)
               .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Teacher teacher = this.getOne(wrapper);
        
        // 填充角色信息
        if (teacher != null && teacher.getUserId() != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
            teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
        }
        
        return teacher;
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
        
        // 分配班主任角色（默认角色）
        userService.assignRoleToUser(teacher.getUserId(), "ROLE_CLASS_TEACHER");
        
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
        
        // 权限检查：检查当前用户是否可以编辑该教师对应的用户
        if (!dataPermissionUtil.canEditUser(existTeacher.getUserId())) {
            throw new BusinessException("无权限编辑该教师信息");
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
        
        // 填充角色信息
        if (teacher.getUserId() != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
            teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
        }
        
        return teacher;
    }
    
    @Override
    public Page<Teacher> getTeacherPage(Page<Teacher> page, String teacherNo, Long collegeId, Long schoolId, Integer status) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤：根据用户角色自动添加查询条件（先应用数据权限，再应用前端条件）
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID
        // 学院负责人：添加 college_id = 当前用户学院ID（包括院长自己）
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的教师（包括院长自己）
            // 如果前端传入了collegeId参数，需要验证是否与当前用户的collegeId一致
            if (collegeId != null && !collegeId.equals(currentUserCollegeId)) {
                // 如果传入的collegeId与当前用户的collegeId不一致，返回空结果
                wrapper.eq(Teacher::getCollegeId, -1L);
            } else {
                // 如果没有传入collegeId参数，或者传入的collegeId与当前用户的collegeId一致，则查询本学院的所有教师
                wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
            }
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的教师
            // 如果前端传入了schoolId参数，需要验证是否与当前用户的schoolId一致
            if (schoolId != null && !schoolId.equals(currentUserSchoolId)) {
                // 如果传入的schoolId与当前用户的schoolId不一致，返回空结果
                wrapper.eq(Teacher::getSchoolId, -1L);
            } else {
                // 如果没有传入schoolId参数，或者传入的schoolId与当前用户的schoolId一致，则查询本校的所有教师
                wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
            }
        } else {
            // 系统管理员：可以查看所有教师，应用前端传入的条件
            if (collegeId != null) {
                wrapper.eq(Teacher::getCollegeId, collegeId);
            }
            if (schoolId != null) {
                wrapper.eq(Teacher::getSchoolId, schoolId);
            }
        }
        
        // 其他条件查询（在数据权限过滤之后）
        if (StringUtils.hasText(teacherNo)) {
            wrapper.like(Teacher::getTeacherNo, teacherNo);
        }
        if (status != null) {
            wrapper.eq(Teacher::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Teacher::getCreateTime);
        
        Page<Teacher> result = this.page(page, wrapper);
        
        // 填充每个教师的角色信息
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Teacher teacher : result.getRecords()) {
                if (teacher.getUserId() != null) {
                    List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
                    teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
                }
            }
        }
        
        return result;
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
                                      String department, String roleCode, String password, Integer status) {
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
        
        // 如果department为空，自动填充为学院名称
        if (!StringUtils.hasText(department) && collegeId != null) {
            College college = collegeMapper.selectById(collegeId);
            if (college != null) {
                teacher.setDepartment(college.getCollegeName());
            } else {
                teacher.setDepartment(department);
            }
        } else {
            teacher.setDepartment(department);
        }
        
        if (status == null) {
            teacher.setStatus(1); // 默认启用
        } else {
            teacher.setStatus(status);
        }
        teacher.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存教师
        this.save(teacher);
        
        // 分配角色：如果指定了角色代码，使用指定的角色，否则默认分配班主任角色
        String finalRoleCode = StringUtils.hasText(roleCode) ? roleCode : "ROLE_CLASS_TEACHER";
        
        // 权限检查：检查当前用户是否可以分配该角色
        if (!dataPermissionUtil.canAssignRole(finalRoleCode)) {
            throw new BusinessException("无权限分配该角色：" + finalRoleCode);
        }
        
        userService.assignRoleToUser(teacher.getUserId(), finalRoleCode);
        
        // 如果分配的是学校管理员角色，需要创建SchoolAdmin记录
        if ("ROLE_SCHOOL_ADMIN".equals(finalRoleCode) && schoolId != null) {
            // 检查是否已经存在SchoolAdmin记录
            SchoolAdmin existSchoolAdmin = schoolAdminMapper.selectOne(
                    new LambdaQueryWrapper<SchoolAdmin>()
                            .eq(SchoolAdmin::getUserId, teacher.getUserId())
                            .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (existSchoolAdmin == null) {
                // 创建SchoolAdmin记录
                SchoolAdmin schoolAdmin = new SchoolAdmin();
                schoolAdmin.setUserId(teacher.getUserId());
                schoolAdmin.setSchoolId(schoolId);
                schoolAdmin.setStatus(status != null ? status : 1);
                schoolAdmin.setDeleteFlag(DeleteFlag.NORMAL.getCode());
                schoolAdminMapper.insert(schoolAdmin);
            }
        }
        
        return teacher;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher updateTeacherWithUser(Long teacherId, Long userId, String teacherNo, String realName,
                                         String idCard, String phone, String email, Long collegeId,
                                         Long schoolId, String title, String department, String roleCode, Integer status) {
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
        
        // 权限检查：检查当前用户是否可以编辑该教师对应的用户
        if (!dataPermissionUtil.canEditUser(existTeacher.getUserId())) {
            throw new BusinessException("无权限编辑该教师信息");
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
            // 如果修改了学院，自动更新部门为学院名称
            College college = collegeMapper.selectById(collegeId);
            if (college != null) {
                teacher.setDepartment(college.getCollegeName());
            }
        }
        if (schoolId != null) {
            teacher.setSchoolId(schoolId);
        }
        if (StringUtils.hasText(title)) {
            teacher.setTitle(title);
        }
        // 如果department为空且collegeId不为空，自动填充为学院名称
        if (!StringUtils.hasText(department) && collegeId != null) {
            College college = collegeMapper.selectById(collegeId);
            if (college != null) {
                teacher.setDepartment(college.getCollegeName());
            }
        } else if (StringUtils.hasText(department)) {
            teacher.setDepartment(department);
        }
        if (status != null) {
            teacher.setStatus(status);
        }
        this.updateById(teacher);
        

        if (StringUtils.hasText(roleCode)) {
            userService.assignRoleToUser(userId, roleCode);
        }
        
        return this.getById(teacherId);
    }
    
    @Override
    public List<Teacher> getTeacherListBySchool(Long schoolId, Long collegeId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .eq(Teacher::getStatus, 1); // 只查询启用的教师
        
        if (schoolId != null) {
            wrapper.eq(Teacher::getSchoolId, schoolId);
        }
        if (collegeId != null) {
            wrapper.eq(Teacher::getCollegeId, collegeId);
        }
        
        // 数据权限过滤
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
        }
        
        wrapper.orderByDesc(Teacher::getCreateTime);
        return this.list(wrapper);
    }
}

