package com.server.internshipserver.controller.enterprise;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperationApply;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationApplyService;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import com.server.internshipserver.service.system.SchoolService;
import com.server.internshipserver.service.user.EnterpriseService;
import com.server.internshipserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业合作管理控制器（企业管理员使用）
 */
@Api(tags = "企业合作管理")
@RestController
@RequestMapping("/enterprise/cooperation")
@PreAuthorize("hasRole('ROLE_ENTERPRISE_ADMIN')")
public class EnterpriseCooperationController {
    
    @Autowired
    private EnterpriseSchoolCooperationApplyService applyService;
    
    @Autowired
    private EnterpriseSchoolCooperationService cooperationService;
    
    @Autowired
    private EnterpriseService enterpriseService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private SchoolService schoolService;
    
    @ApiOperation("申请合作")
    @PostMapping("/apply")
    public Result<EnterpriseSchoolCooperationApply> applyCooperation(@RequestBody EnterpriseSchoolCooperationApply apply) {
        // 获取当前登录企业的ID
        Long currentEnterpriseId = enterpriseService.getEnterpriseByUserId(userService.getCurrentUser().getUserId()).getEnterpriseId();
        apply.setEnterpriseId(currentEnterpriseId);
        
        EnterpriseSchoolCooperationApply result = applyService.applyCooperation(apply);
        return Result.success("申请提交成功，等待学校审核", result);
    }
    
    @ApiOperation("查询企业的合作申请列表（包括所有状态）")
    @GetMapping("/apply/list")
    public Result<List<EnterpriseSchoolCooperationApply>> getApplyList() {
        // 获取当前登录企业的ID
        Long currentEnterpriseId = enterpriseService.getEnterpriseByUserId(userService.getCurrentUser().getUserId()).getEnterpriseId();
        List<EnterpriseSchoolCooperationApply> list = applyService.getApplyListByEnterpriseId(currentEnterpriseId);
        return Result.success("查询成功", list);
    }
    
    @ApiOperation("查询企业的合作关系列表（已建立的合作）")
    @GetMapping("/list")
    public Result<?> getCooperationList() {
        // 获取当前登录企业的ID
        Long currentEnterpriseId = enterpriseService.getEnterpriseByUserId(userService.getCurrentUser().getUserId()).getEnterpriseId();
        return Result.success("查询成功", cooperationService.getCooperationListByEnterpriseId(currentEnterpriseId));
    }
    
    @ApiOperation("获取所有学校列表（用于合作申请）")
    @GetMapping("/school/list")
    public Result<List<School>> getAvailableSchoolList() {
        // 查询所有学校（公开列表，不进行权限过滤）
        List<School> allSchools = schoolService.getPublicSchoolList();
        
        return Result.success("查询成功", allSchools);
    }
}

