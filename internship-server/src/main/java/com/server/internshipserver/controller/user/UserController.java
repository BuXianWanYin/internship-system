package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.domain.user.dto.UserQueryDTO;
import com.server.internshipserver.service.user.UserService;

import java.util.List;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.server.internshipserver.common.utils.ExcelUtil;
import com.server.internshipserver.service.user.RoleService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private RoleService roleService;
    
    /**
     * 分页查询用户列表
     * 
     * @param queryDTO 查询条件DTO（包含分页参数和查询条件）
     * @return 用户分页列表
     */
    @ApiOperation("分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Page<UserInfo>> getUserPage(@ModelAttribute UserQueryDTO queryDTO) {
        // 设置默认分页值
        if (queryDTO.getCurrent() == null || queryDTO.getCurrent() < 1) {
            queryDTO.setCurrent(1L);
        }
        if (queryDTO.getSize() == null || queryDTO.getSize() < 1) {
            queryDTO.setSize(10L);
        }
        Page<UserInfo> page = new Page<>(queryDTO.getCurrent(), queryDTO.getSize());
        Page<UserInfo> result = userService.getUserPage(page, queryDTO);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询用户详情")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<UserInfo> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        UserInfo user = userService.getUserById(userId);
        return Result.success("查询成功", user);
    }
    
    @ApiOperation("添加用户")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<UserInfo> addUser(@RequestBody UserInfo user) {
        UserInfo result = userService.addUser(user);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新用户信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<UserInfo> updateUser(@RequestBody UserInfo user) {
        UserInfo result = userService.updateUser(user);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("删除用户（逻辑删除）")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<?> deleteUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        boolean success = userService.deleteUser(userId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    @ApiOperation("重置用户密码")
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<?> resetPassword(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "新密码", required = true) @RequestParam String newPassword) {
        boolean success = userService.resetPassword(userId, newPassword);
        return success ? Result.success("密码重置成功") : Result.error("密码重置失败");
    }
    
    @ApiOperation("根据用户名查询用户")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<UserInfo> getUserByUsername(
            @ApiParam(value = "用户名", required = true) @PathVariable String username) {
        UserInfo user = userService.getUserByUsername(username);
        return Result.success("查询成功", user);
    }
    
    @ApiOperation("为用户分配角色")
    @PostMapping("/{userId}/assign-role")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<?> assignRoleToUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "角色代码", required = true) @RequestParam String roleCode) {
        boolean success = userService.assignRoleToUser(userId, roleCode);
        return success ? Result.success("角色分配成功") : Result.error("角色分配失败");
    }
    
    @ApiOperation("检查是否可以停用用户")
    @GetMapping("/{userId}/can-delete")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Boolean> canDeleteUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        boolean canDelete = userService.canDeleteUser(userId);
        return Result.success("查询成功", canDelete);
    }
    
    @ApiOperation("获取用户角色列表")
    @GetMapping("/{userId}/roles")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<List<Role>> getUserRoles(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        List<Role> roles = userService.getUserRoles(userId);
        return Result.success("查询成功", roles);
    }
    
    @ApiOperation("获取当前登录用户信息")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current")
    public Result<UserInfo> getCurrentUser() {
        UserInfo user = userService.getCurrentUser();
        return Result.success("查询成功", user);
    }
    
    @ApiOperation("更新当前用户个人信息")
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/current/profile")
    public Result<UserInfo> updateCurrentUserProfile(@RequestBody UserInfo user) {
        UserInfo result = userService.updateCurrentUserProfile(user);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("修改当前用户密码")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/current/change-password")
    public Result<?> changePassword(
            @ApiParam(value = "旧密码", required = true) @RequestParam String oldPassword,
            @ApiParam(value = "新密码", required = true) @RequestParam String newPassword) {
        boolean success = userService.changePassword(oldPassword, newPassword);
        return success ? Result.success("密码修改成功") : Result.error("密码修改失败");
    }
    
    @ApiOperation("获取当前用户组织信息（学校、学院、班级）")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/current/org-info")
    public Result<java.util.Map<String, Object>> getCurrentUserOrgInfo() {
        java.util.Map<String, Object> orgInfo = userService.getCurrentUserOrgInfo();
        return Result.success("查询成功", orgInfo);
    }
    
    @ApiOperation("导出用户列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/export")
    public void exportUsers(
            @ApiParam(value = "用户名", required = false) @RequestParam(required = false) String username,
            @ApiParam(value = "真实姓名", required = false) @RequestParam(required = false) String realName,
            @ApiParam(value = "手机号", required = false) @RequestParam(required = false) String phone,
            @ApiParam(value = "状态：1-启用，0-禁用", required = false) @RequestParam(required = false) Integer status,
            @ApiParam(value = "角色代码（多个用逗号分隔）", required = false) @RequestParam(required = false) String roleCodes,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId,
            @ApiParam(value = "班级ID", required = false) @RequestParam(required = false) Long classId,
            HttpServletResponse response) throws IOException {
        UserQueryDTO queryDTO = new UserQueryDTO();
        queryDTO.setUsername(username);
        queryDTO.setRealName(realName);
        queryDTO.setPhone(phone);
        queryDTO.setStatus(status);
        queryDTO.setRoleCodes(roleCodes); // roleCodes是String类型，多个用逗号分隔
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setClassId(classId);
        
        List<UserInfo> users = userService.getAllUsers(queryDTO);
        
        // 获取所有角色信息，用于角色代码到角色名称的转换
        List<Role> allRoles = roleService.list();
        final java.util.Map<String, String> roleNameMap;
        if (allRoles != null && !allRoles.isEmpty()) {
            roleNameMap = allRoles.stream()
                    .filter(r -> r != null && r.getRoleCode() != null && r.getRoleName() != null)
                    .collect(Collectors.toMap(Role::getRoleCode, Role::getRoleName, (v1, v2) -> v1));
        } else {
            roleNameMap = new java.util.HashMap<>();
        }
        
        // 处理数据，转换状态和时间为文字
        for (UserInfo user : users) {
            if (user.getStatus() != null) {
                user.setStatusText(user.getStatus() == 1 ? "启用" : "禁用");
            } else {
                user.setStatusText("");
            }
            if (user.getCreateTime() != null) {
                user.setCreateTimeText(user.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                user.setCreateTimeText("");
            }
            // 转换角色代码为角色名称
            if (user.getRoles() != null && !user.getRoles().isEmpty()) {
                String roleNames = user.getRoles().stream()
                        .map(roleCode -> roleNameMap.getOrDefault(roleCode, roleCode))
                        .collect(Collectors.joining("、"));
                user.setRoleNames(roleNames);
            } else {
                user.setRoleNames("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"用户ID", "用户名", "真实姓名", "身份证号", "手机号", "邮箱", "角色", "状态", "创建时间"};
        String[] fieldNames = {"userId", "username", "realName", "idCard", "phone", "email", "roleNames", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, users, headers, fieldNames, "用户列表");
    }
}

