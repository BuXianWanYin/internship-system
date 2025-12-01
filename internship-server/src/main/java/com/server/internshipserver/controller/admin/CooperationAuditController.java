package com.server.internshipserver.controller.admin;

import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperationApply;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationApplyService;
import com.server.internshipserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 合作申请审核控制器（学校管理员使用）
 */
@Api(tags = "合作申请审核管理")
@RestController
@RequestMapping("/admin/cooperation")
@PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
public class CooperationAuditController {
    
    @Autowired
    private EnterpriseSchoolCooperationApplyService applyService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @ApiOperation("查询待审核的合作申请列表")
    @GetMapping("/pending")
    public Result<List<EnterpriseSchoolCooperationApply>> getPendingApplyList(
            @ApiParam(value = "学校ID（系统管理员可指定，学校管理员自动使用当前学校）") @RequestParam(required = false) Long schoolId) {
        // 如果是学校管理员，自动使用当前用户的学校ID
        if (schoolId == null) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId == null && !dataPermissionUtil.isSystemAdmin()) {
                return Result.error("无法获取学校信息");
            }
            schoolId = currentUserSchoolId;
        }
        
        List<EnterpriseSchoolCooperationApply> list = applyService.getPendingApplyListBySchoolId(schoolId);
        return Result.success("查询成功", list);
    }
    
    @ApiOperation("审核合作申请")
    @PostMapping("/{id}/audit")
    public Result<?> auditCooperationApply(
            @ApiParam(value = "申请ID", required = true) @PathVariable Long id,
            @ApiParam(value = "审核状态：1-通过，2-拒绝", required = true) @RequestParam Integer auditStatus,
            @ApiParam(value = "审核意见") @RequestParam(required = false) String auditOpinion) {
        
        // 将Integer转换为枚举
        AuditStatus status = AuditStatus.getByCode(auditStatus);
        if (status == null) {
            return Result.error("审核状态无效");
        }
        
        // 获取当前登录用户ID作为审核人
        Long auditorId = userService.getCurrentUser().getUserId();
        
        boolean success = applyService.auditCooperationApply(id, auditStatus, auditOpinion, auditorId);
        return success ? Result.success("审核成功") : Result.error("审核失败");
    }
}

