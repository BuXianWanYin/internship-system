package com.server.internshipserver.service.user;

import com.server.internshipserver.domain.user.UserInfo;

import java.util.Map;

/**
 * 认证授权Service接口
 * 提供用户登录、登出、Token刷新等认证授权功能
 */
public interface AuthService {
    
    /**
     * 用户登录
     * @param loginUser 登录用户信息（包含用户名和密码）
     * @return 登录结果（包含token和用户信息）
     */
    Map<String, Object> login(UserInfo loginUser);
    
    /**
     * 用户登出
     * @param token Token字符串
     * @return 是否成功
     */
    boolean logout(String token);
    
    /**
     * 刷新Token
     * @param token 旧Token
     * @return 新Token信息
     */
    Map<String, Object> refreshToken(String token);
}

