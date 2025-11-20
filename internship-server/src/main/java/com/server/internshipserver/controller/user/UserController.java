package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.User;
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
    public Result<Page<User>> getUserPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "用户名（可选）") @RequestParam(required = false) String username,
            @ApiParam(value = "真实姓名（可选）") @RequestParam(required = false) String realName,
            @ApiParam(value = "手机号（可选）") @RequestParam(required = false) String phone) {
        Page<User> page = new Page<>(current, size);
        Page<User> result = userService.getUserPage(page, username, realName, phone);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询用户详情")
    @GetMapping("/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<User> getUserById(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        User user = userService.getUserById(userId);
        return Result.success("查询成功", user);
    }
    
    @ApiOperation("添加用户")
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<User> addUser(@RequestBody User user) {
        User result = userService.addUser(user);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新用户信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<User> updateUser(@RequestBody User user) {
        User result = userService.updateUser(user);
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
}

