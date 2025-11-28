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
import com.server.internshipserver.common.utils.ExcelUtil;
import com.server.internshipserver.service.system.SchoolService;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.mapper.system.CollegeMapper;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 专业管理控制器
 */
@Api(tags = "专业管理")
@RestController
@RequestMapping("/system/major")
public class MajorController {
    
    @Autowired
    private MajorService majorService;
    
    @Autowired
    private SchoolService schoolService;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
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
    
    @ApiOperation("导出专业列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/export")
    public void exportMajors(
            @ApiParam(value = "专业名称", required = false) @RequestParam(required = false) String majorName,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            HttpServletResponse response) throws IOException {
        List<Major> majors = majorService.getAllMajors(majorName, collegeId, schoolId);
        
        // 填充学校名称
        if (majors != null && !majors.isEmpty()) {
            List<Long> collegeIds = majors.stream()
                    .map(Major::getCollegeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!collegeIds.isEmpty()) {
                List<College> colleges = collegeMapper.selectBatchIds(collegeIds);
                if (colleges != null && !colleges.isEmpty()) {
                    java.util.Map<Long, Long> collegeSchoolMap = colleges.stream()
                            .filter(c -> c != null && c.getCollegeId() != null && c.getSchoolId() != null)
                            .collect(Collectors.toMap(College::getCollegeId, College::getSchoolId, (v1, v2) -> v1));
                    
                    List<Long> schoolIds = colleges.stream()
                            .map(College::getSchoolId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    
                    java.util.Map<Long, String> schoolNameMap = new java.util.HashMap<>();
                    for (Long sid : schoolIds) {
                        try {
                            com.server.internshipserver.domain.system.School school = schoolService.getSchoolById(sid);
                            if (school != null) {
                                schoolNameMap.put(sid, school.getSchoolName());
                            }
                        } catch (Exception e) {
                            // 忽略错误
                        }
                    }
                    
                    for (Major major : majors) {
                        if (major.getCollegeId() != null) {
                            Long sid = collegeSchoolMap.get(major.getCollegeId());
                            if (sid != null) {
                                major.setSchoolName(schoolNameMap.get(sid));
                            }
                        }
                    }
                }
            }
        }
        
        // 处理数据，转换状态和时间为文字
        for (Major major : majors) {
            if (major.getStatus() != null) {
                major.setStatusText(major.getStatus() == 1 ? "启用" : "禁用");
            } else {
                major.setStatusText("");
            }
            if (major.getCreateTime() != null) {
                major.setCreateTimeText(major.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                major.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"专业ID", "专业名称", "专业代码", "所属学院", "所属学校", "学制年限", "状态", "创建时间"};
        String[] fieldNames = {"majorId", "majorName", "majorCode", "collegeName", "schoolName", "duration", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, majors, headers, fieldNames, "专业列表");
    }
}

