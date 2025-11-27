package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.dto.EnterpriseRegisterDTO;
import com.server.internshipserver.domain.user.dto.EnterpriseAddDTO;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.service.user.EnterpriseService;
import java.util.List;
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
    
    @ApiOperation("企业注册")
    @PostMapping("/register")
    public Result<Enterprise> registerEnterprise(@RequestBody EnterpriseRegisterDTO registerDTO) {
        Enterprise result = enterpriseService.registerEnterprise(registerDTO.getEnterprise(), registerDTO.getSchoolIds());
        return Result.success("注册成功，等待院校审核", result);
    }
    
    @ApiOperation("分页查询企业列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<Enterprise> getEnterpriseById(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        Enterprise enterprise = enterpriseService.getEnterpriseById(enterpriseId);
        return Result.success("查询成功", enterprise);
    }
    
    @ApiOperation("根据用户ID查询企业信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<Enterprise> getEnterpriseByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Enterprise enterprise = enterpriseService.getEnterpriseByUserId(userId);
        return Result.success("查询成功", enterprise);
    }
    
    @ApiOperation("添加企业（系统管理员）")
    @PostMapping
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    public Result<Enterprise> addEnterprise(@RequestBody EnterpriseAddDTO addDTO) {
        Enterprise result = enterpriseService.addEnterpriseWithAdmin(
            addDTO.getEnterprise(),
            addDTO.getAdminName(),
            addDTO.getAdminPhone(),
            addDTO.getAdminEmail(),
            addDTO.getAdminPassword()
        );
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新企业信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
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
        boolean success = enterpriseService.auditEnterprise(enterpriseId, auditStatus, auditOpinion);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }
    
    @ApiOperation("停用企业（软删除）")
    @DeleteMapping("/{enterpriseId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<?> deleteEnterprise(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        boolean success = enterpriseService.deleteEnterprise(enterpriseId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
    
    @ApiOperation("根据企业ID查询合作学校列表")
    @GetMapping("/{enterpriseId}/cooperation-schools")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<List<School>> getCooperationSchoolsByEnterpriseId(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        List<School> schools = enterpriseService.getCooperationSchoolsByEnterpriseId(enterpriseId);
        return Result.success("查询成功", schools);
    }
}

