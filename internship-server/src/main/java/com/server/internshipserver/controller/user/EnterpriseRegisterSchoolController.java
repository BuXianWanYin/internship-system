package com.server.internshipserver.controller.user;

import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.EnterpriseRegisterSchool;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.service.user.EnterpriseRegisterSchoolService;
import com.server.internshipserver.service.user.UserService;
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

import java.util.List;

/**
 * 企业注册申请院校关联控制器
 */
@Api(tags = "企业注册申请院校关联管理")
@RestController
@RequestMapping("/user/enterprise-register-school")
public class EnterpriseRegisterSchoolController {
    
    @Autowired
    private EnterpriseRegisterSchoolService enterpriseRegisterSchoolService;
    
    @Autowired
    private UserService userService;
    
    @ApiOperation("根据企业ID查询注册申请院校列表")
    @GetMapping("/enterprise/{enterpriseId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<List<EnterpriseRegisterSchool>> getByEnterpriseId(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        List<EnterpriseRegisterSchool> list = enterpriseRegisterSchoolService.getByEnterpriseId(enterpriseId);
        return Result.success("查询成功", list);
    }
    
    @ApiOperation("根据学校ID查询待审核的企业注册申请列表")
    @GetMapping("/school/{schoolId}/pending")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<List<EnterpriseRegisterSchool>> getPendingBySchoolId(
            @ApiParam(value = "学校ID", required = true) @PathVariable Long schoolId) {
        List<EnterpriseRegisterSchool> list = enterpriseRegisterSchoolService.getPendingBySchoolId(schoolId);
        return Result.success("查询成功", list);
    }
    
    @ApiOperation("审核企业注册申请（按院校）")
    @PostMapping("/{id}/audit")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<?> auditEnterpriseRegister(
            @ApiParam(value = "关联ID", required = true) @PathVariable Long id,
            @ApiParam(value = "审核状态：1-通过，2-拒绝", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见") @RequestParam(required = false) String auditOpinion) {
        // 将Integer转换为枚举
        AuditStatus status = AuditStatus.getByCode(auditStatus);
        if (status == null) {
            throw new com.server.internshipserver.common.exception.BusinessException("审核状态无效");
        }
        // 获取当前登录用户ID作为审核人
        UserInfo user = userService.getCurrentUser();
        
        // 注意：Service接口还未优化，暂时传递Integer，待Service接口优化后改为传递枚举
        boolean success = enterpriseRegisterSchoolService.auditEnterpriseRegister(
            id, status.getCode(), auditOpinion, user.getUserId());
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }
}

