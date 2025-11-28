package com.server.internshipserver.controller.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.evaluation.SchoolEvaluation;
import com.server.internshipserver.service.evaluation.SchoolEvaluationService;
import com.server.internshipserver.service.impl.evaluation.SchoolEvaluationServiceImpl;
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

import java.math.BigDecimal;

/**
 * 学校评价管理Controller
 */
@Api(tags = "学校评价管理")
@RestController
@RequestMapping("/evaluation/school")
public class SchoolEvaluationController {
    
    @Autowired
    private SchoolEvaluationService schoolEvaluationService;
    
    @Autowired
    private SchoolEvaluationServiceImpl schoolEvaluationServiceImpl;
    
    @ApiOperation("添加或更新学校评价（支持草稿保存）")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping
    public Result<SchoolEvaluation> saveOrUpdateEvaluation(@RequestBody SchoolEvaluation evaluation) {
        SchoolEvaluation result = schoolEvaluationService.saveOrUpdateEvaluation(evaluation);
        return Result.success("保存成功", result);
    }
    
    @ApiOperation("提交学校评价")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/{evaluationId}/submit")
    public Result<?> submitEvaluation(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long evaluationId) {
        schoolEvaluationService.submitEvaluation(evaluationId);
        return Result.success("提交成功");
    }
    
    @ApiOperation("查询学校评价详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_STUDENT')")
    @GetMapping("/{evaluationId}")
    public Result<SchoolEvaluation> getEvaluationById(
            @ApiParam(value = "评价ID", required = true) @PathVariable Long evaluationId) {
        SchoolEvaluation evaluation = schoolEvaluationService.getEvaluationById(evaluationId);
        return Result.success(evaluation);
    }
    
    @ApiOperation("根据申请ID查询学校评价")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_STUDENT')")
    @GetMapping("/apply/{applyId}")
    public Result<SchoolEvaluation> getEvaluationByApplyId(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        SchoolEvaluation evaluation = schoolEvaluationService.getEvaluationByApplyId(applyId);
        return Result.success(evaluation);
    }
    
    @ApiOperation("计算日志周报质量建议分数")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/apply/{applyId}/suggested-score")
    public Result<BigDecimal> getSuggestedLogWeeklyReportScore(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        BigDecimal score = schoolEvaluationServiceImpl.calculateLogWeeklyReportScore(applyId);
        return Result.success(score);
    }
    
    @ApiOperation("分页查询学校评价列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<SchoolEvaluation>> getEvaluationPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生姓名", required = false) @RequestParam(required = false) String studentName,
            @ApiParam(value = "评价状态", required = false) @RequestParam(required = false) Integer evaluationStatus) {
        Page<SchoolEvaluation> page = new Page<>(current, size);
        Page<SchoolEvaluation> result = schoolEvaluationService.getEvaluationPage(page, studentName, evaluationStatus);
        return Result.success(result);
    }
}

