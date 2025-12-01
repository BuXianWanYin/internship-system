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
import com.server.internshipserver.common.utils.ExcelUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
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
    
    @ApiOperation("取消发布岗位")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{postId}/unpublish")
    public Result<?> unpublishPost(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        internshipPostService.unpublishPost(postId);
        return Result.success("取消发布成功");
    }
    
    @ApiOperation("下架岗位")
    @PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
    @PostMapping("/{postId}/close")
    public Result<?> closePost(
            @ApiParam(value = "岗位ID", required = true) @PathVariable Long postId) {
        internshipPostService.closePost(postId);
        return Result.success("下架成功");
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
    
    @ApiOperation("导出岗位列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    @GetMapping("/export")
    public void exportPosts(
            @ApiParam(value = "岗位名称", required = false) @RequestParam(required = false) String postName,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "状态", required = false) @RequestParam(required = false) Integer status,
            @ApiParam(value = "仅显示合作企业岗位（学生端使用）", required = false) @RequestParam(required = false) Boolean cooperationOnly,
            HttpServletResponse response) throws IOException {
        InternshipPostQueryDTO queryDTO = new InternshipPostQueryDTO();
        queryDTO.setPostName(postName);
        queryDTO.setEnterpriseId(enterpriseId);
        queryDTO.setStatus(status);
        queryDTO.setCooperationOnly(cooperationOnly);
        
        List<InternshipPost> posts = internshipPostService.getAllPosts(queryDTO);
        
        // 处理数据，转换状态和时间为文字
        for (InternshipPost post : posts) {
            // 转换状态
            if (post.getStatus() != null) {
                switch (post.getStatus()) {
                    case 0:
                        post.setStatusText("待审核");
                        break;
                    case 1:
                        post.setStatusText("已通过");
                        break;
                    case 2:
                        post.setStatusText("已拒绝");
                        break;
                    case 3:
                        post.setStatusText("已发布");
                        break;
                    case 4:
                        post.setStatusText("已关闭");
                        break;
                    default:
                        post.setStatusText("");
                }
            } else {
                post.setStatusText("");
            }
            // 转换日期
            if (post.getInternshipStartDate() != null) {
                post.setInternshipStartDateText(post.getInternshipStartDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                post.setInternshipStartDateText("");
            }
            if (post.getInternshipEndDate() != null) {
                post.setInternshipEndDateText(post.getInternshipEndDate().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            } else {
                post.setInternshipEndDateText("");
            }
            if (post.getPublishTime() != null) {
                post.setPublishTimeText(post.getPublishTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                post.setPublishTimeText("");
            }
            if (post.getCreateTime() != null) {
                post.setCreateTimeText(post.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                post.setCreateTimeText("");
            }
            // 转换薪资范围
            if (post.getSalaryMin() != null || post.getSalaryMax() != null) {
                String salaryType = post.getSalaryType() != null ? post.getSalaryType() : "月薪";
                if (post.getSalaryMin() != null && post.getSalaryMax() != null) {
                    post.setSalaryRangeText(post.getSalaryMin() + "-" + post.getSalaryMax() + "元/" + salaryType);
                } else if (post.getSalaryMin() != null) {
                    post.setSalaryRangeText(post.getSalaryMin() + "元/" + salaryType + "起");
                } else if (post.getSalaryMax() != null) {
                    post.setSalaryRangeText("最高" + post.getSalaryMax() + "元/" + salaryType);
                } else {
                    post.setSalaryRangeText("面议");
                }
            } else {
                post.setSalaryRangeText("面议");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"岗位ID", "岗位名称", "岗位编号", "所属企业", "工作地点", "详细地址", "招聘人数", "已申请人数", "已录用人数", "薪资范围", "实习开始日期", "实习结束日期", "工作时间", "联系人", "联系电话", "联系邮箱", "状态", "发布时间", "创建时间"};
        String[] fieldNames = {"postId", "postName", "postCode", "enterpriseName", "workLocation", "workAddress", "recruitCount", "appliedCount", "acceptedCount", "salaryRangeText", "internshipStartDateText", "internshipEndDateText", "workHours", "contactPerson", "contactPhone", "contactEmail", "statusText", "publishTimeText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, posts, headers, fieldNames, "岗位列表");
    }
}

