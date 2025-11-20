package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.User;

/**
 * 用户管理Service接口
 */
public interface UserService extends IService<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    User getUserByUsername(String username);
    
    /**
     * 添加用户
     * @param user 用户信息
     * @return 添加的用户信息
     */
    User addUser(User user);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    User updateUser(User user);
    
    /**
     * 根据ID查询用户详情
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserById(Long userId);
    
    /**
     * 分页查询用户列表
     * @param page 分页参数
     * @param username 用户名（可选）
     * @param realName 真实姓名（可选）
     * @param phone 手机号（可选）
     * @return 用户列表
     */
    Page<User> getUserPage(Page<User> page, String username, String realName, String phone);
    
    /**
     * 停用用户（软删除）
     * @param userId 用户ID
     * @return 是否成功
     */
    boolean deleteUser(Long userId);
    
    /**
     * 重置用户密码
     * @param userId 用户ID
     * @param newPassword 新密码（明文）
     * @return 是否成功
     */
    boolean resetPassword(Long userId, String newPassword);
}

