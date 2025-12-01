package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Role;

import java.util.List;

/**
 * 角色管理Service接口
 * 提供角色信息的增删改查等业务功能
 */
public interface RoleService extends IService<Role> {
    
    /**
     * 根据角色代码查询角色
     * @param roleCode 角色代码
     * @return 角色信息
     */
    Role getRoleByRoleCode(String roleCode);
    
    /**
     * 查询所有启用的角色
     * @return 角色列表
     */
    List<Role> getAllEnabledRoles();
    
    /**
     * 根据用户ID查询用户角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    List<Role> getRolesByUserId(Long userId);
}

