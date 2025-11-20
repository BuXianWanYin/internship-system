package com.server.internshipserver.security.service;

import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.domain.user.User;
import com.server.internshipserver.mapper.user.UserMapper;
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
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {
    
    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        
        // 1. 从数据库查询用户信息
        User user = userMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在：" + username);
        }
        
        // 2. 检查用户状态
        if (user.getStatus() == null || user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用：" + username);
        }
        
        // 3. 查询用户角色
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 4. 查询用户权限
        List<String> permissionCodes = userMapper.selectPermissionCodesByUserId(user.getUserId());
        
        // 5. 构建权限集合
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
        
        // 6. 构建UserDetails对象
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(user.getStatus() == 0)
                .build();
    }
}

