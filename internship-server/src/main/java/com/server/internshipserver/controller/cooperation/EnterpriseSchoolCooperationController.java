package com.server.internshipserver.controller.cooperation;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业学校合作关系管理控制器
 */
@Api(tags = "企业学校合作关系管理")
@RestController
@RequestMapping("/cooperation/enterprise-school")
public class EnterpriseSchoolCooperationController {
    
    @Autowired
    private EnterpriseSchoolCooperationService cooperationService;
    
    @ApiOperation("添加合作关系")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<EnterpriseSchoolCooperation> addCooperation(@RequestBody EnterpriseSchoolCooperation cooperation) {
        EnterpriseSchoolCooperation result = cooperationService.addCooperation(cooperation);
        return Result.success("添加合作关系成功", result);
    }
    
    @ApiOperation("更新合作关系信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<EnterpriseSchoolCooperation> updateCooperation(@RequestBody EnterpriseSchoolCooperation cooperation) {
        EnterpriseSchoolCooperation result = cooperationService.updateCooperation(cooperation);
        return Result.success("更新合作关系成功", result);
    }
    
    @ApiOperation("删除合作关系（软删除）")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<?> deleteCooperation(
            @ApiParam(value = "合作关系ID", required = true) @PathVariable Long id) {
        boolean success = cooperationService.deleteCooperation(id);
        return success ? Result.success("删除合作关系成功") : Result.error("删除合作关系失败");
    }
    
    @ApiOperation("根据企业ID查询合作学校列表")
    @GetMapping("/enterprise/{enterpriseId}/schools")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<List<School>> getCooperationSchoolsByEnterpriseId(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        List<School> schools = cooperationService.getCooperationSchoolsByEnterpriseId(enterpriseId);
        return Result.success("查询成功", schools);
    }
    
    @ApiOperation("根据学校ID查询合作企业ID列表")
    @GetMapping("/school/{schoolId}/enterprises")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    public Result<List<Long>> getCooperationEnterpriseIdsBySchoolId(
            @ApiParam(value = "学校ID", required = true) @PathVariable Long schoolId) {
        List<Long> enterpriseIds = cooperationService.getCooperationEnterpriseIdsBySchoolId(schoolId);
        return Result.success("查询成功", enterpriseIds);
    }
    
    @ApiOperation("检查企业和学校是否有合作关系")
    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_STUDENT')")
    public Result<Boolean> hasCooperation(
            @ApiParam(value = "企业ID", required = true) @RequestParam Long enterpriseId,
            @ApiParam(value = "学校ID", required = true) @RequestParam Long schoolId) {
        boolean hasCooperation = cooperationService.hasCooperation(enterpriseId, schoolId);
        return Result.success("查询成功", hasCooperation);
    }
    
    @ApiOperation("根据企业ID查询合作关系列表（包含完整信息）")
    @GetMapping("/enterprise/{enterpriseId}/list")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<List<EnterpriseSchoolCooperation>> getCooperationListByEnterpriseId(
            @ApiParam(value = "企业ID", required = true) @PathVariable Long enterpriseId) {
        List<EnterpriseSchoolCooperation> cooperations = cooperationService.getCooperationListByEnterpriseId(enterpriseId);
        return Result.success("查询成功", cooperations);
    }
}

