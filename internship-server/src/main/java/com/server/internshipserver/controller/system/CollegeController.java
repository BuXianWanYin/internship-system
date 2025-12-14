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
 * 学院管理控制器
 */
@Api(tags = "学院管理")
@RestController
@RequestMapping("/system/college")
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
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/{id}")
    public Result<College> getCollegeById(
            @ApiParam(value = "学院ID", required = true) @PathVariable Long id) {
        College college = collegeService.getCollegeById(id);
        return Result.success(college);
    }
    
    @ApiOperation("分页查询学院列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
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
    
    @ApiOperation("导出学院列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/export")
    public void exportColleges(
            @ApiParam(value = "学院名称", required = false) @RequestParam(required = false) String collegeName,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            HttpServletResponse response) throws IOException {
        List<College> colleges = collegeService.getAllColleges(collegeName, schoolId);
        
        // 处理数据，转换状态和时间为文字
        for (College college : colleges) {
            if (college.getStatus() != null) {
                college.setStatusText(college.getStatus() == 1 ? "启用" : "禁用");
            } else {
                college.setStatusText("");
            }
            if (college.getCreateTime() != null) {
                college.setCreateTimeText(college.getCreateTime().format(
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                college.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"学院ID", "学院名称", "学院代码", "所属学校", "院长", "联系电话", "状态", "创建时间"};
        String[] fieldNames = {"collegeId", "collegeName", "collegeCode", "schoolName", "deanName", "contactPhone", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, colleges, headers, fieldNames, "学院列表");
    }
    
    @ApiOperation("公开获取学院列表（用于教师注册等公开场景）")
    @GetMapping("/public/list")
    public Result<List<College>> getPublicCollegeList() {
        List<College> colleges = collegeService.getPublicCollegeList();
        return Result.success(colleges);
    }
}

