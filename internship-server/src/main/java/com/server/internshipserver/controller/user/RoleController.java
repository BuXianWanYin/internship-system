package com.server.internshipserver.controller.user;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.service.user.RoleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 角色管理控制器
 */
@Api(tags = "角色管理")
@RestController
@RequestMapping("/user/role")
public class RoleController {
    
    @Autowired
    private RoleService roleService;
    
    @ApiOperation("查询所有启用的角色列表")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<List<Role>> getAllEnabledRoles() {
        List<Role> roles = roleService.getAllEnabledRoles();
        return Result.success("查询成功", roles);
    }
}

