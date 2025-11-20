package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.domain.user.Permission;
import com.server.internshipserver.mapper.user.PermissionMapper;
import com.server.internshipserver.service.user.PermissionService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 权限管理Service实现类
 */
@Service
public class PermissionServiceImpl extends ServiceImpl<PermissionMapper, Permission> implements PermissionService {
    
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
        
        // 通过SQL查询角色权限
        return baseMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .inSql(Permission::getPermissionId, 
                                "SELECT permission_id FROM role_permission WHERE role_id = " + roleId + " AND delete_flag = 0")
                        .eq(Permission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
    }
    
    @Override
    public List<Permission> getPermissionsByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 通过SQL查询用户权限（通过用户角色关联）
        return baseMapper.selectList(
                new LambdaQueryWrapper<Permission>()
                        .inSql(Permission::getPermissionId, 
                                "SELECT DISTINCT rp.permission_id FROM role_permission rp " +
                                "INNER JOIN user_role ur ON rp.role_id = ur.role_id " +
                                "WHERE ur.user_id = " + userId + " AND ur.delete_flag = 0 AND rp.delete_flag = 0")
                        .eq(Permission::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
    }
}

