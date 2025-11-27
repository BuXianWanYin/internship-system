package com.server.internshipserver.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.UserMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 用户工具类
 * 用于封装获取当前用户等重复逻辑
 */
public class UserUtil {
    
    private UserUtil() {
        // 工具类，禁止实例化
    }
    
    /**
     * 获取当前登录用户Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }
    
    /**
     * 获取当前登录用户名
     */
    public static String getCurrentUsername() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            return userDetails.getUsername();
        }
        return null;
    }
    
    /**
     * 获取当前登录用户详情
     */
    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return (UserDetails) authentication.getPrincipal();
        }
        return null;
    }
    
    /**
     * 判断用户是否已登录
     */
    public static boolean isAuthenticated() {
        Authentication authentication = getAuthentication();
        return authentication != null && authentication.isAuthenticated();
    }
    
    /**
     * 获取当前登录用户信息
     * @param userMapper 用户Mapper
     * @return 当前登录用户信息
     * @throws BusinessException 如果未登录或用户不存在
     */
    public static UserInfo getCurrentUser(UserMapper userMapper) {
        String username = getCurrentUsername();
        if (username == null) {
            throw new BusinessException("未登录");
        }
        
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        return user;
    }
    
    /**
     * 获取当前登录用户信息（允许返回null）
     * @param userMapper 用户Mapper
     * @return 当前登录用户信息，如果未登录或用户不存在则返回null
     */
    public static UserInfo getCurrentUserOrNull(UserMapper userMapper) {
        String username = getCurrentUsername();
        if (username == null) {
            return null;
        }
        
        return userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
    }
}

