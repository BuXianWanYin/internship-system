package com.server.internshipserver.controller.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.InternshipPostQueryDTO;
import com.server.internshipserver.service.internship.InternshipPostService;
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

import java.util.List;

/**
 * 实习岗位管理控制器
 */
@Api(tags = "实习岗位管理")
@RestController
@RequestMapping("/internship/post")
public class InternshipPostController {
    
    @Autowired
    private InternshipPostService internshipPostService;
    
    @ApiOperation("发布岗位")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping
    public Result<InternshipPost> addPost(@RequestBody InternshipPost post) {
        InternshipPost result = internshipPostService.addPost(post);
        return Result.success("发布岗位成功", result);
    }
    
    @ApiOperation("更新岗位信息")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PutMapping
    public Result<InternshipPost> updatePost(@RequestBody InternshipPost post) {
        InternshipPost result = internshipPostService.updatePost(post);
        return Result.success("更新岗位成功", result);
    }
    
    @ApiOperation("分页查询岗位列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/page")
    public Result<Page<InternshipPost>> getPostPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "岗位名称", required = false) @RequestParam(required = false) String postName,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "状态", required = false) @RequestParam(required = false) Integer status,
            @ApiParam(value = "仅显示合作企业岗位（学生端使用）", required = false) @RequestParam(required = false) Boolean cooperationOnly) {
        Page<InternshipPost> page = new Page<>(current, size);
        InternshipPostQueryDTO queryDTO = new InternshipPostQueryDTO();
        queryDTO.setPostName(postName);
        queryDTO.setEnterpriseId(enterpriseId);
        queryDTO.setStatus(status);
        queryDTO.setCooperationOnly(cooperationOnly);
        Page<InternshipPost> result = internshipPostService.getPostPage(page, queryDTO);
        return Result.success(result);
    }
    
    @ApiOperation("查询岗位详情")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/{postId}")
    public Result<InternshipPost> getPostById(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        InternshipPost post = internshipPostService.getPostById(postId);
        return Result.success(post);
    }
    
    @ApiOperation("审核岗位")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @PostMapping("/{postId}/audit")
    public Result<?> auditPost(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId,
            @ApiParam(value = "审核状态（1-已通过，2-已拒绝）", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见", required = false) @RequestParam(required = false) String auditOpinion) {
        // 将Integer转换为枚举
        AuditStatus status = AuditStatus.getByCode(auditStatus);
        if (status == null) {
            throw new com.server.internshipserver.common.exception.BusinessException("审核状态无效");
        }
        // 注意：Service接口还未优化，暂时传递Integer，待Service接口优化后改为传递枚举
        internshipPostService.auditPost(postId, status.getCode(), auditOpinion);
        return Result.success("审核成功");
    }
    
    @ApiOperation("发布岗位")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{postId}/publish")
    public Result<?> publishPost(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        internshipPostService.publishPost(postId);
        return Result.success("发布成功");
    }
    
    @ApiOperation("关闭岗位")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{postId}/close")
    public Result<?> closePost(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        internshipPostService.closePost(postId);
        return Result.success("关闭成功");
    }
    
    @ApiOperation("删除岗位")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @DeleteMapping("/{postId}")
    public Result<?> deletePost(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        internshipPostService.deletePost(postId);
        return Result.success("删除成功");
    }
    
    @ApiOperation("查询岗位申请情况")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @GetMapping("/{postId}/applications")
    public Result<List<InternshipApply>> getPostApplications(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        List<InternshipApply> applications = internshipPostService.getPostApplications(postId);
        return Result.success(applications);
    }
}

