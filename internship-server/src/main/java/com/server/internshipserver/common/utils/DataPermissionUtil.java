package com.server.internshipserver.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.SchoolAdmin;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.SchoolAdminMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.EnterpriseMentorMapper;
import com.server.internshipserver.mapper.cooperation.EnterpriseSchoolCooperationMapper;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.CooperationStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 数据权限工具类
 * 用于获取当前登录用户的数据权限范围（学校ID、学院ID、班级ID等）
 */
@Component
public class DataPermissionUtil {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private SchoolAdminMapper schoolAdminMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private EnterpriseMentorMapper enterpriseMentorMapper;
    
    @Autowired
    private EnterpriseSchoolCooperationMapper cooperationMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    /**
     * 判断当前用户是否为系统管理员
     */
    public boolean isSystemAdmin() {
        UserDetails userDetails = UserUtil.getCurrentUserDetails();
        if (userDetails == null) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals(Constants.ROLE_SYSTEM_ADMIN));
    }
    
    /**
     * 获取当前用户的学校ID
     * @return 学校ID，如果无法获取则返回null
     */
    public Long getCurrentUserInfoSchoolId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取学校ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 学校管理员：从SchoolAdmin表获取
        // 学院负责人：从Teacher表获取
        // 班主任：从Teacher表获取，然后关联Class表获取学校ID
        // 学生：从Student表获取
        
        // 检查是否为学校管理员
        if (hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN)) {
            SchoolAdmin schoolAdmin = schoolAdminMapper.selectOne(
                    new LambdaQueryWrapper<SchoolAdmin>()
                            .eq(SchoolAdmin::getUserId, user.getUserId())
                            .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (schoolAdmin != null) {
                return schoolAdmin.getSchoolId();
            }
        }
        
        // 检查是否为学院负责人、班主任
        if (hasRole(roleCodes, Constants.ROLE_COLLEGE_LEADER) || hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, user.getUserId())
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (teacher != null) {
                return teacher.getSchoolId();
            }
        }
        
        // 检查是否为学生
        if (hasRole(roleCodes, Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                return student.getSchoolId();
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前用户的学院ID
     * @return 学院ID，如果无法获取则返回null
     */
    public Long getCurrentUserInfoCollegeId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取学院ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 学院负责人、班主任：从Teacher表获取
        if (hasRole(roleCodes, Constants.ROLE_COLLEGE_LEADER) || hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, user.getUserId())
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (teacher != null) {
                return teacher.getCollegeId();
            }
        }
        
        // 学生：从Student表获取
        if (hasRole(roleCodes, Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                return student.getCollegeId();
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前用户的班级ID
     * @return 班级ID，如果无法获取则返回null
     */
    public Long getCurrentUserInfoClassId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取班级ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 班主任：从Class表的class_teacher_id获取（class_teacher_id存储的是user_id）
        if (hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
            // 直接通过user_id查询所有管理的班级（支持多班级）
            List<Class> classList = classMapper.selectList(
                    new LambdaQueryWrapper<Class>()
                            .eq(Class::getClassTeacherId, user.getUserId())
                            .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .orderByAsc(Class::getCreateTime)
            );
            if (classList != null && !classList.isEmpty()) {
                // 返回第一个班级ID（保持向后兼容）
                return classList.get(0).getClassId();
            }
        }
        
        // 学生：从Student表获取
        if (hasRole(roleCodes, Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                return student.getClassId();
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前用户的学校ID（简化方法名）
     * @return 学校ID，如果无法获取则返回null
     */
    public Long getCurrentUserSchoolId() {
        return getCurrentUserInfoSchoolId();
    }
    
    /**
     * 获取当前用户的学院ID（简化方法名）
     * @return 学院ID，如果无法获取则返回null
     */
    public Long getCurrentUserCollegeId() {
        return getCurrentUserInfoCollegeId();
    }
    
    /**
     * 获取当前用户的用户ID
     * @return 用户ID，如果无法获取则返回null
     */
    public Long getCurrentUserId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        return user.getUserId();
    }
    
    /**
     * 检查当前用户是否有指定角色
     * @param roleCode 角色代码
     * @return 如果有该角色返回true，否则返回false
     */
    public boolean hasRole(String roleCode) {
        if (roleCode == null) {
            return false;
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return false;
        }
        
        // 查询用户角色
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        return hasRole(roleCodes, roleCode);
    }
    
    /**
     * 检查角色列表是否包含指定角色（静态方法）
     * @param roleCodes 角色代码列表
     * @param roleCode 要检查的角色代码
     * @return 是否包含该角色
     */
    public static boolean hasRole(List<String> roleCodes, String roleCode) {
        return roleCodes != null && roleCodes.contains(roleCode);
    }
    
    /**
     * 获取当前用户的班级ID（简化方法名）
     * @return 班级ID，如果无法获取则返回null（如果管理多个班级，返回第一个）
     */
    public Long getCurrentUserClassId() {
        List<Long> classIds = getCurrentUserClassIds();
        if (classIds != null && !classIds.isEmpty()) {
            return classIds.get(0);
        }
        return null;
    }
    
    /**
     * 获取当前用户的班级ID列表（支持多班级权限）
     * @return 班级ID列表，如果无法获取则返回null或空列表
     */
    public List<Long> getCurrentUserClassIds() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取班级ID列表
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 班主任：从Class表的class_teacher_id获取（class_teacher_id存储的是user_id）
        if (hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
            // 直接通过user_id查询所有管理的班级
            List<Class> classList = classMapper.selectList(
                    new LambdaQueryWrapper<Class>()
                            .eq(Class::getClassTeacherId, user.getUserId())
                            .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .orderByAsc(Class::getCreateTime)
            );
            if (classList != null && !classList.isEmpty()) {
                return classList.stream()
                        .map(Class::getClassId)
                        .collect(Collectors.toList());
            }
        }
        
        // 学生：从Student表获取
        if (hasRole(roleCodes, Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null && student.getClassId() != null) {
                return Collections.singletonList(student.getClassId());
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前用户的企业ID
     * @return 企业ID，如果无法获取则返回null
     */
    public Long getCurrentUserEnterpriseId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取企业ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 企业管理员：从Enterprise表获取
        if (hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
            Enterprise enterprise = enterpriseMapper.selectOne(
                    new LambdaQueryWrapper<Enterprise>()
                            .eq(Enterprise::getUserId, user.getUserId())
                            .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (enterprise != null) {
                return enterprise.getEnterpriseId();
            }
        }
        
        // 企业导师：从EnterpriseMentor表获取
        if (hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
            EnterpriseMentor mentor = enterpriseMentorMapper.selectOne(
                    new LambdaQueryWrapper<EnterpriseMentor>()
                            .eq(EnterpriseMentor::getUserId, user.getUserId())
                            .eq(EnterpriseMentor::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (mentor != null) {
                return mentor.getEnterpriseId();
            }
        }
        
        return null;
    }
    
    /**
     * 获取当前用户的教师ID（用于指导教师）
     * @return 教师ID，如果无法获取则返回null
     */
    public Long getCurrentUserTeacherId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return null;
        }
        
        // 从Teacher表获取教师ID
        Teacher teacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getUserId, user.getUserId())
                        .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (teacher != null) {
            return teacher.getTeacherId();
        }
        
        return null;
    }
    
    /**
     * 获取当前学生的学生ID
     * @return 学生ID，如果当前用户不是学生则返回null
     */
    public Long getCurrentStudentId() {
        Long userId = getCurrentUserId();
        if (userId == null) {
            return null;
        }
        
        // 检查当前用户是否有学生角色
        if (!hasRole(Constants.ROLE_STUDENT)) {
            return null;
        }
        
        // 查询学生信息
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (student != null) {
            return student.getStudentId();
        }
        
        return null;
    }
    
    /**
     * 获取当前用户有合作关系的企业ID列表
     * @return 企业ID列表，如果无法获取则返回null（表示不限制）
     */
    public List<Long> getCooperationEnterpriseIds() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        Long currentUserSchoolId = getCurrentUserSchoolId();
        Long currentUserClassId = getCurrentUserClassId();
        
        // 学校管理员：获取和本校有合作关系的企业ID列表
        if (currentUserSchoolId != null && currentUserClassId == null) {
            // 查询合作关系
            LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EnterpriseSchoolCooperation::getSchoolId, currentUserSchoolId)
                   .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
                   .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            List<EnterpriseSchoolCooperation> cooperations = cooperationMapper.selectList(wrapper);
            
            if (cooperations.isEmpty()) {
                // 如果没有合作关系，返回空列表（表示没有可查看的企业）
                return Collections.emptyList();
            }
            
            return cooperations.stream()
                    .map(EnterpriseSchoolCooperation::getEnterpriseId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        // 班主任：获取和管理的班级有合作关系的企业ID列表
        if (currentUserClassId != null) {
            // 先查询本班学生所属学校
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, currentUserClassId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getSchoolId)
            );
            
            if (students == null || students.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 获取学校ID列表（去重）
            List<Long> schoolIds = students.stream()
                    .map(Student::getSchoolId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (schoolIds.isEmpty()) {
                return Collections.emptyList();
            }
            
            // 查询这些学校有合作关系的企业ID列表
            LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
            wrapper.in(EnterpriseSchoolCooperation::getSchoolId, schoolIds)
                   .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
                   .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            List<EnterpriseSchoolCooperation> cooperations = cooperationMapper.selectList(wrapper);
            
            if (cooperations.isEmpty()) {
                return Collections.emptyList();
            }
            
            return cooperations.stream()
                    .map(EnterpriseSchoolCooperation::getEnterpriseId)
                    .distinct()
                    .collect(Collectors.toList());
        }
        
        return null;
    }
    
    /**
     * 检查当前用户是否可以编辑指定用户
     * 权限规则：
     * 1. 系统管理员可以编辑所有用户
     * 2. 学校管理员不能编辑系统管理员，可以编辑其他所有用户
     * 3. 学院负责人不能编辑系统管理员、学校管理员，可以编辑教师、学生等
     * 4. 班主任不能编辑系统管理员、学校管理员、学院负责人，可以编辑学生等
     * 
     * @param targetUserId 目标用户ID
     * @return true-可以编辑，false-不能编辑
     */
    public boolean canEditUser(Long targetUserId) {
        if (targetUserId == null) {
            return false;
        }
        
        // 系统管理员可以编辑所有用户
        if (isSystemAdmin()) {
            return true;
        }
        
        // 获取目标用户的角色
        List<String> targetUserRoles = userMapper.selectRoleCodesByUserId(targetUserId);
        if (targetUserRoles == null || targetUserRoles.isEmpty()) {
            return false;
        }
        
        // 获取当前用户的角色
        String currentUsername = UserUtil.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }
        
        UserInfo currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, currentUsername)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (currentUser == null) {
            return false;
        }
        
        List<String> currentUserRoles = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (currentUserRoles == null || currentUserRoles.isEmpty()) {
            return false;
        }
        
        // 任何人都不能编辑系统管理员
        if (targetUserRoles.contains(Constants.ROLE_SYSTEM_ADMIN)) {
            return false;
        }
        
        // 学校管理员不能编辑系统管理员（上面已处理），可以编辑其他所有用户
        if (currentUserRoles.contains(Constants.ROLE_SCHOOL_ADMIN)) {
            return true;
        }
        
        // 学院负责人不能编辑系统管理员、学校管理员
        if (currentUserRoles.contains(Constants.ROLE_COLLEGE_LEADER)) {
            if (targetUserRoles.contains(Constants.ROLE_SCHOOL_ADMIN)) {
                return false;
            }
            // 可以编辑教师、学生等
            return true;
        }
        
        // 班主任不能编辑系统管理员、学校管理员、学院负责人
        if (currentUserRoles.contains(Constants.ROLE_CLASS_TEACHER)) {
            if (targetUserRoles.contains(Constants.ROLE_SCHOOL_ADMIN) || 
                targetUserRoles.contains(Constants.ROLE_COLLEGE_LEADER)) {
                return false;
            }
            // 可以编辑学生等
            return true;
        }
        
        // 其他角色默认不能编辑其他用户
        return false;
    }
    
    /**
     * 检查当前用户是否可以分配指定角色
     * 权限规则：
     * 1. 系统管理员可以分配所有角色
     * 2. 学校管理员不能分配系统管理员角色，可以分配其他角色
     * 3. 学院负责人不能分配系统管理员、学校管理员角色，可以分配教师、学生等角色
     * 4. 班主任不能分配系统管理员、学校管理员、学院负责人角色，可以分配学生角色
     * 
     * @param roleCode 角色代码
     * @return true-可以分配，false-不能分配
     */
    public boolean canAssignRole(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return false;
        }
        
        // 系统管理员可以分配所有角色
        if (isSystemAdmin()) {
            return true;
        }
        
        // 获取当前用户的角色
        String currentUsername = UserUtil.getCurrentUsername();
        if (currentUsername == null) {
            return false;
        }
        
        UserInfo currentUser = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, currentUsername)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (currentUser == null) {
            return false;
        }
        
        List<String> currentUserRoles = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (currentUserRoles == null || currentUserRoles.isEmpty()) {
            return false;
        }
        
        // 任何人都不能分配系统管理员角色
        if (Constants.ROLE_SYSTEM_ADMIN.equals(roleCode)) {
            return false;
        }
        
        // 学校管理员不能分配系统管理员角色（上面已处理）
        // 可以分配学校相关角色（ROLE_SCHOOL_ADMIN、ROLE_COLLEGE_LEADER、ROLE_CLASS_TEACHER、ROLE_STUDENT）
        // 不能分配企业相关角色
        if (currentUserRoles.contains(Constants.ROLE_SCHOOL_ADMIN)) {
            // 不能分配企业相关角色
            if (Constants.ROLE_ENTERPRISE_ADMIN.equals(roleCode) || 
                Constants.ROLE_ENTERPRISE_MENTOR.equals(roleCode)) {
                return false;
            }
            // 可以分配学校相关角色
            return true;
        }
        
        // 学院负责人不能分配系统管理员、学校管理员角色
        // 只能分配教师相关角色（ROLE_COLLEGE_LEADER、ROLE_CLASS_TEACHER）和学生角色
        if (currentUserRoles.contains(Constants.ROLE_COLLEGE_LEADER)) {
            if (Constants.ROLE_SCHOOL_ADMIN.equals(roleCode)) {
                return false;
            }
            // 可以分配的教师相关角色和学生角色
            if (Constants.ROLE_COLLEGE_LEADER.equals(roleCode) || 
                Constants.ROLE_CLASS_TEACHER.equals(roleCode) ||
                Constants.ROLE_STUDENT.equals(roleCode)) {
                return true;
            }
            // 不能分配企业相关角色等其他角色
            return false;
        }
        
        // 班主任不能分配系统管理员、学校管理员、学院负责人角色
        if (currentUserRoles.contains(Constants.ROLE_CLASS_TEACHER)) {
            if (Constants.ROLE_SCHOOL_ADMIN.equals(roleCode) || 
                Constants.ROLE_COLLEGE_LEADER.equals(roleCode)) {
                return false;
            }
            // 只能分配学生角色
            if (Constants.ROLE_STUDENT.equals(roleCode)) {
                return true;
            }
            return false;
        }
        
        // 企业管理员只能分配企业导师角色
        if (currentUserRoles.contains(Constants.ROLE_ENTERPRISE_ADMIN)) {
            if (Constants.ROLE_ENTERPRISE_MENTOR.equals(roleCode)) {
                return true;
            }
            return false;
        }
        
        // 其他角色默认不能分配角色
        return false;
    }
}

