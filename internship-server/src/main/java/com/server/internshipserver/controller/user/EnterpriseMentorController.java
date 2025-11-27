package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import com.server.internshipserver.domain.user.dto.EnterpriseMentorAddDTO;
import com.server.internshipserver.domain.user.dto.EnterpriseMentorUpdateDTO;
import com.server.internshipserver.service.user.EnterpriseMentorService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业导师管理控制器
 */
@Api(tags = "企业导师管理")
@RestController
@RequestMapping("/user/enterprise-mentor")
public class EnterpriseMentorController {
    
    @Autowired
    private EnterpriseMentorService enterpriseMentorService;
    
    @ApiOperation("分页查询企业导师列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<Page<EnterpriseMentor>> getEnterpriseMentorPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "导师姓名（可选）") @RequestParam(required = false) String mentorName,
            @ApiParam(value = "企业ID（可选）") @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "状态：1-启用，0-禁用（可选）") @RequestParam(required = false) Integer status) {
        Page<EnterpriseMentor> page = new Page<>(current, size);
        Page<EnterpriseMentor> result = enterpriseMentorService.getEnterpriseMentorPage(page, mentorName, enterpriseId, status);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询企业导师详情")
    @GetMapping("/{mentorId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> getEnterpriseMentorById(
            @ApiParam(value = "企业导师ID", required = true) @PathVariable Long mentorId) {
        EnterpriseMentor mentor = enterpriseMentorService.getEnterpriseMentorById(mentorId);
        return Result.success("查询成功", mentor);
    }
    
    @ApiOperation("根据用户ID查询企业导师信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> getEnterpriseMentorByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        EnterpriseMentor mentor = enterpriseMentorService.getEnterpriseMentorByUserId(userId);
        return Result.success("查询成功", mentor);
    }
    
    @ApiOperation("添加企业导师")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> addEnterpriseMentor(@RequestBody EnterpriseMentorAddDTO mentorAddDTO) {
        EnterpriseMentor result = enterpriseMentorService.addEnterpriseMentorWithUser(
                mentorAddDTO.getMentorName(),
                mentorAddDTO.getEnterpriseId(),
                mentorAddDTO.getPosition(),
                mentorAddDTO.getDepartment(),
                mentorAddDTO.getPhone(),
                mentorAddDTO.getEmail(),
                mentorAddDTO.getPassword(),
                mentorAddDTO.getStatus()
        );
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新企业导师信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> updateEnterpriseMentor(@RequestBody EnterpriseMentorUpdateDTO mentorUpdateDTO) {
        EnterpriseMentor result = enterpriseMentorService.updateEnterpriseMentorWithUser(
                mentorUpdateDTO.getMentorId(),
                mentorUpdateDTO.getUserId(),
                mentorUpdateDTO.getMentorName(),
                mentorUpdateDTO.getEnterpriseId(),
                mentorUpdateDTO.getPosition(),
                mentorUpdateDTO.getDepartment(),
                mentorUpdateDTO.getPhone(),
                mentorUpdateDTO.getEmail(),
                mentorUpdateDTO.getStatus()
        );
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用企业导师（软删除）")
    @DeleteMapping("/{mentorId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<?> deleteEnterpriseMentor(
            @ApiParam(value = "企业导师ID", required = true) @PathVariable Long mentorId) {
        boolean success = enterpriseMentorService.deleteEnterpriseMentor(mentorId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
}

