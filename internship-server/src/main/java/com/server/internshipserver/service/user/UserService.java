package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Role;

import java.util.List;

/**
 * 用户管理Service接口
 */
public interface UserService extends IService<UserInfo> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    UserInfo getUserByUsername(String username);
    
    /**
     * 添加用户
     * @param user 用户信息
     * @return 添加的用户信息
     */
    UserInfo addUser(UserInfo user);
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 更新后的用户信息
     */
    UserInfo updateUser(UserInfo user);
    
    /**
     * 根据ID查询用户详情
     * @param userId 用户ID
     * @return 用户信息
     */
    UserInfo getUserById(Long userId);
    
    /**
     * 分页查询用户列表
     * @param page 分页参数
     * @param username 用户名（可选）
     * @param realName 真实姓名（可选）
     * @param phone 手机号（可选）
     * @param status 状态：1-启用，0-禁用（可选）
     * @param roleCodes 角色代码（可选，多个用逗号分隔）
     * @param schoolId 学校ID（可选）
     * @param collegeId 学院ID（可选）
     * @param classId 班级ID（可选）
     * @return 用户列表
     */
    Page<UserInfo> getUserPage(Page<UserInfo> page, String username, String realName, String phone, 
                               Integer status, String roleCodes, Long schoolId, Long collegeId, Long classId);
    
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
    
    /**
     * 为用户分配角色
     * @param userId 用户ID
     * @param roleCode 角色代码（如：ROLE_STUDENT、ROLE_TEACHER等）
     * @return 是否成功
     */
    boolean assignRoleToUser(Long userId, String roleCode);
    
    /**
     * 检查是否可以停用用户（用于前端判断是否禁用停用按钮）
     * @param userId 用户ID
     * @return 是否可以停用
     */
    boolean canDeleteUser(Long userId);
    
    /**
     * 获取用户角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getUserRoles(Long userId);
}

