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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.server.internshipserver.common.utils.ExcelUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.time.format.DateTimeFormatter;

/**
 * 学校管理控制器
 */
@Api(tags = "学校管理")
@RestController
@RequestMapping("/system/school")
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
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN')")
    @GetMapping("/{id}")
    public Result<School> getSchoolById(
            @ApiParam(value = "学校ID", required = true) @PathVariable Long id) {
        School school = schoolService.getSchoolById(id);
        return Result.success(school);
    }
    
    @ApiOperation("分页查询学校列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
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
    
    @ApiOperation("公开获取学校列表（用于企业注册等公开场景）")
    @GetMapping("/public/list")
    public Result<List<School>> getPublicSchoolList() {
        List<School> schools = schoolService.getPublicSchoolList();
        return Result.success(schools);
    }
    
    @ApiOperation("导出学校列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/export")
    public void exportSchools(
            @ApiParam(value = "学校名称", required = false) @RequestParam(required = false) String schoolName,
            @ApiParam(value = "学校代码", required = false) @RequestParam(required = false) String schoolCode,
            HttpServletResponse response) throws IOException {
        List<School> schools = schoolService.getAllSchools(schoolName, schoolCode);
        
        // 处理数据，转换状态和时间为文字
        for (School school : schools) {
            // 转换状态
            if (school.getStatus() != null) {
                school.setStatusText(school.getStatus() == 1 ? "启用" : "禁用");
            } else {
                school.setStatusText("");
            }
            // 转换创建时间
            if (school.getCreateTime() != null) {
                school.setCreateTimeText(school.getCreateTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                school.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"学校ID", "学校名称", "学校代码", "地址", "负责人", "负责人电话", "状态", "创建时间"};
        String[] fieldNames = {"schoolId", "schoolName", "schoolCode", "address", "managerName", "managerPhone", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, schools, headers, fieldNames, "学校列表");
    }
}

