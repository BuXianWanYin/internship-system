package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.utils.ExcelUtil;
import com.server.internshipserver.domain.user.dto.StudentImportDTO;
import com.server.internshipserver.domain.user.dto.StudentImportResult;
import com.server.internshipserver.domain.user.dto.StudentQueryDTO;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.service.user.StudentService;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 学生管理控制器
 */
@Api(tags = "学生管理")
@RestController
@RequestMapping("/user/student")
public class StudentController {
    
    @Autowired
    private StudentService studentService;
    
    @ApiOperation("分页查询学生列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Page<Student>> getStudentPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学号（可选）") @RequestParam(required = false) String studentNo,
            @ApiParam(value = "班级ID（可选）") @RequestParam(required = false) Long classId,
            @ApiParam(value = "专业ID（可选）") @RequestParam(required = false) Long majorId,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "状态：1-已审核，0-待审核（可选）") @RequestParam(required = false) Integer status,
            @ApiParam(value = "入学年份（可选）") @RequestParam(required = false) Integer enrollmentYear) {
        Page<Student> page = new Page<>(current, size);
        StudentQueryDTO queryDTO = new StudentQueryDTO();
        queryDTO.setStudentNo(studentNo);
        queryDTO.setClassId(classId);
        queryDTO.setMajorId(majorId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setStatus(status);
        queryDTO.setEnrollmentYear(enrollmentYear);
        Page<Student> result = studentService.getStudentPage(page, queryDTO);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询学生详情")
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Student> getStudentById(
            @ApiParam(value = "学生ID", required = true) @PathVariable Long studentId) {
        Student student = studentService.getStudentById(studentId);
        return Result.success("查询成功", student);
    }
    
    @ApiOperation("根据用户ID查询学生信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    public Result<Student> getStudentByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        Student student = studentService.getStudentByUserId(userId);
        return Result.success("查询成功", student);
    }
    
    @ApiOperation("添加学生")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Student> addStudent(@RequestBody Student student) {
        Student result = studentService.addStudent(student);
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新学生信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Student> updateStudent(@RequestBody Student student) {
        Student result = studentService.updateStudent(student);
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用学生（软删除）")
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<?> deleteStudent(
            @ApiParam(value = "学生ID", required = true) @PathVariable Long studentId) {
        boolean success = studentService.deleteStudent(studentId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
    
    @ApiOperation("下载学生导入Excel模板")
    @GetMapping("/import/template")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public void downloadImportTemplate(HttpServletResponse response) throws IOException {
        ExcelUtil.generateStudentImportTemplate(response);
    }
    
    @ApiOperation("Excel批量导入学生")
    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<StudentImportResult> importStudents(
            @ApiParam(value = "Excel文件", required = true) @RequestParam("file") MultipartFile file,
            @ApiParam(value = "班级ID（必填，所有导入的学生将使用此班级）") @RequestParam(required = true) Long classId) {
        StudentImportResult result = studentService.importStudentsFromFile(file, classId);
        String message = String.format("导入完成：成功 %d 条，失败 %d 条", 
                result.getSuccessCount(), result.getFailCount());
        return Result.success(message, result);
    }
    
    @ApiOperation("学生自主注册（使用分享码）")
    @PostMapping("/register")
    public Result<Student> registerStudent(
            @ApiParam(value = "学生注册信息", required = true) @RequestBody StudentImportDTO studentImportDTO,
            @ApiParam(value = "班级分享码", required = true) @RequestParam String shareCode) {
        Student student = studentService.registerStudentWithShareCode(studentImportDTO, shareCode);
        return Result.success("注册成功，等待班主任审核", student);
    }
    
    @ApiOperation("分页查询待审核学生列表（班主任审核）")
    @GetMapping("/pending-approval/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<Page<Student>> getPendingApprovalStudentPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学号（可选）") @RequestParam(required = false) String studentNo,
            @ApiParam(value = "姓名（可选）") @RequestParam(required = false) String realName) {
        Page<Student> page = new Page<>(current, size);
        Page<Student> result = studentService.getPendingApprovalStudentPage(page, studentNo, realName);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("审核学生注册申请")
    @PostMapping("/{studentId}/approve")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<?> approveStudentRegistration(
            @ApiParam(value = "学生ID", required = true) @PathVariable Long studentId,
            @ApiParam(value = "是否通过：true-通过，false-拒绝", required = true) @RequestParam Boolean approved,
            @ApiParam(value = "审核意见（可选）") @RequestParam(required = false) String auditOpinion) {
        boolean success = studentService.approveStudentRegistration(studentId, approved, auditOpinion);
        String message = approved ? "审核通过，学生账号已激活" : "审核已拒绝";
        return success ? Result.success(message) : Result.error("审核失败");
    }
    
    @ApiOperation("获取所有入学年份")
    @GetMapping("/enrollment-years")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    public Result<List<Integer>> getEnrollmentYears() {
        List<Integer> years = studentService.getDistinctEnrollmentYears();
        return Result.success(years);
    }
    
    @ApiOperation("导出学生列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/export")
    public void exportStudents(
            @ApiParam(value = "学号（可选）") @RequestParam(required = false) String studentNo,
            @ApiParam(value = "学校ID（可选）") @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID（可选）") @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "专业ID（可选）") @RequestParam(required = false) Long majorId,
            @ApiParam(value = "班级ID（可选）") @RequestParam(required = false) Long classId,
            @ApiParam(value = "状态：1-已审核，0-待审核（可选）") @RequestParam(required = false) Integer status,
            @ApiParam(value = "入学年份（可选）") @RequestParam(required = false) Integer enrollmentYear,
            HttpServletResponse response) throws IOException {
        StudentQueryDTO queryDTO = new StudentQueryDTO();
        queryDTO.setStudentNo(studentNo);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setMajorId(majorId);
        queryDTO.setClassId(classId);
        queryDTO.setStatus(status);
        queryDTO.setEnrollmentYear(enrollmentYear);
        
        List<Student> students = studentService.getAllStudents(queryDTO);
        
        // 定义表头和字段名
        String[] headers = {"学号", "真实姓名", "手机号", "邮箱", "班级", "专业", "学院", "入学年份", "状态", "创建时间"};
        String[] fieldNames = {"studentNo", "realName", "phone", "email", "className", "majorName", "collegeName", "enrollmentYear", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, students, headers, fieldNames, "学生列表");
    }
}

