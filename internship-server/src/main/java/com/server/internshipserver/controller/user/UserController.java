package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("分页查询用户列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Page<UserInfo>> getUserPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "用户名（可选）") @RequestParam(required = false) String username,
            @ApiParam(value = "真实姓名（可选）") @RequestParam(required = false) String realName,
            @ApiParam(value = "手机号（可选）") @RequestParam(required = false) String phone,
            @ApiParam(value = "状态：1-启用，0-禁用（可选）") @RequestParam(required = false) Integer status,
            @ApiParam(value = "角色代码（可选，多个用逗号分隔，如：ROLE_STUDENT,ROLE_TEACHER）") @RequestParam(required = false) String roleCodes,
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "班级ID（可选）") @RequestParam(required = false) Long classId) {
        Page<UserInfo> page = new Page<>(current, size);
        Page<UserInfo> result = userService.getUserPage(page, username, realName, phone, status, roleCodes, schoolId, collegeId, classId);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询用户详情")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<UserInfo> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        UserInfo user = userService.getUserById(userId);
        return Result.success("查询成功", user);
    }
    
    @ApiOperation("添加用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<UserInfo> addUser(@RequestBody UserInfo user) {
        UserInfo result = userService.addUser(user);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新用户信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<UserInfo> updateUser(@RequestBody UserInfo user) {
        UserInfo result = userService.updateUser(user);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用用户（软删除）")
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<?> deleteUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        boolean success = userService.deleteUser(userId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
    
    @ApiOperation("重置用户密码")
    @PostMapping("/{userId}/reset-password")
    @PreAuthorize("hasAuthority('user:resetPassword')")
    public Result<?> resetPassword(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "新密码", required = true) @RequestParam String newPassword) {
        boolean success = userService.resetPassword(userId, newPassword);
        return success ? Result.success("密码重置成功") : Result.error("密码重置失败");
    }
    
    @ApiOperation("根据用户名查询用户")
    @GetMapping("/username/{username}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<UserInfo> getUserByUsername(
            @ApiParam(value = "用户名", required = true) @PathVariable String username) {
        UserInfo user = userService.getUserByUsername(username);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success("查询成功", user);
    }
    
    @ApiOperation("为用户分配角色")
    @PostMapping("/{userId}/assign-role")
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<?> assignRoleToUser(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId,
            @ApiParam(value = "角色代码", required = true) @RequestParam String roleCode) {
        boolean success = userService.assignRoleToUser(userId, roleCode);
        return success ? Result.success("角色分配成功") : Result.error("角色分配失败");
    }
}

