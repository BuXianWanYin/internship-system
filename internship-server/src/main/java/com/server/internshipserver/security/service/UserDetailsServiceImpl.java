package com.server.internshipserver.security.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;

/**
 * 用户详情服务实现
 * 临时实现，后续会连接数据库查询用户信息
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // TODO: 后续连接数据库查询用户信息
        // 临时实现，返回一个测试用户
        // 实际应该从数据库查询用户信息、角色、权限等
        
        if (username == null || username.isEmpty()) {
            throw new UsernameNotFoundException("用户名不能为空");
        }
        
        // 临时实现：返回一个测试用户
        // 后续需要：
        // 1. 从数据库查询用户信息
        // 2. 查询用户角色
        // 3. 查询用户权限
        // 4. 构建UserDetails对象
        
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        
        return User.builder()
                .username(username)
                .password("$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwy8pLZO") // 临时密码：123456
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}

