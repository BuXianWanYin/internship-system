package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.internship.dto.InternshipPlanQueryDTO;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.internship.InternshipPlanService;
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

/**
 * 实习计划管理控制器
 */
@Api(tags = "实习计划管理")
@RestController
@RequestMapping("/internship/plan")
public class InternshipPlanController {
    
    @Autowired
    private InternshipPlanService internshipPlanService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @ApiOperation("创建实习计划")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping
    public Result<InternshipPlan> addPlan(@RequestBody InternshipPlan plan) {
        InternshipPlan result = internshipPlanService.addPlan(plan);
        return Result.success("创建实习计划成功", result);
    }
    
    @ApiOperation("更新实习计划")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PutMapping
    public Result<InternshipPlan> updatePlan(@RequestBody InternshipPlan plan) {
        InternshipPlan result = internshipPlanService.updatePlan(plan);
        return Result.success("更新实习计划成功", result);
    }
    
    @ApiOperation("分页查询实习计划列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/page")
    public Result<Page<InternshipPlan>> getPlanPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "计划名称", required = false) @RequestParam(required = false) String planName,
            @ApiParam(value = "学期ID", required = false) @RequestParam(required = false) Long semesterId,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId,
            @ApiParam(value = "状态", required = false) @RequestParam(required = false) Integer status) {
        Page<InternshipPlan> page = new Page<>(current, size);
        InternshipPlanQueryDTO queryDTO = new InternshipPlanQueryDTO();
        queryDTO.setPlanName(planName);
        queryDTO.setSemesterId(semesterId);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setMajorId(majorId);
        queryDTO.setStatus(status);
        Page<InternshipPlan> result = internshipPlanService.getPlanPage(page, queryDTO);
        return Result.success(result);
    }
    
    @ApiOperation("查询实习计划详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_STUDENT')")
    @GetMapping("/{planId}")
    public Result<InternshipPlan> getPlanById(
            @ApiParam(value = "计划ID", required = true) @PathVariable Long planId) {
        InternshipPlan plan = internshipPlanService.getPlanById(planId);
        return Result.success(plan);
    }
    
    @ApiOperation("提交审核")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PostMapping("/{planId}/submit")
    public Result<?> submitPlan(
            @ApiParam(value = "计划ID", required = true) @PathVariable Long planId) {
        internshipPlanService.submitPlan(planId);
        return Result.success("提交审核成功");
    }
    
    @ApiOperation("审核实习计划")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PostMapping("/{planId}/audit")
    public Result<?> auditPlan(
            @ApiParam(value = "计划ID", required = true) @PathVariable Long planId,
            @ApiParam(value = "审核状态（1-已通过，2-已拒绝）", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见", required = false) @RequestParam(required = false) String auditOpinion) {
        // 将Integer转换为枚举
        // 注意：Service实现中使用AuditStatus枚举的code值（1-已通过，2-已拒绝）
        AuditStatus status = AuditStatus.getByCode(auditStatus);
        if (status == null || (status != AuditStatus.APPROVED && status != AuditStatus.REJECTED)) {
            throw new com.server.internshipserver.common.exception.BusinessException("审核状态无效，应为1（已通过）或2（已拒绝）");
        }
        // 注意：Service接口还未优化，暂时传递Integer，待Service接口优化后改为传递枚举
        internshipPlanService.auditPlan(planId, status.getCode(), auditOpinion);
        return Result.success("审核成功");
    }
    
    @ApiOperation("发布实习计划")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PostMapping("/{planId}/publish")
    public Result<?> publishPlan(
            @ApiParam(value = "计划ID", required = true) @PathVariable Long planId) {
        internshipPlanService.publishPlan(planId);
        return Result.success("发布成功");
    }
    
    @ApiOperation("删除实习计划")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @DeleteMapping("/{planId}")
    public Result<?> deletePlan(
            @ApiParam(value = "计划ID", required = true) @PathVariable Long planId) {
        internshipPlanService.deletePlan(planId);
        return Result.success("删除成功");
    }
    
    @ApiOperation("获取学生可用的实习计划列表")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/available")
    public Result<java.util.List<InternshipPlan>> getAvailablePlans() {
        // 获取当前登录学生信息
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        
        Student student = studentMapper.selectOne(
            new LambdaQueryWrapper<Student>()
                .eq(Student::getUserId, user.getUserId())
                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (student == null) {
            return Result.error("学生信息不存在");
        }
        
        java.util.List<InternshipPlan> plans = internshipPlanService.getAvailablePlansForStudent(student.getStudentId());
        return Result.success(plans);
    }
}

