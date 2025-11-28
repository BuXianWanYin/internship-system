package com.server.internshipserver.controller.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.evaluation.SelfEvaluation;
import com.server.internshipserver.service.evaluation.SelfEvaluationService;
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
 * 学生自评管理Controller
 */
@Api(tags = "学生自评管理")
@RestController
@RequestMapping("/evaluation/self")
public class SelfEvaluationController {
    
    @Autowired
    private SelfEvaluationService selfEvaluationService;
    
    @ApiOperation("添加或更新学生自评（支持草稿保存）")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping
    public Result<SelfEvaluation> saveOrUpdateEvaluation(@RequestBody SelfEvaluation evaluation) {
        SelfEvaluation result = selfEvaluationService.saveOrUpdateEvaluation(evaluation);
        return Result.success("保存成功", result);
    }
    
    @ApiOperation("提交学生自评")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping("/{evaluationId}/submit")
    public Result<?> submitEvaluation(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long evaluationId) {
        selfEvaluationService.submitEvaluation(evaluationId);
        return Result.success("提交成功");
    }
    
    @ApiOperation("查询学生自评详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/{evaluationId}")
    public Result<SelfEvaluation> getEvaluationById(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long evaluationId) {
        SelfEvaluation evaluation = selfEvaluationService.getEvaluationById(evaluationId);
        return Result.success(evaluation);
    }
    
    @ApiOperation("根据申请ID查询学生自评")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/apply/{applyId}")
    public Result<SelfEvaluation> getEvaluationByApplyId(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        SelfEvaluation evaluation = selfEvaluationService.getEvaluationByApplyId(applyId);
        return Result.success(evaluation);
    }
    
    @ApiOperation("分页查询学生自评列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<SelfEvaluation>> getEvaluationPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "评价状态", required = false) @RequestParam(required = false) Integer evaluationStatus) {
        Page<SelfEvaluation> page = new Page<>(current, size);
        Page<SelfEvaluation> result = selfEvaluationService.getEvaluationPage(page, studentId, evaluationStatus);
        return Result.success(result);
    }
}

