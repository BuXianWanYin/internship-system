package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.dto.TeacherAddDTO;
import com.server.internshipserver.domain.user.dto.TeacherUpdateDTO;
import com.server.internshipserver.domain.user.dto.TeacherRegisterDTO;
import com.server.internshipserver.service.user.TeacherService;

import java.util.List;
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
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.domain.user.UserInfo;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 教师管理控制器
 */
@Api(tags = "教师管理")
@RestController
@RequestMapping("/user/teacher")
public class TeacherController {
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private SchoolService schoolService;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @ApiOperation("分页查询教师列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Page<Teacher>> getTeacherPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "工号") @RequestParam(required = false) String teacherNo,
            @ApiParam(value = "学院ID") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "状态：1-启用，0-禁用") @RequestParam(required = false) Integer status) {
        Page<Teacher> page = new Page<>(current, size);
        Page<Teacher> result = teacherService.getTeacherPage(page, teacherNo, collegeId, schoolId, status);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询教师详情")
    @GetMapping("/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> getTeacherById(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId) {
        Teacher teacher = teacherService.getTeacherById(teacherId);
        return Result.success("查询成功", teacher);
    }
    
    @ApiOperation("根据用户ID查询教师信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> getTeacherByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Teacher teacher = teacherService.getTeacherByUserId(userId);
        return Result.success("查询成功", teacher);
    }
    
    @ApiOperation("添加教师")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> addTeacher(@RequestBody TeacherAddDTO teacherAddDTO) {
        Teacher result = teacherService.addTeacherWithUser(teacherAddDTO);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新教师信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Teacher> updateTeacher(@RequestBody TeacherUpdateDTO teacherUpdateDTO) {
        Teacher result = teacherService.updateTeacherWithUser(teacherUpdateDTO);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("删除教师（逻辑删除）")
    @DeleteMapping("/{teacherId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<?> deleteTeacher(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId) {
        boolean success = teacherService.deleteTeacher(teacherId);
        return success ? Result.success("删除成功") : Result.error("删除失败");
    }
    
    @ApiOperation("根据学校ID查询教师列表（用于下拉选择）")
    @GetMapping("/list")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<List<Teacher>> getTeacherList(
            @ApiParam(value = "学校ID") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID") @RequestParam(required = false) Long collegeId) {
        List<Teacher> list = teacherService.getTeacherListBySchool(schoolId, collegeId);
        return Result.success("查询成功", list);
    }
    
    @ApiOperation("导出教师列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/export")
    public void exportTeachers(
            @ApiParam(value = "工号", required = false) @RequestParam(required = false) String teacherNo,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "状态：1-启用，0-禁用", required = false) @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        List<Teacher> teachers = teacherService.getAllTeachers(teacherNo, collegeId, schoolId, status);
        
        // 填充关联信息
        if (teachers != null && !teachers.isEmpty()) {
            // 获取所有学院ID
            List<Long> collegeIds = teachers.stream()
                    .map(Teacher::getCollegeId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询学院信息
            java.util.Map<Long, College> collegeMap = new java.util.HashMap<>();
            if (!collegeIds.isEmpty()) {
                List<College> colleges = collegeMapper.selectBatchIds(collegeIds);
                if (colleges != null && !colleges.isEmpty()) {
                    collegeMap = colleges.stream()
                            .filter(c -> c != null && c.getCollegeId() != null)
                            .collect(Collectors.toMap(College::getCollegeId, c -> c, (v1, v2) -> v1));
                }
            }
            
            // 获取所有学校ID
            List<Long> schoolIds = teachers.stream()
                    .map(Teacher::getSchoolId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询学校信息
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
            
            // 获取所有用户ID
            List<Long> userIds = teachers.stream()
                    .map(Teacher::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询用户信息
            java.util.Map<Long, UserInfo> userMap = new java.util.HashMap<>();
            if (!userIds.isEmpty()) {
                List<UserInfo> users = userMapper.selectBatchIds(userIds);
                if (users != null && !users.isEmpty()) {
                    userMap = users.stream()
                            .filter(u -> u != null && u.getUserId() != null)
                            .collect(Collectors.toMap(UserInfo::getUserId, u -> u, (v1, v2) -> v1));
                }
            }
            
            // 填充信息
            for (Teacher teacher : teachers) {
                if (teacher.getCollegeId() != null) {
                    College college = collegeMap.get(teacher.getCollegeId());
                    if (college != null) {
                        teacher.setCollegeName(college.getCollegeName());
                    }
                }
                if (teacher.getSchoolId() != null) {
                    teacher.setSchoolName(schoolNameMap.get(teacher.getSchoolId()));
                }
                if (teacher.getUserId() != null) {
                    UserInfo user = userMap.get(teacher.getUserId());
                    if (user != null) {
                        teacher.setTeacherName(user.getRealName());
                        teacher.setPhone(user.getPhone());
                        teacher.setEmail(user.getEmail());
                        // 从UserInfo获取状态并设置statusText
                        if (user.getStatus() != null) {
                            teacher.setStatusText(user.getStatus() == 1 ? "启用" : "禁用");
                        } else {
                            teacher.setStatusText("");
                        }
                    } else {
                        teacher.setStatusText("");
                    }
                } else {
                    teacher.setStatusText("");
                }
            }
        }
        
        // 处理数据，转换时间为文字
        for (Teacher teacher : teachers) {
            if (teacher.getCreateTime() != null) {
                teacher.setCreateTimeText(teacher.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                teacher.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"教师ID", "工号", "姓名", "所属学院", "所属学校", "职称", "部门", "手机号", "邮箱", "状态", "创建时间"};
        String[] fieldNames = {"teacherId", "teacherNo", "teacherName", "collegeName", "schoolName", "title", "department", "phone", "email", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, teachers, headers, fieldNames, "教师列表");
    }
    
    @ApiOperation("教师自主注册")
    @PostMapping("/register")
    public Result<Teacher> registerTeacher(
            @ApiParam(value = "教师注册信息", required = true) @RequestBody TeacherRegisterDTO registerDTO) {
        Teacher teacher = teacherService.registerTeacher(registerDTO);
        return Result.success("注册成功，请等待管理员审核", teacher);
    }
    
    @ApiOperation("分页查询待审核教师列表")
    @GetMapping("/pending-approval/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<Page<Teacher>> getPendingApprovalTeacherPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "工号") @RequestParam(required = false) String teacherNo,
            @ApiParam(value = "姓名") @RequestParam(required = false) String realName) {
        Page<Teacher> page = new Page<>(current, size);
        Page<Teacher> result = teacherService.getPendingApprovalTeacherPage(page, teacherNo, realName);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("审核教师注册申请")
    @PostMapping("/{teacherId}/approve")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    public Result<?> approveTeacherRegistration(
            @ApiParam(value = "教师ID", required = true) @PathVariable Long teacherId,
            @ApiParam(value = "是否通过：true-通过，false-拒绝", required = true) @RequestParam Boolean approved,
            @ApiParam(value = "审核意见") @RequestParam(required = false) String auditOpinion) {
        boolean success = teacherService.approveTeacherRegistration(teacherId, approved, auditOpinion);
        String message = approved ? "审核通过，教师账号已激活" : "审核已拒绝";
        return success ? Result.success(message) : Result.error("审核失败");
    }
}

