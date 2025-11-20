package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.service.system.SemesterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 学期管理控制器
 */
@Api(tags = "学期管理")
@RestController
@RequestMapping("/system/semester")
public class SemesterController {
    
    @Autowired
    private SemesterService semesterService;
    
    @ApiOperation("添加学期")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PostMapping
    public Result<Semester> addSemester(@RequestBody Semester semester) {
        Semester result = semesterService.addSemester(semester);
        return Result.success("添加学期成功", result);
    }
    
    @ApiOperation("更新学期信息")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping("/{id}")
    public Result<Semester> updateSemester(
            @ApiParam(value = "学期ID", required = true) @PathVariable Long id,
            @RequestBody Semester semester) {
        semester.setSemesterId(id);
        Semester result = semesterService.updateSemester(semester);
        return Result.success("更新学期成功", result);
    }
    
    @ApiOperation("查询学期详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/{id}")
    public Result<Semester> getSemesterById(
            @ApiParam(value = "学期ID", required = true) @PathVariable Long id) {
        Semester semester = semesterService.getSemesterById(id);
        return Result.success(semester);
    }
    
    @ApiOperation("分页查询学期列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/page")
    public Result<Page<Semester>> getSemesterPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学期名称", required = false) @RequestParam(required = false) String semesterName) {
        Page<Semester> page = new Page<>(current, size);
        Page<Semester> result = semesterService.getSemesterPage(page, semesterName);
        return Result.success(result);
    }
    
    @ApiOperation("设置当前学期")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @PutMapping("/{id}/current")
    public Result<?> setCurrentSemester(
            @ApiParam(value = "学期ID", required = true) @PathVariable Long id) {
        semesterService.setCurrentSemester(id);
        return Result.success("设置当前学期成功");
    }
    
    @ApiOperation("获取当前学期")
    @GetMapping("/current")
    public Result<Semester> getCurrentSemester() {
        Semester semester = semesterService.getCurrentSemester();
        return Result.success(semester);
    }
    
    @ApiOperation("删除学期")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<?> deleteSemester(
            @ApiParam(value = "学期ID", required = true) @PathVariable Long id) {
        semesterService.deleteSemester(id);
        return Result.success("删除学期成功");
    }
}

