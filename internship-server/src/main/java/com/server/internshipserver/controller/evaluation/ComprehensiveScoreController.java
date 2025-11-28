package com.server.internshipserver.controller.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;
import com.server.internshipserver.service.evaluation.ComprehensiveScoreService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 综合成绩管理Controller
 */
@Api(tags = "综合成绩管理")
@RestController
@RequestMapping("/evaluation/comprehensive-score")
public class ComprehensiveScoreController {
    
    @Autowired
    private ComprehensiveScoreService comprehensiveScoreService;
    
    @ApiOperation("计算综合成绩")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/apply/{applyId}/calculate")
    public Result<ComprehensiveScore> calculateComprehensiveScore(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        ComprehensiveScore score = comprehensiveScoreService.calculateComprehensiveScore(applyId);
        return Result.success("计算成功", score);
    }
    
    @ApiOperation("查询综合成绩详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/{scoreId}")
    public Result<ComprehensiveScore> getScoreById(
            @ApiParam(value = "成绩ID", required = true) @PathVariable Long scoreId) {
        ComprehensiveScore score = comprehensiveScoreService.getScoreById(scoreId);
        return Result.success(score);
    }
    
    @ApiOperation("根据申请ID查询综合成绩")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/apply/{applyId}")
    public Result<ComprehensiveScore> getScoreByApplyId(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        ComprehensiveScore score = comprehensiveScoreService.getScoreByApplyId(applyId);
        return Result.success(score);
    }
    
    @ApiOperation("分页查询综合成绩列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<ComprehensiveScore>> getScorePage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生姓名", required = false) @RequestParam(required = false) String studentName) {
        Page<ComprehensiveScore> page = new Page<>(current, size);
        Page<ComprehensiveScore> result = comprehensiveScoreService.getScorePage(page, studentName);
        return Result.success(result);
    }
    
    @ApiOperation("检查三个评价是否都完成")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/apply/{applyId}/check-completed")
    public Result<Boolean> checkAllEvaluationsCompleted(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long applyId) {
        boolean completed = comprehensiveScoreService.checkAllEvaluationsCompleted(applyId);
        return Result.success(completed);
    }
}

