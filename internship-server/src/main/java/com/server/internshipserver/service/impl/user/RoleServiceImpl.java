package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.domain.user.UserRole;
import com.server.internshipserver.mapper.user.RoleMapper;
import com.server.internshipserver.mapper.user.UserRoleMapper;
import com.server.internshipserver.service.user.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色管理Service实现类
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Override
    public Role getRoleByRoleCode(String roleCode) {
        if (!StringUtils.hasText(roleCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getRoleCode, roleCode)
               .eq(Role::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public List<Role> getAllEnabledRoles() {
        LambdaQueryWrapper<Role> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Role::getStatus, 1)
               .eq(Role::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByAsc(Role::getRoleId);
        return this.list(wrapper);
    }
    
    @Override
    public List<Role> getRolesByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        // 先查询用户角色关联表，获取role_id列表，然后使用in方法
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .eq(UserRole::getUserId, userId)
                        .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(UserRole::getRoleId)
        );
        if (userRoles != null && !userRoles.isEmpty()) {
            List<Long> roleIds = userRoles.stream()
                    .map(UserRole::getRoleId)
                    .collect(Collectors.toList());
            return baseMapper.selectList(
                    new LambdaQueryWrapper<Role>()
                            .in(Role::getRoleId, roleIds)
                            .eq(Role::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
        }
        return java.util.Collections.emptyList();
    }
}

