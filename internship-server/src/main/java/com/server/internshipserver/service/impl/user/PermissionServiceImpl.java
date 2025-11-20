package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.domain.user.Permission;
import com.server.internshipserver.domain.user.RolePermission;
import com.server.internshipserver.domain.user.UserRole;
import com.server.internshipserver.mapper.user.PermissionMapper;
import com.server.internshipserver.mapper.user.RolePermissionMapper;
import com.server.internshipserver.mapper.user.UserRoleMapper;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.utils.RedisUtil;
import com.server.internshipserver.service.user.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 权限管理Service实现类
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    
    @Autowired
    private RolePermissionMapper rolePermissionMapper;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private RedisUtil redisUtil;
    
    /**
     * 权限缓存过期时间（小时）
     */
    private static final long PERMISSION_CACHE_EXPIRE_HOURS = 2;
    
    @Override
    public Permission getPermissionByPermissionCode(String permissionCode) {
        if (!StringUtils.hasText(permissionCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getPermissionCode, permissionCode)
               .eq(Permission::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public List<Permission> getAllEnabledPermissions() {
        LambdaQueryWrapper<Permission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Permission::getStatus, 1)
               .eq(Permission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByAsc(Permission::getPermissionId);
        return this.list(wrapper);
    }
    
    @Override
    public List<Permission> getPermissionsByRoleId(Long roleId) {
        if (roleId == null) {
            return null;
        }
        
        // 先查询角色权限关联表，获取permission_id列表，然后使用in方法
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .eq(RolePermission::getRoleId, roleId)
                        .eq(RolePermission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(RolePermission::getPermissionId)
        );
        if (rolePermissions != null && !rolePermissions.isEmpty()) {
            List<Long> permissionIds = rolePermissions.stream()
                    .map(RolePermission::getPermissionId)
                    .collect(Collectors.toList());
            return baseMapper.selectList(
                    new LambdaQueryWrapper<Permission>()
                            .in(Permission::getPermissionId, permissionIds)
                            .eq(Permission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
        }
        return java.util.Collections.emptyList();
    }
    
    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 先从Redis缓存中获取
        String cacheKey = Constants.REDIS_PERMISSION_PREFIX + "user:" + userId;
        @SuppressWarnings("unchecked")
        List<Permission> cachedPermissions = redisUtil.get(cacheKey, List.class);
        if (cachedPermissions != null) {
            return cachedPermissions;
        }
        
        // 缓存未命中，从数据库查询
        // 先查询用户角色关联表，获取role_id列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(UserRole::getRoleId)
        );
        if (userRoles == null || userRoles.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        List<Long> roleIds = userRoles.stream()
                .map(UserRole::getRoleId)
                .collect(Collectors.toList());
        
        // 再查询角色权限关联表，获取permission_id列表
        List<RolePermission> rolePermissions = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>()
                        .in(RolePermission::getRoleId, roleIds)
                        .eq(RolePermission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(RolePermission::getPermissionId)
        );
        if (rolePermissions == null || rolePermissions.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        
        // 去重permission_id
        List<Long> permissionIds = rolePermissions.stream()
                .map(RolePermission::getPermissionId)
                .distinct()
                .collect(Collectors.toList());
        
        // 最后查询权限列表
        List<Permission> permissions = baseMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .in(Permission::getPermissionId, permissionIds)
                        .eq(Permission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        // 存入Redis缓存
        if (permissions != null && !permissions.isEmpty()) {
            redisUtil.set(cacheKey, permissions, PERMISSION_CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
        }
        
        return permissions;
    }
    
    @Override
    public void clearUserPermissionCache(Long userId) {
        if (userId != null) {
            String cacheKey = Constants.REDIS_PERMISSION_PREFIX + "user:" + userId;
            redisUtil.delete(cacheKey);
        }
    }
    
    /**
     * 清除所有用户权限缓存（谨慎使用）
     */
    public void clearAllPermissionCache() {
        // 注意：这里只清除当前用户的缓存，如果需要清除所有缓存，需要遍历所有用户ID
        // 或者使用Redis的keys命令（不推荐，性能差）
        // 建议在权限变更时只清除相关用户的缓存
    }
}

