package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Permission;

import java.util.List;

/**
 * 权限管理Service接口
 * 提供权限信息的增删改查等业务功能
 */
public interface PermissionService extends IService<Permission> {
    
    /**
     * 根据权限代码查询权限
     * @param permissionCode 权限代码
     * @return 权限信息
     */
    Permission getPermissionByPermissionCode(String permissionCode);
    
    /**
     * 查询所有启用的权限
     * @return 权限列表
     */
    List<Permission> getAllEnabledPermissions();
    
    /**
     * 根据角色ID查询角色权限列表
     * @param roleId 角色ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByRoleId(Long roleId);
    
    /**
     * 根据用户ID查询用户权限列表
     * @param userId 用户ID
     * @return 权限列表
     */
    List<Permission> getPermissionsByUserId(Long userId);
    
    /**
     * 清除用户权限缓存
     * @param userId 用户ID
     */
    void clearUserPermissionCache(Long userId);
}

