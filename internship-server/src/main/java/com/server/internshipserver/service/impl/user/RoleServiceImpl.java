package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.mapper.user.RoleMapper;
import com.server.internshipserver.service.user.RoleService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 角色管理Service实现类
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements RoleService {
    
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
        
        // 通过SQL查询用户角色
        return baseMapper.selectList(
                new LambdaQueryWrapper<Role>()
                        .inSql(Role::getRoleId, 
                                "SELECT role_id FROM user_role WHERE user_id = " + userId + " AND delete_flag = 0")
                        .eq(Role::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
    }
}

