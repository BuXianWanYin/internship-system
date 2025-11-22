package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.service.internship.InternshipApplyService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
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
        Page<InternshipApply> result = internshipApplyService.getApplyPage(page, studentId, enterpriseId, postId, applyType, status);
        return Result.success(result);
    }
    
    @ApiOperation("查询实习申请详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/{applyId}")
    public Result<InternshipApply> getApplyById(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        InternshipApply apply = internshipApplyService.getApplyById(applyId);
        return Result.success(apply);
    }
    
    @ApiOperation("审核实习申请（自主实习）")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_CLASS_TEACHER')")
    @PostMapping("/{applyId}/audit")
    public Result<?> auditApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "审核状态（1-已通过，2-已拒绝）", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见", required = false) @RequestParam(required = false) String auditOpinion) {
        internshipApplyService.auditApply(applyId, auditStatus, auditOpinion);
        return Result.success("审核成功");
    }
    
    @ApiOperation("企业筛选操作")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{applyId}/filter")
    public Result<?> filterApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId,
            @ApiParam(value = "操作类型（1-标记感兴趣，2-安排面试，3-录用，4-拒绝）", required = true) @RequestParam Integer action,
            @ApiParam(value = "备注", required = false) @RequestParam(required = false) String comment) {
        internshipApplyService.filterApply(applyId, action, comment);
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
}

