package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.domain.system.dto.SemesterQueryDTO;
import com.server.internshipserver.service.system.SemesterService;
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

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 学期管理控制器
 */
@Api(tags = "学期管理")
@RestController
@RequestMapping("/system/semester")
public class SemesterController {
    
    @Autowired
    private SemesterService semesterService;
    
    @Autowired
    private SchoolService schoolService;
    
    @ApiOperation("添加学期")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PostMapping
    public Result<Semester> addSemester(@RequestBody Semester semester) {
        Semester result = semesterService.addSemester(semester);
        return Result.success("添加学期成功", result);
    }
    
    @ApiOperation("更新学期信息")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
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
            @ApiParam(value = "学期名称", required = false) @RequestParam(required = false) String semesterName,
            @ApiParam(value = "年份", required = false) @RequestParam(required = false) Integer year,
            @ApiParam(value = "是否当前学期：1-是，0-否", required = false) @RequestParam(required = false) Integer isCurrent,
            @ApiParam(value = "开始日期（格式：yyyy-MM-dd）", required = false) @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期（格式：yyyy-MM-dd）", required = false) @RequestParam(required = false) String endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId) {
        Page<Semester> page = new Page<>(current, size);
        SemesterQueryDTO queryDTO = new SemesterQueryDTO();
        queryDTO.setSemesterName(semesterName);
        queryDTO.setYear(year);
        queryDTO.setIsCurrent(isCurrent);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        Page<Semester> result = semesterService.getSemesterPage(page, queryDTO);
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
    
    @ApiOperation("导出学期列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/export")
    public void exportSemesters(
            @ApiParam(value = "学期名称", required = false) @RequestParam(required = false) String semesterName,
            @ApiParam(value = "年份", required = false) @RequestParam(required = false) Integer year,
            @ApiParam(value = "是否当前学期：1-是，0-否", required = false) @RequestParam(required = false) Integer isCurrent,
            @ApiParam(value = "开始日期（格式：yyyy-MM-dd）", required = false) @RequestParam(required = false) String startDate,
            @ApiParam(value = "结束日期（格式：yyyy-MM-dd）", required = false) @RequestParam(required = false) String endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            HttpServletResponse response) throws IOException {
        SemesterQueryDTO queryDTO = new SemesterQueryDTO();
        queryDTO.setSemesterName(semesterName);
        queryDTO.setYear(year);
        queryDTO.setIsCurrent(isCurrent);
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        
        List<Semester> semesters = semesterService.getAllSemesters(queryDTO);
        
        // 填充学校名称
        if (semesters != null && !semesters.isEmpty()) {
            List<Long> schoolIds = semesters.stream()
                    .map(Semester::getSchoolId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            for (Long sid : schoolIds) {
                try {
                    com.server.internshipserver.domain.system.School school = schoolService.getSchoolById(sid);
                    if (school != null) {
                        String schoolName = school.getSchoolName();
                        for (Semester semester : semesters) {
                            if (Objects.equals(semester.getSchoolId(), sid)) {
                                semester.setSchoolName(schoolName);
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略错误
                }
            }
        }
        
        // 处理数据，转换字段为文字
        for (Semester semester : semesters) {
            if (semester.getIsCurrent() != null) {
                semester.setIsCurrentText(semester.getIsCurrent() == 1 ? "是" : "否");
            } else {
                semester.setIsCurrentText("");
            }
            if (semester.getStartDate() != null) {
                semester.setStartDateText(semester.getStartDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                semester.setStartDateText("");
            }
            if (semester.getEndDate() != null) {
                semester.setEndDateText(semester.getEndDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                semester.setEndDateText("");
            }
            if (semester.getCreateTime() != null) {
                semester.setCreateTimeText(semester.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                semester.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"学期ID", "学期名称", "所属学校", "开始日期", "结束日期", "是否当前学期", "创建时间"};
        String[] fieldNames = {"semesterId", "semesterName", "schoolName", "startDateText", "endDateText", "isCurrentText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, semesters, headers, fieldNames, "学期列表");
    }
}

