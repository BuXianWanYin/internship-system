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
import com.server.internshipserver.mapper.cooperation.EnterpriseSchoolCooperationMapper;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.common.enums.DeleteFlag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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
    private EnterpriseSchoolCooperationMapper cooperationMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    /**
     * 判断当前用户是否为系统管理员
     */
    public boolean isSystemAdmin() {
        UserDetails userDetails = SecurityUtil.getCurrentUserDetails();
        if (userDetails == null) {
            return false;
        }
        
        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        return authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_SYSTEM_ADMIN"));
    }
    
    /**
     * 获取当前用户的学校ID
     * @return 学校ID，如果无法获取则返回null
     */
    public Long getCurrentUserInfoSchoolId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return null;
        }
        
        // 查询用户信息
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
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
        if (roleCodes != null && roleCodes.contains("ROLE_SCHOOL_ADMIN")) {
            SchoolAdmin schoolAdmin = schoolAdminMapper.selectOne(
                    new LambdaQueryWrapper<SchoolAdmin>()
                            .eq(SchoolAdmin::getUserId, user.getUserId())
                            .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (schoolAdmin != null) {
                return schoolAdmin.getSchoolId();
            }
        }
        
        // 检查是否为学院负责人、班主任或指导教师
        if (roleCodes != null && (roleCodes.contains("ROLE_COLLEGE_LEADER") 
                || roleCodes.contains("ROLE_CLASS_TEACHER") 
                || roleCodes.contains("ROLE_INSTRUCTOR"))) {
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
        if (roleCodes != null && roleCodes.contains("ROLE_STUDENT")) {
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
        
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return null;
        }
        
        // 查询用户信息
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取学院ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 学院负责人、班主任、指导教师：从Teacher表获取
        if (roleCodes != null && (roleCodes.contains("ROLE_COLLEGE_LEADER") 
                || roleCodes.contains("ROLE_CLASS_TEACHER") 
                || roleCodes.contains("ROLE_INSTRUCTOR"))) {
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
        if (roleCodes != null && roleCodes.contains("ROLE_STUDENT")) {
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
        
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return null;
        }
        
        // 查询用户信息
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取班级ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 班主任：从Class表的class_teacher_id获取
        if (roleCodes != null && roleCodes.contains("ROLE_CLASS_TEACHER")) {
            Class classInfo = classMapper.selectOne(
                    new LambdaQueryWrapper<Class>()
                            .eq(Class::getClassTeacherId, user.getUserId())
                            .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (classInfo != null) {
                return classInfo.getClassId();
            }
        }
        
        // 学生：从Student表获取
        if (roleCodes != null && roleCodes.contains("ROLE_STUDENT")) {
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
     * 获取当前用户的班级ID（简化方法名）
     * @return 班级ID，如果无法获取则返回null
     */
    public Long getCurrentUserClassId() {
        return getCurrentUserInfoClassId();
    }
    
    /**
     * 获取当前用户的企业ID
     * @return 企业ID，如果无法获取则返回null
     */
    public Long getCurrentUserEnterpriseId() {
        if (isSystemAdmin()) {
            return null; // 系统管理员不限制
        }
        
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return null;
        }
        
        // 查询用户信息
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (user == null) {
            return null;
        }
        
        // 根据用户角色获取企业ID
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 企业管理员、企业导师：从Enterprise表获取
        if (roleCodes != null && (roleCodes.contains("ROLE_ENTERPRISE_ADMIN") 
                || roleCodes.contains("ROLE_ENTERPRISE_MENTOR"))) {
            Enterprise enterprise = enterpriseMapper.selectOne(
                    new LambdaQueryWrapper<Enterprise>()
                            .eq(Enterprise::getUserId, user.getUserId())
                            .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (enterprise != null) {
                return enterprise.getEnterpriseId();
            }
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
                   .eq(EnterpriseSchoolCooperation::getCooperationStatus, 1) // 只查询进行中的合作
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
                   .eq(EnterpriseSchoolCooperation::getCooperationStatus, 1) // 只查询进行中的合作
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
}

