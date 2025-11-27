package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.service.system.MajorService;
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
 * 专业管理控制器
 */
@Api(tags = "专业管理")
@RestController
@RequestMapping("/system/major")
public class MajorController {
    
    @Autowired
    private MajorService majorService;
    
    @ApiOperation("添加专业")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping
    public Result<Major> addMajor(@RequestBody Major major) {
        // 注意：学院负责人的collegeId需要在请求中指定，或通过数据权限过滤自动限制
        Major result = majorService.addMajor(major);
        return Result.success("添加专业成功", result);
    }
    
    @ApiOperation("更新专业信息")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PutMapping("/{id}")
    public Result<Major> updateMajor(
            @ApiParam(value = "专业ID", required = true) @PathVariable Long id,
            @RequestBody Major major) {
        major.setMajorId(id);
        Major result = majorService.updateMajor(major);
        return Result.success("更新专业成功", result);
    }
    
    @ApiOperation("查询专业详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/{id}")
    public Result<Major> getMajorById(
            @ApiParam(value = "专业ID", required = true) @PathVariable Long id) {
        Major major = majorService.getMajorById(id);
        return Result.success(major);
    }
    
    @ApiOperation("分页查询专业列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/page")
    public Result<Page<Major>> getMajorPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "专业名称", required = false) @RequestParam(required = false) String majorName,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId) {
        Page<Major> page = new Page<>(current, size);
        // 数据权限过滤已在Service层实现，根据当前登录用户角色自动过滤
        Page<Major> result = majorService.getMajorPage(page, majorName, collegeId, schoolId);
        return Result.success(result);
    }
    
    @ApiOperation("停用专业")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @DeleteMapping("/{id}")
    public Result<?> deleteMajor(
            @ApiParam(value = "专业ID", required = true) @PathVariable Long id) {
        majorService.deleteMajor(id);
        return Result.success("停用专业成功");
    }
}

