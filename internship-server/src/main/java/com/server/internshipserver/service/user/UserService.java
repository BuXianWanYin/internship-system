package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.domain.user.dto.UserQueryDTO;

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
     * @param queryDTO 查询条件
     * @return 用户列表
     */
    Page<UserInfo> getUserPage(Page<UserInfo> page, UserQueryDTO queryDTO);
    
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
    
    /**
     * 获取当前登录用户信息
     * @return 用户信息
     */
    UserInfo getCurrentUser();
    
    /**
     * 更新当前用户个人信息
     * @param user 用户信息（只更新允许的字段：realName, phone, email, avatar）
     * @return 更新后的用户信息
     */
    UserInfo updateCurrentUserProfile(UserInfo user);
    
    /**
     * 修改当前用户密码
     * @param oldPassword 旧密码（明文）
     * @param newPassword 新密码（明文）
     * @return 是否成功
     */
    boolean changePassword(String oldPassword, String newPassword);
    
    /**
     * 获取当前用户组织信息（学校、学院、班级）
     * @return 组织信息Map，包含schoolId、schoolName、collegeId、collegeName、classIds、classNames
     */
    java.util.Map<String, Object> getCurrentUserOrgInfo();
    
    /**
     * 查询所有用户列表（用于导出）
     * @param queryDTO 查询条件
     * @return 用户列表
     */
    List<UserInfo> getAllUsers(UserQueryDTO queryDTO);
}

