package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.User;
import com.server.internshipserver.service.user.EnterpriseService;
import com.server.internshipserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 企业管理控制器
 */
@Api(tags = "企业管理")
@RestController
@RequestMapping("/user/enterprise")
public class EnterpriseController {
    
    @Autowired
    private EnterpriseService enterpriseService;
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("企业注册")
    @PostMapping("/register")
    public Result<Enterprise> registerEnterprise(@RequestBody Enterprise enterprise) {
        Enterprise result = enterpriseService.registerEnterprise(enterprise);
        return Result.success("注册成功，等待审核", result);
    }
    
    @ApiOperation("分页查询企业列表")
    @GetMapping("/page")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Page<Enterprise>> getEnterprisePage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "企业名称（可选）") @RequestParam(required = false) String enterpriseName,
            @ApiParam(value = "企业代码（可选）") @RequestParam(required = false) String enterpriseCode,
            @ApiParam(value = "审核状态（可选）0-待审核，1-已通过，2-已拒绝") @RequestParam(required = false) Integer auditStatus) {
        Page<Enterprise> page = new Page<>(current, size);
        Page<Enterprise> result = enterpriseService.getEnterprisePage(page, enterpriseName, enterpriseCode, auditStatus);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询企业详情")
    @GetMapping("/{enterpriseId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Enterprise> getEnterpriseById(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId);
        return Result.success("查询成功", enterprise);
    }
    
    @ApiOperation("根据用户ID查询企业信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('user:view')")
    public Result<Enterprise> getEnterpriseByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Enterprise enterprise = enterpriseService.getEnterpriseByUserId(userId);
        if (enterprise == null) {
            return Result.error("企业信息不存在");
        }
        return Result.success("查询成功", enterprise);
    }
    
    @ApiOperation("添加企业")
    @PostMapping
    @PreAuthorize("hasAuthority('user:add')")
    public Result<Enterprise> addEnterprise(@RequestBody Enterprise enterprise) {
        Enterprise result = enterpriseService.addEnterprise(enterprise);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新企业信息")
    @PutMapping
    @PreAuthorize("hasAuthority('user:edit')")
    public Result<Enterprise> updateEnterprise(@RequestBody Enterprise enterprise) {
        Enterprise result = enterpriseService.updateEnterprise(enterprise);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("审核企业")
    @PostMapping("/{enterpriseId}/audit")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<?> auditEnterprise(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId,
            @ApiParam(value = "审核状态：1-通过，2-拒绝", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见") @RequestParam(required = false) String auditOpinion) {
        // 获取当前登录用户ID
        String username = SecurityUtil.getCurrentUsername();
        Long auditorId = null;
        if (username != null) {
            User user = userService.getUserByUsername(username);
            if (user != null) {
                auditorId = user.getUserId();
            }
        }
        if (auditorId == null) {
            return Result.error("无法获取当前登录用户信息");
        }
        
        boolean success = enterpriseService.auditEnterprise(enterpriseId, auditStatus, auditOpinion, auditorId);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }
    
    @ApiOperation("停用企业（软删除）")
    @DeleteMapping("/{enterpriseId}")
    @PreAuthorize("hasAuthority('user:delete')")
    public Result<?> deleteEnterprise(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        boolean success = enterpriseService.deleteEnterprise(enterpriseId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
}

