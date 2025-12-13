package com.server.internshipserver.security.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Teacher;
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
        
        // 从数据库查询用户信息
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        
        // 检查用户状态
        if (user.getStatus() == null || user.getStatus().equals(UserStatus.DISABLED.getCode())) {
            // 如果是教师，检查审核状态，给出更明确的提示
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, user.getUserId())
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            
            if (teacher != null) {
                // 检查审核状态
                if (teacher.getAuditStatus() != null) {
                    if (teacher.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
                        throw new UsernameNotFoundException("账号未审核通过，无法登录，请等待管理员审核");
                    } else if (teacher.getAuditStatus().equals(AuditStatus.REJECTED.getCode())) {
                        throw new UsernameNotFoundException("账号审核未通过，无法登录");
                    }
                }
            }
            
            // 如果不是教师或审核状态不是待审核/已拒绝，则提示用户已被禁用
            throw new UsernameNotFoundException("用户已被禁用：" + username);
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

