package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.service.system.CollegeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学院管理控制器
 */
@Api(tags = "学院管理")
@RestController
@RequestMapping("/api/system/college")
public class CollegeController {
    
    @Autowired
    private CollegeService collegeService;
    
    @ApiOperation("添加学院")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PostMapping
    public Result<College> addCollege(@RequestBody College college) {
        // 注意：学校管理员的schoolId需要在请求中指定，或通过数据权限过滤自动限制
        College result = collegeService.addCollege(college);
        return Result.success("添加学院成功", result);
    }
    
    @ApiOperation("更新学院信息")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PutMapping("/{id}")
    public Result<College> updateCollege(
            @ApiParam(value = "学院ID", required = true) @PathVariable Long id,
            @RequestBody College college) {
        college.setCollegeId(id);
        College result = collegeService.updateCollege(college);
        return Result.success("更新学院成功", result);
    }
    
    @ApiOperation("查询学院详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/{id}")
    public Result<College> getCollegeById(
            @ApiParam(value = "学院ID", required = true) @PathVariable Long id) {
        College college = collegeService.getCollegeById(id);
        return Result.success(college);
    }
    
    @ApiOperation("分页查询学院列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<College>> getCollegePage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学院名称", required = false) @RequestParam(required = false) String collegeName,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId) {
        Page<College> page = new Page<>(current, size);
        // 数据权限过滤已在Service层实现，根据当前登录用户角色自动过滤
        Page<College> result = collegeService.getCollegePage(page, collegeName, schoolId);
        return Result.success(result);
    }
    
    @ApiOperation("停用学院")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<?> deleteCollege(
            @ApiParam(value = "学院ID", required = true) @PathVariable Long id) {
        collegeService.deleteCollege(id);
        return Result.success("停用学院成功");
    }
}

