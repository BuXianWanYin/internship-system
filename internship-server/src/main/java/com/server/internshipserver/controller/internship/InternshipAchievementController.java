package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.internship.InternshipAchievement;
import com.server.internshipserver.service.internship.InternshipAchievementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 阶段性成果管理控制器
 */
@Api(tags = "阶段性成果管理")
@RestController
@RequestMapping("/internship/achievement")
public class InternshipAchievementController {
    
    @Autowired
    private InternshipAchievementService internshipAchievementService;
    
    @ApiOperation("提交阶段性成果")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PostMapping
    public Result<InternshipAchievement> addAchievement(@RequestBody InternshipAchievement achievement) {
        InternshipAchievement result = internshipAchievementService.addAchievement(achievement);
        return Result.success("提交成果成功", result);
    }
    
    @ApiOperation("更新阶段性成果")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @PutMapping
    public Result<InternshipAchievement> updateAchievement(@RequestBody InternshipAchievement achievement) {
        InternshipAchievement result = internshipAchievementService.updateAchievement(achievement);
        return Result.success("更新成果成功", result);
    }
    
    @ApiOperation("分页查询阶段性成果列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/page")
    public Result<Page<InternshipAchievement>> getAchievementPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "学生ID", required = false) @RequestParam(required = false) Long studentId,
            @ApiParam(value = "申请ID", required = false) @RequestParam(required = false) Long applyId,
            @ApiParam(value = "成果类型", required = false) @RequestParam(required = false) String achievementType,
            @ApiParam(value = "审核状态", required = false) @RequestParam(required = false) Integer reviewStatus) {
        Page<InternshipAchievement> page = new Page<>(current, size);
        Page<InternshipAchievement> result = internshipAchievementService.getAchievementPage(page, studentId, applyId, achievementType, reviewStatus);
        return Result.success(result);
    }
    
    @ApiOperation("查询阶段性成果详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_STUDENT', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/{achievementId}")
    public Result<InternshipAchievement> getAchievementById(
            @ApiParam(value = "成果ID", required = true) @PathVariable Long achievementId) {
        InternshipAchievement achievement = internshipAchievementService.getAchievementById(achievementId);
        return Result.success(achievement);
    }
    
    @ApiOperation("审核成果")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_CLASS_TEACHER', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @PostMapping("/{achievementId}/review")
    public Result<?> reviewAchievement(
            @ApiParam(value = "成果ID", required = true) @PathVariable Long achievementId,
            @ApiParam(value = "审核状态（1-已通过，2-已拒绝）", required = true) @RequestParam Integer reviewStatus,
            @ApiParam(value = "审核意见", required = false) @RequestParam(required = false) String reviewComment) {
        internshipAchievementService.reviewAchievement(achievementId, reviewStatus, reviewComment);
        return Result.success("审核成功");
    }
    
    @ApiOperation("删除成果")
    @PreAuthorize("hasRole('ROLE_STUDENT')")
    @DeleteMapping("/{achievementId}")
    public Result<?> deleteAchievement(
            @ApiParam(value = "成果ID", required = true) @PathVariable Long achievementId) {
        internshipAchievementService.deleteAchievement(achievementId);
        return Result.success("删除成功");
    }
}

