package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.service.system.SchoolService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学校管理控制器
 */
@Api(tags = "学校管理")
@RestController
@RequestMapping("/api/system/school")
public class SchoolController {
    
    @Autowired
    private SchoolService schoolService;
    
    @ApiOperation("添加学校")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping
    public Result<School> addSchool(@RequestBody School school) {
        School result = schoolService.addSchool(school);
        return Result.success("添加学校成功", result);
    }
    
    @ApiOperation("更新学校信息")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping("/{id}")
    public Result<School> updateSchool(
            @ApiParam(value = "学校ID", required = true) @PathVariable Long id,
            @RequestBody School school) {
        school.setSchoolId(id);
        School result = schoolService.updateSchool(school);
        return Result.success("更新学校成功", result);
    }
    
    @ApiOperation("查询学校详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/{id}")
    public Result<School> getSchoolById(
            @ApiParam(value = "学校ID", required = true) @PathVariable Long id) {
        School school = schoolService.getSchoolById(id);
        return Result.success(school);
    }
    
    @ApiOperation("分页查询学校列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/page")
    public Result<Page<School>> getSchoolPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学校名称", required = false) @RequestParam(required = false) String schoolName,
            @ApiParam(value = "学校代码", required = false) @RequestParam(required = false) String schoolCode) {
        Page<School> page = new Page<>(current, size);
        Page<School> result = schoolService.getSchoolPage(page, schoolName, schoolCode);
        return Result.success(result);
    }
    
    @ApiOperation("停用学校")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<?> deleteSchool(
            @ApiParam(value = "学校ID", required = true) @PathVariable Long id) {
        schoolService.deleteSchool(id);
        return Result.success("停用学校成功");
    }
}

