package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.AuditApplyDTO;
import com.server.internshipserver.domain.internship.dto.AuditUnbindDTO;
import com.server.internshipserver.domain.internship.dto.FilterApplyDTO;
import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.FilterAction;
import com.server.internshipserver.service.internship.InternshipApplyService;
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
 * 实习申请管理控制器
 */
@Api(tags = "实习申请管理")
@RestController
@RequestMapping("/internship/apply")
public class InternshipApplyController {
    
    @Autowired
    private InternshipApplyService internshipApplyService;
    
    @ApiOperation("提交实习申请（选择合作企业）")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/cooperation")
    public Result<InternshipApply> addCooperationApply(@RequestBody InternshipApply apply) {
        InternshipApply result = internshipApplyService.addCooperationApply(apply);
        return Result.success("提交申请成功", result);
    }
    
    @ApiOperation("提交实习申请（自主实习）")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/self")
    public Result<InternshipApply> addSelfApply(@RequestBody InternshipApply apply) {
        InternshipApply result = internshipApplyService.addSelfApply(apply);
        return Result.success("提交申请成功", result);
    }
    
    @ApiOperation("更新实习申请")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PutMapping
    public Result<InternshipApply> updateApply(@RequestBody InternshipApply apply) {
        InternshipApply result = internshipApplyService.updateApply(apply);
        return Result.success("更新申请成功", result);
    }
    
    @ApiOperation("分页查询实习申请列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/page")
    public Result<Page<InternshipApply>> getApplyPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "岗位ID", required = false) @RequestParam(required = false) Long postId,
            @ApiParam(value = "申请类型", required = false) @RequestParam(required = false) Integer applyType,
            @ApiParam(value = "状态", required = false) @RequestParam(required = false) Integer status) {
        Page<InternshipApply> page = new Page<>(current, size);
        InternshipApplyQueryDTO queryDTO = new InternshipApplyQueryDTO();
        queryDTO.setStudentId(studentId);
        queryDTO.setEnterpriseId(enterpriseId);
        queryDTO.setPostId(postId);
        queryDTO.setApplyType(applyType);
        queryDTO.setStatus(status);
        Page<InternshipApply> result = internshipApplyService.getApplyPage(page, queryDTO);
        return Result.success(result);
    }
    
    @ApiOperation("查询实习申请详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/{applyId}")
    public Result<InternshipApply> getApplyById(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        InternshipApply apply = internshipApplyService.getApplyById(applyId);
        return Result.success(apply);
    }
    
    @ApiOperation("审核实习申请（合作企业和自主实习）")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @PostMapping("/{applyId}/audit")
    public Result<?> auditApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "审核状态（1-已通过，2-已拒绝）", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见", required = false) @RequestParam(required = false) String auditOpinion) {
        AuditApplyDTO auditDTO = new AuditApplyDTO();
        auditDTO.setAuditStatus(AuditStatus.getByCode(auditStatus));
        auditDTO.setAuditOpinion(auditOpinion);
        internshipApplyService.auditApply(applyId, auditDTO);
        return Result.success("审核成功");
    }
    
    @ApiOperation("企业筛选操作")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{applyId}/filter")
    public Result<?> filterApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "操作类型（1-标记感兴趣，2-安排面试，3-录用，4-拒绝）", required = true) @RequestParam Integer action,
            @ApiParam(value = "备注", required = false) @RequestParam(required = false) String comment) {
        FilterApplyDTO filterDTO = new FilterApplyDTO();
        filterDTO.setAction(FilterAction.getByCode(action));
        filterDTO.setComment(comment);
        internshipApplyService.filterApply(applyId, filterDTO);
        return Result.success("操作成功");
    }
    
    @ApiOperation("取消申请")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/{applyId}/cancel")
    public Result<?> cancelApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        internshipApplyService.cancelApply(applyId);
        return Result.success("取消成功");
    }
    
    @ApiOperation("删除申请")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @DeleteMapping("/{applyId}")
    public Result<?> deleteApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        internshipApplyService.deleteApply(applyId);
        return Result.success("删除成功");
    }
    
    @ApiOperation("查询企业实习学生列表（仅显示已录用的学生）")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/enterprise/students")
    public Result<Page<InternshipApply>> getEnterpriseStudents(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生姓名", required = false) @RequestParam(required = false) String studentName,
            @ApiParam(value = "学号", required = false) @RequestParam(required = false) String studentNo,
            @ApiParam(value = "岗位ID", required = false) @RequestParam(required = false) Long postId) {
        Page<InternshipApply> page = new Page<>(current, size);
        Page<InternshipApply> result = internshipApplyService.getEnterpriseStudents(page, studentName, studentNo, postId);
        return Result.success(result);
    }
    
    @ApiOperation("给学生分配企业导师")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{applyId}/assign-mentor")
    public Result<?> assignMentor(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "企业导师ID", required = true) @RequestParam Long mentorId) {
        internshipApplyService.assignMentor(applyId, mentorId);
        return Result.success("分配成功");
    }
    
    @ApiOperation("学生确认上岗")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/{applyId}/confirm-onboard")
    public Result<?> confirmOnboard(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        internshipApplyService.confirmOnboard(applyId);
        return Result.success("确认上岗成功");
    }
    
    @ApiOperation("学生申请离职/解绑")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/{applyId}/apply-unbind")
    public Result<?> applyUnbind(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "离职原因", required = true) @RequestParam String reason) {
        internshipApplyService.applyUnbind(applyId, reason);
        return Result.success("离职申请提交成功，等待审核");
    }
    
    @ApiOperation("审核解绑申请（班主任/学院负责人/学校管理员/企业管理员/企业导师）")
    @PreAuthorize("hasAnyRole('ROLE_CLASS_TEACHER', 'ROLE_COLLEGE_LEADER', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{applyId}/audit-unbind")
    public Result<?> auditUnbind(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "审核状态（2-已解绑，3-解绑被拒绝）", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见", required = false) @RequestParam(required = false) String auditOpinion) {
        AuditUnbindDTO auditDTO = new AuditUnbindDTO();
        auditDTO.setAuditStatus(AuditStatus.getByCode(auditStatus));
        auditDTO.setAuditOpinion(auditOpinion);
        internshipApplyService.auditUnbind(applyId, auditDTO);
        return Result.success("审核成功");
    }
    
    @ApiOperation("获取当前学生的实习申请（已确认上岗的）")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @GetMapping("/current")
    public Result<InternshipApply> getCurrentInternship() {
        InternshipApply apply = internshipApplyService.getCurrentInternship();
        if (apply == null) {
            return Result.success(null);
        }
        return Result.success(apply);
    }
}

