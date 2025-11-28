package com.server.internshipserver.controller.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.evaluation.EnterpriseEvaluation;
import com.server.internshipserver.service.evaluation.EnterpriseEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 企业评价管理Controller
 */
@Api(tags = "企业评价管理")
@RestController
@RequestMapping("/evaluation/enterprise")
public class EnterpriseEvaluationController {
    
    @Autowired
    private EnterpriseEvaluationService enterpriseEvaluationService;
    
    @ApiOperation("添加或更新企业评价（支持草稿保存）")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping
    public Result<EnterpriseEvaluation> saveOrUpdateEvaluation(@RequestBody EnterpriseEvaluation evaluation) {
        EnterpriseEvaluation result = enterpriseEvaluationService.saveOrUpdateEvaluation(evaluation);
        return Result.success("保存成功", result);
    }
    
    @ApiOperation("提交企业评价")
    @PreAuthorize("hasAnyRole('ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @PostMapping("/{evaluationId}/submit")
    public Result<?> submitEvaluation(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long evaluationId) {
        enterpriseEvaluationService.submitEvaluation(evaluationId);
        return Result.success("提交成功");
    }
    
    @ApiOperation("查询企业评价详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_STUDENT')")
    @GetMapping("/{evaluationId}")
    public Result<EnterpriseEvaluation> getEvaluationById(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long evaluationId) {
        EnterpriseEvaluation evaluation = enterpriseEvaluationService.getEvaluationById(evaluationId);
        return Result.success(evaluation);
    }
    
    @ApiOperation("根据申请ID查询企业评价")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_STUDENT')")
    @GetMapping("/apply/{applyId}")
    public Result<EnterpriseEvaluation> getEvaluationByApplyId(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        EnterpriseEvaluation evaluation = enterpriseEvaluationService.getEvaluationByApplyId(applyId);
        return Result.success(evaluation);
    }
    
    @ApiOperation("分页查询企业评价列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/page")
    public Result<Page<EnterpriseEvaluation>> getEvaluationPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "学生姓名", required = false) @RequestParam(required = false) String studentName,
            @ApiParam(value = "评价状态", required = false) @RequestParam(required = false) Integer evaluationStatus) {
        Page<EnterpriseEvaluation> page = new Page<>(current, size);
        Page<EnterpriseEvaluation> result = enterpriseEvaluationService.getEvaluationPage(page, enterpriseId, studentName, evaluationStatus);
        return Result.success(result);
    }
}

