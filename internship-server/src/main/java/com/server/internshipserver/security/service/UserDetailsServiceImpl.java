package com.server.internshipserver.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.mapper.user.StudentMapper;
import org.springframework.security.core.userdetails.User;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户详情服务实现
 * 实现Spring Security的UserDetailsService接口，用于加载用户信息和权限
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private StudentMapper studentMapper;

    /**
     * 根据用户名加载用户详情
     * 包括用户基本信息、角色和权限信息
     * 
     * @param username 用户名
     * @return UserDetails对象
     * @throws UsernameNotFoundException 用户不存在或已被禁用时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        
        // 按照正确的顺序进行校验：
        // 1. 先校验是否有账号（用户是否存在）
        // 2. 再校验是否删除（如果已删除，提示"用户不存在"）
        // 3. 再校验是否启用
        // 4. 再校验审核状态（如果是教师/学生）
        // 注意：用户名和密码的校验由Spring Security的AuthenticationManager负责
        
        // 步骤1和2：检查用户是否存在，以及是否已删除
        // 注意：这里不过滤deleteFlag，以便判断用户是否存在以及是否已删除
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
        );
        
        // 步骤1：如果用户不存在，直接抛出异常
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 步骤2：检查是否已删除（如果已删除，提示"用户不存在"）
        if (user.getDeleteFlag() == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new UsernameNotFoundException("用户不存在");
        }
        
        // 步骤3：检查用户状态（必须是启用）
        if (user.getStatus() == null || user.getStatus().equals(UserStatus.DISABLED.getCode())) {
            throw new UsernameNotFoundException("账号已被禁用，无法登录");
        }
        
        // 步骤4：检查审核状态（如果是教师或学生）
        Teacher teacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getUserId, user.getUserId())
        );
        
        // 如果是教师，检查审核状态
        if (teacher != null) {
            if (teacher.getAuditStatus() == null || !teacher.getAuditStatus().equals(AuditStatus.APPROVED.getCode())) {
                if (teacher.getAuditStatus() != null && teacher.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
                    throw new UsernameNotFoundException("账号未审核通过，无法登录，请等待管理员审核");
                } else if (teacher.getAuditStatus() != null && teacher.getAuditStatus().equals(AuditStatus.REJECTED.getCode())) {
                    throw new UsernameNotFoundException("账号审核未通过，无法登录");
                } else {
                    throw new UsernameNotFoundException("账号审核状态异常，无法登录");
                }
            }
        } else {
            // 检查是否是学生
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
            );
            
            // 如果是学生，检查审核状态
            if (student != null) {
                if (student.getAuditStatus() == null || !student.getAuditStatus().equals(AuditStatus.APPROVED.getCode())) {
                    if (student.getAuditStatus() != null && student.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
                        throw new UsernameNotFoundException("账号未审核通过，无法登录，请等待管理员审核");
                    } else if (student.getAuditStatus() != null && student.getAuditStatus().equals(AuditStatus.REJECTED.getCode())) {
                        throw new UsernameNotFoundException("账号审核未通过，无法登录");
                    } else {
                        throw new UsernameNotFoundException("账号审核状态异常，无法登录");
                    }
                }
            }
        }
        
        // 查询用户角色
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 查询用户权限
        List<String> permissionCodes = userMapper.selectPermissionCodesByUserId(user.getUserId());
        
        // 构建权限集合
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        
        // 添加角色权限（以ROLE_开头）
        if (roleCodes != null && !roleCodes.isEmpty()) {
            authorities.addAll(roleCodes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }
        
        // 添加功能权限（以permission_code形式）
        if (permissionCodes != null && !permissionCodes.isEmpty()) {
            authorities.addAll(permissionCodes.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList()));
        }
        
        // 构建UserDetails对象
        return buildUserDetails(user, authorities);
    }
    
    /**
     * 构建UserDetails对象
     * 将用户实体和权限集合转换为Spring Security的UserDetails对象
     * 
     * @param user 用户实体
     * @param authorities 权限集合
     * @return UserDetails对象
     */
    private UserDetails buildUserDetails(UserInfo user, Collection<GrantedAuthority> authorities) {
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() != null && user.getStatus().equals(UserStatus.DISABLED.getCode()))
                .build();
    }
}

