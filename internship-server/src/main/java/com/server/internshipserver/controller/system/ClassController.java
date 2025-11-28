package com.server.internshipserver.controller.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.service.system.ClassService;
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
import com.server.internshipserver.service.system.CollegeService;
import com.server.internshipserver.service.system.MajorService;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.domain.user.UserInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 班级管理控制器
 */
@Api(tags = "班级管理")
@RestController
@RequestMapping("/system/class")
public class ClassController {
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private SchoolService schoolService;
    
    @Autowired
    private CollegeService collegeService;
    
    @Autowired
    private MajorService majorService;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @ApiOperation("添加班级")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PostMapping
    public Result<Class> addClass(@RequestBody Class classInfo) {
        // 注意：学院负责人或班主任的majorId需要在请求中指定，或通过数据权限过滤自动限制
        Class result = classService.addClass(classInfo);
        return Result.success("添加班级成功", result);
    }
    
    @ApiOperation("更新班级信息")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PutMapping("/{id}")
    public Result<Class> updateClass(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id,
            @RequestBody Class classInfo) {
        classInfo.setClassId(id);
        Class result = classService.updateClass(classInfo);
        return Result.success("更新班级成功", result);
    }
    
    @ApiOperation("查询班级详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/{id}")
    public Result<Class> getClassById(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        Class classInfo = classService.getClassById(id);
        return Result.success(classInfo);
    }
    
    @ApiOperation("分页查询班级列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/page")
    public Result<Page<Class>> getClassPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "班级名称", required = false) @RequestParam(required = false) String className,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId) {
        Page<Class> page = new Page<>(current, size);
        // 数据权限过滤已在Service层实现，根据当前登录用户角色自动过滤
        Page<Class> result = classService.getClassPage(page, className, majorId, collegeId, schoolId);
        return Result.success(result);
    }
    
    @ApiOperation("停用班级")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PutMapping("/{id}/disable")
    public Result<?> disableClass(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        classService.disableClass(id);
        return Result.success("停用班级成功");
    }
    
    @ApiOperation("启用班级")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PutMapping("/{id}/enable")
    public Result<?> enableClass(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        classService.enableClass(id);
        return Result.success("启用班级成功");
    }
    
    @ApiOperation("删除班级")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @DeleteMapping("/{id}")
    public Result<?> deleteClass(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        classService.deleteClass(id);
        return Result.success("删除班级成功");
    }
    
    @ApiOperation("生成班级分享码")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PostMapping("/{id}/share-code")
    public Result<Map<String, Object>> generateShareCode(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        classService.generateShareCode(id);
        Map<String, Object> data = classService.getShareCodeInfo(id);
        return Result.success("生成分享码成功", data);
    }
    
    @ApiOperation("重新生成班级分享码")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PostMapping("/{id}/share-code/regenerate")
    public Result<Map<String, Object>> regenerateShareCode(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        classService.regenerateShareCode(id);
        Map<String, Object> data = classService.getShareCodeInfo(id);
        return Result.success("重新生成分享码成功", data);
    }
    
    @ApiOperation("查看班级分享码")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/{id}/share-code")
    public Result<Map<String, Object>> getShareCode(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long id) {
        Map<String, Object> data = classService.getShareCodeInfo(id);
        return Result.success(data);
    }
    
    @ApiOperation("验证分享码")
    @PostMapping("/share-code/validate")
    public Result<Class> validateShareCode(
            @ApiParam(value = "分享码", required = true) @RequestParam String shareCode) {
        Class classInfo = classService.validateShareCode(shareCode);
        return Result.success("分享码验证成功", classInfo);
    }
    
    @ApiOperation("任命班主任")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/{classId}/appoint-teacher")
    public Result<?> appointClassTeacher(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long classId,
            @ApiParam(value = "教师ID", required = true) @RequestParam(required = true) Long teacherId) {
        if (teacherId == null) {
            return Result.error("教师ID不能为空");
        }
            boolean success = classService.appointClassTeacher(classId, teacherId);
            return success ? Result.success("任命班主任成功") : Result.error("任命班主任失败");
    }
    
    @ApiOperation("取消班主任任命")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/{classId}/remove-teacher")
    public Result<?> removeClassTeacher(
            @ApiParam(value = "班级ID", required = true) @PathVariable Long classId) {
        boolean success = classService.removeClassTeacher(classId);
        return success ? Result.success("取消班主任任命成功") : Result.error("取消班主任任命失败");
    }
    
    @ApiOperation("导出班级列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/export")
    public void exportClasses(
            @ApiParam(value = "班级名称", required = false) @RequestParam(required = false) String className,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            HttpServletResponse response) throws IOException {
        List<Class> classes = classService.getAllClasses(className, majorId, collegeId, schoolId);
        
        // 填充关联信息
        if (classes != null && !classes.isEmpty()) {
            // 获取所有专业ID
            List<Long> majorIds = classes.stream()
                    .map(Class::getMajorId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!majorIds.isEmpty()) {
                // 查询专业信息
                List<Major> majors = majorMapper.selectBatchIds(majorIds);
                if (majors != null && !majors.isEmpty()) {
                    java.util.Map<Long, Major> majorMap = majors.stream()
                            .filter(m -> m != null && m.getMajorId() != null)
                            .collect(Collectors.toMap(Major::getMajorId, m -> m, (v1, v2) -> v1));
                    
                    // 获取所有学院ID
                    List<Long> collegeIds = majors.stream()
                            .map(Major::getCollegeId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    
                    // 查询学院信息
                    java.util.Map<Long, College> collegeMap = new java.util.HashMap<>();
                    List<College> colleges = null;
                    if (!collegeIds.isEmpty()) {
                        colleges = collegeMapper.selectBatchIds(collegeIds);
                        if (colleges != null && !colleges.isEmpty()) {
                            collegeMap = colleges.stream()
                                    .filter(c -> c != null && c.getCollegeId() != null)
                                    .collect(Collectors.toMap(College::getCollegeId, c -> c, (v1, v2) -> v1));
                        }
                    }
                    
                    // 获取所有学校ID并查询学校信息
                    java.util.Map<Long, String> schoolNameMap = new java.util.HashMap<>();
                    if (colleges != null && !colleges.isEmpty()) {
                        List<Long> schoolIds = colleges.stream()
                                .map(College::getSchoolId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.toList());
                        
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
                    }
                    
                    // 获取所有班主任用户ID
                    List<Long> teacherUserIds = classes.stream()
                            .map(Class::getClassTeacherId)
                            .filter(Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    
                    // 查询班主任信息
                    java.util.Map<Long, String> teacherNameMap = new java.util.HashMap<>();
                    if (!teacherUserIds.isEmpty()) {
                        List<UserInfo> teachers = userMapper.selectBatchIds(teacherUserIds);
                        if (teachers != null && !teachers.isEmpty()) {
                            teacherNameMap = teachers.stream()
                                    .filter(t -> t != null && t.getUserId() != null && t.getRealName() != null)
                                    .collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getRealName, (v1, v2) -> v1));
                        }
                    }
                    
                    // 填充信息
                    for (Class classInfo : classes) {
                        if (classInfo.getMajorId() != null) {
                            Major major = majorMap.get(classInfo.getMajorId());
                            if (major != null) {
                                classInfo.setMajorName(major.getMajorName());
                                if (major.getCollegeId() != null) {
                                    College college = collegeMap.get(major.getCollegeId());
                                    if (college != null) {
                                        classInfo.setCollegeName(college.getCollegeName());
                                        if (college.getSchoolId() != null) {
                                            classInfo.setSchoolName(schoolNameMap.get(college.getSchoolId()));
                                        }
                                    }
                                }
                            }
                        }
                        if (classInfo.getClassTeacherId() != null) {
                            classInfo.setClassTeacherName(teacherNameMap.get(classInfo.getClassTeacherId()));
                        }
                    }
                }
            }
        }
        
        // 处理数据，转换状态和时间为文字
        for (Class classInfo : classes) {
            if (classInfo.getStatus() != null) {
                classInfo.setStatusText(classInfo.getStatus() == 1 ? "启用" : "禁用");
            } else {
                classInfo.setStatusText("");
            }
            if (classInfo.getCreateTime() != null) {
                classInfo.setCreateTimeText(classInfo.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                classInfo.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"班级ID", "班级名称", "班级代码", "所属专业", "所属学院", "所属学校", "入学年份", "班主任", "状态", "创建时间"};
        String[] fieldNames = {"classId", "className", "classCode", "majorName", "collegeName", "schoolName", "enrollmentYear", "classTeacherName", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, classes, headers, fieldNames, "班级列表");
    }
}

