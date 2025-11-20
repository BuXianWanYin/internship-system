package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.user.SchoolAdmin;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.UserRole;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.mapper.user.SchoolAdminMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.UserRoleMapper;
import com.server.internshipserver.service.user.PermissionService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.service.user.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private SchoolAdminMapper schoolAdminMapper;
    
    @Override
    public UserInfo getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUsername, username)
               .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo addUser(UserInfo user) {
        // 参数校验
        if (!StringUtils.hasText(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (!StringUtils.hasText(user.getRealName())) {
            throw new BusinessException("真实姓名不能为空");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUsername, user.getUsername())
               .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        UserInfo existUser = this.getOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        user.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(user);
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo updateUser(UserInfo user) {
        if (user.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 检查用户是否存在
        UserInfo existUser = this.getById(user.getUserId());
        if (existUser == null || existUser.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 如果修改了用户名，检查新用户名是否已存在
        if (StringUtils.hasText(user.getUsername()) 
                && !user.getUsername().equals(existUser.getUsername())) {
            LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInfo::getUsername, user.getUsername())
                   .ne(UserInfo::getUserId, user.getUserId())
                   .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            UserInfo usernameExistUser = this.getOne(wrapper);
            if (usernameExistUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 如果修改了密码，需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        
        // 更新
        this.updateById(user);
        return this.getById(user.getUserId());
    }
    
    @Override
    public UserInfo getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        return user;
    }
    
    @Override
    public Page<UserInfo> getUserPage(Page<UserInfo> page, String username, String realName, String phone, 
                                      Integer status, String roleCodes, Long schoolId, Long collegeId, Long classId) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(username)) {
            wrapper.like(UserInfo::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(UserInfo::getRealName, realName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(UserInfo::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(UserInfo::getStatus, status);
        }
        
        // 角色筛选：通过user_role表查询
        List<Long> roleFilterUserIds = null;
        if (StringUtils.hasText(roleCodes)) {
            // 解析角色代码（支持逗号分隔的多个角色）
            String[] roleCodeArray = roleCodes.split(",");
            List<String> roleCodeList = new ArrayList<>();
            for (String roleCode : roleCodeArray) {
                if (StringUtils.hasText(roleCode.trim())) {
                    roleCodeList.add(roleCode.trim());
                }
            }
            
            if (!roleCodeList.isEmpty()) {
                // 查询这些角色的role_id列表
                List<Role> roles = new ArrayList<>();
                for (String roleCode : roleCodeList) {
                    Role role = roleService.getRoleByRoleCode(roleCode);
                    if (role != null) {
                        roles.add(role);
                    }
                }
                
                if (!roles.isEmpty()) {
                    List<Long> roleIds = roles.stream()
                            .map(Role::getRoleId)
                            .collect(Collectors.toList());
                    
                    // 查询拥有这些角色的user_id列表
                    List<UserRole> userRoles = userRoleMapper.selectList(
                            new LambdaQueryWrapper<UserRole>()
                                    .in(UserRole::getRoleId, roleIds)
                                    .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                    .select(UserRole::getUserId)
                    );
                    if (userRoles != null && !userRoles.isEmpty()) {
                        roleFilterUserIds = userRoles.stream()
                                .map(UserRole::getUserId)
                                .distinct()
                                .collect(Collectors.toList());
                    } else {
                        // 如果没有用户拥有这些角色，返回空结果
                        roleFilterUserIds = new ArrayList<>();
                    }
                }
            }
        }
        
        // 组织筛选：通过Student、Teacher表关联查询
        List<Long> orgFilterUserIds = null;
        if (classId != null || collegeId != null || schoolId != null) {
            List<Long> orgUserIds = new ArrayList<>();
            
            // 班级筛选（优先级最高）
            if (classId != null) {
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getClassId, classId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getUserId)
                );
                if (students != null && !students.isEmpty()) {
                    orgUserIds.addAll(students.stream()
                            .map(Student::getUserId)
                            .collect(Collectors.toList()));
                }
            }
            // 学院筛选
            else if (collegeId != null) {
                // 查询本院的学生
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getCollegeId, collegeId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getUserId)
                );
                if (students != null && !students.isEmpty()) {
                    orgUserIds.addAll(students.stream()
                            .map(Student::getUserId)
                            .collect(Collectors.toList()));
                }
                // 查询本院的教师
                List<Teacher> teachers = teacherMapper.selectList(
                        new LambdaQueryWrapper<Teacher>()
                                .eq(Teacher::getCollegeId, collegeId)
                                .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Teacher::getUserId)
                );
                if (teachers != null && !teachers.isEmpty()) {
                    orgUserIds.addAll(teachers.stream()
                            .map(Teacher::getUserId)
                            .collect(Collectors.toList()));
                }
            }
            // 学校筛选
            else if (schoolId != null) {
                // 查询本校的学生
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getSchoolId, schoolId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getUserId)
                );
                if (students != null && !students.isEmpty()) {
                    orgUserIds.addAll(students.stream()
                            .map(Student::getUserId)
                            .collect(Collectors.toList()));
                }
                // 查询本校的教师
                List<Teacher> teachers = teacherMapper.selectList(
                        new LambdaQueryWrapper<Teacher>()
                                .eq(Teacher::getSchoolId, schoolId)
                                .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Teacher::getUserId)
                );
                if (teachers != null && !teachers.isEmpty()) {
                    orgUserIds.addAll(teachers.stream()
                            .map(Teacher::getUserId)
                            .collect(Collectors.toList()));
                }
                // 查询本校的学校管理员
                List<SchoolAdmin> schoolAdmins = schoolAdminMapper.selectList(
                        new LambdaQueryWrapper<SchoolAdmin>()
                                .eq(SchoolAdmin::getSchoolId, schoolId)
                                .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(SchoolAdmin::getUserId)
                );
                if (schoolAdmins != null && !schoolAdmins.isEmpty()) {
                    orgUserIds.addAll(schoolAdmins.stream()
                            .map(SchoolAdmin::getUserId)
                            .collect(Collectors.toList()));
                }
            }
            
            if (!orgUserIds.isEmpty()) {
                orgFilterUserIds = orgUserIds.stream()
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                // 如果没有符合条件的用户，返回空结果
                orgFilterUserIds = new ArrayList<>();
            }
        }
        
        // 合并角色筛选和组织筛选的user_id列表
        List<Long> finalUserIds = null;
        if (roleFilterUserIds != null && orgFilterUserIds != null) {
            // 两个筛选都存在，取交集
            finalUserIds = roleFilterUserIds.stream()
                    .filter(orgFilterUserIds::contains)
                    .collect(Collectors.toList());
        } else if (roleFilterUserIds != null) {
            // 只有角色筛选
            finalUserIds = roleFilterUserIds;
        } else if (orgFilterUserIds != null) {
            // 只有组织筛选
            finalUserIds = orgFilterUserIds;
        }
        
        // 应用筛选结果
        if (finalUserIds != null) {
            if (finalUserIds.isEmpty()) {
                // 如果没有符合条件的用户，返回空结果
                wrapper.eq(UserInfo::getUserId, -1L);
            } else {
                wrapper.in(UserInfo::getUserId, finalUserIds);
            }
        }
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        // UserInfo表没有school_id、college_id、class_id字段，需要通过关联查询实现
        // 系统管理员：不添加过滤条件
        // 学校管理员：只能查看本校的用户（通过Student、Teacher、SchoolAdmin表关联）
        // 学院负责人：只能查看本院的用户（通过Student、Teacher表关联）
        // 班主任：只能查看本班的学生用户（通过Student表关联）
        // 其他角色：只能查看个人信息（这里不限制，由Controller层控制）
        
        Long currentUserClassId = dataPermissionUtil.getCurrentUserClassId();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserClassId != null) {
            // 班主任：只能查看本班的学生用户
            // 先查询本班的学生user_id列表，然后使用in方法
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, currentUserClassId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getUserId)
            );
            if (students != null && !students.isEmpty()) {
                List<Long> userIds = students.stream()
                        .map(Student::getUserId)
                        .collect(Collectors.toList());
                wrapper.in(UserInfo::getUserId, userIds);
            } else {
                // 如果没有学生，返回空结果
                wrapper.eq(UserInfo::getUserId, -1L);
            }
        } else if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的用户（学生和教师）
            // 先查询本院的学生user_id列表
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getCollegeId, currentUserCollegeId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getUserId)
            );
            // 再查询本院的教师user_id列表
            List<Teacher> teachers = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getCollegeId, currentUserCollegeId)
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Teacher::getUserId)
            );
            // 合并user_id列表
            List<Long> userIds = new ArrayList<>();
            if (students != null && !students.isEmpty()) {
                userIds.addAll(students.stream()
                        .map(Student::getUserId)
                        .collect(Collectors.toList()));
            }
            if (teachers != null && !teachers.isEmpty()) {
                userIds.addAll(teachers.stream()
                        .map(Teacher::getUserId)
                        .collect(Collectors.toList()));
            }
            if (!userIds.isEmpty()) {
                wrapper.in(UserInfo::getUserId, userIds);
            } else {
                // 如果没有用户，返回空结果
                wrapper.eq(UserInfo::getUserId, -1L);
            }
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的用户（学生、教师、学校管理员）
            // 先查询本校的学生user_id列表
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getSchoolId, currentUserSchoolId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getUserId)
            );
            // 再查询本校的教师user_id列表
            List<Teacher> teachers = teacherMapper.selectList(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getSchoolId, currentUserSchoolId)
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Teacher::getUserId)
            );
            // 再查询本校的学校管理员user_id列表
            List<SchoolAdmin> schoolAdmins = schoolAdminMapper.selectList(
                    new LambdaQueryWrapper<SchoolAdmin>()
                            .eq(SchoolAdmin::getSchoolId, currentUserSchoolId)
                            .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(SchoolAdmin::getUserId)
            );
            // 合并user_id列表
            List<Long> userIds = new ArrayList<>();
            if (students != null && !students.isEmpty()) {
                userIds.addAll(students.stream()
                        .map(Student::getUserId)
                        .collect(Collectors.toList()));
            }
            if (teachers != null && !teachers.isEmpty()) {
                userIds.addAll(teachers.stream()
                        .map(Teacher::getUserId)
                        .collect(Collectors.toList()));
            }
            if (schoolAdmins != null && !schoolAdmins.isEmpty()) {
                userIds.addAll(schoolAdmins.stream()
                        .map(SchoolAdmin::getUserId)
                        .collect(Collectors.toList()));
            }
            if (!userIds.isEmpty()) {
                wrapper.in(UserInfo::getUserId, userIds);
            } else {
                // 如果没有用户，返回空结果
                wrapper.eq(UserInfo::getUserId, -1L);
            }
        }
        // 系统管理员不添加限制
        
        // 按创建时间倒序
        wrapper.orderByDesc(UserInfo::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 软删除
        user.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Long userId, String roleCode) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException("角色代码不能为空");
        }
        
        // 检查用户是否存在
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 查询角色是否存在
        Role role = roleService.getRoleByRoleCode(roleCode);
        if (role == null || role.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("角色不存在：" + roleCode);
        }
        
        // 检查用户是否已经拥有该角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
               .eq(UserRole::getRoleId, role.getRoleId())
               .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        UserRole existUserRole = userRoleMapper.selectOne(wrapper);
        if (existUserRole != null) {
            // 用户已经拥有该角色，直接返回成功
            return true;
        }
        
        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getRoleId());
        userRole.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        boolean success = userRoleMapper.insert(userRole) > 0;
        
        // 角色分配成功后，清除用户权限缓存
        if (success) {
            permissionService.clearUserPermissionCache(userId);
        }
        
        return success;
    }
}

