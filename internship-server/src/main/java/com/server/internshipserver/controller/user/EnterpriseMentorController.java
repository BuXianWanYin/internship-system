package com.server.internshipserver.controller.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import com.server.internshipserver.domain.user.dto.EnterpriseMentorAddDTO;
import com.server.internshipserver.domain.user.dto.EnterpriseMentorUpdateDTO;
import com.server.internshipserver.service.user.EnterpriseMentorService;
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
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.domain.user.Enterprise;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Objects;

/**
 * 企业导师管理控制器
 */
@Api(tags = "企业导师管理")
@RestController
@RequestMapping("/user/enterprise-mentor")
public class EnterpriseMentorController {
    
    @Autowired
    private EnterpriseMentorService enterpriseMentorService;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @ApiOperation("分页查询企业导师列表")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<Page<EnterpriseMentor>> getEnterpriseMentorPage(
            @ApiParam(value = "页码", example = "1") @RequestParam(defaultValue = "1") Long current,
            @ApiParam(value = "每页数量", example = "10") @RequestParam(defaultValue = "10") Long size,
            @ApiParam(value = "导师姓名（可选）") @RequestParam(required = false) String mentorName,
            @ApiParam(value = "企业ID（可选）") @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "状态：1-启用，0-禁用（可选）") @RequestParam(required = false) Integer status) {
        Page<EnterpriseMentor> page = new Page<>(current, size);
        Page<EnterpriseMentor> result = enterpriseMentorService.getEnterpriseMentorPage(page, mentorName, enterpriseId, status);
        return Result.success("查询成功", result);
    }
    
    @ApiOperation("根据ID查询企业导师详情")
    @GetMapping("/{mentorId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> getEnterpriseMentorById(
            @ApiParam(value = "企业导师ID", required = true) @PathVariable Long mentorId) {
        EnterpriseMentor mentor = enterpriseMentorService.getEnterpriseMentorById(mentorId);
        return Result.success("查询成功", mentor);
    }
    
    @ApiOperation("根据用户ID查询企业导师信息")
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> getEnterpriseMentorByUserId(
            @ApiParam(value = "用户ID", required = true) @PathVariable Long userId) {
        EnterpriseMentor mentor = enterpriseMentorService.getEnterpriseMentorByUserId(userId);
        return Result.success("查询成功", mentor);
    }
    
    @ApiOperation("添加企业导师")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> addEnterpriseMentor(@RequestBody EnterpriseMentorAddDTO mentorAddDTO) {
        EnterpriseMentor result = enterpriseMentorService.addEnterpriseMentorWithUser(
                mentorAddDTO.getMentorName(),
                mentorAddDTO.getEnterpriseId(),
                mentorAddDTO.getPosition(),
                mentorAddDTO.getDepartment(),
                mentorAddDTO.getPhone(),
                mentorAddDTO.getEmail(),
                mentorAddDTO.getPassword(),
                mentorAddDTO.getStatus()
        );
        return Result.success("添加成功", result);
    }
    
    @ApiOperation("更新企业导师信息")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<EnterpriseMentor> updateEnterpriseMentor(@RequestBody EnterpriseMentorUpdateDTO mentorUpdateDTO) {
        EnterpriseMentor result = enterpriseMentorService.updateEnterpriseMentorWithUser(
                mentorUpdateDTO.getMentorId(),
                mentorUpdateDTO.getUserId(),
                mentorUpdateDTO.getMentorName(),
                mentorUpdateDTO.getEnterpriseId(),
                mentorUpdateDTO.getPosition(),
                mentorUpdateDTO.getDepartment(),
                mentorUpdateDTO.getPhone(),
                mentorUpdateDTO.getEmail(),
                mentorUpdateDTO.getStatus()
        );
        return Result.success("更新成功", result);
    }
    
    @ApiOperation("停用企业导师（软删除）")
    @DeleteMapping("/{mentorId}")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_ENTERPRISE_ADMIN')")
    public Result<?> deleteEnterpriseMentor(
            @ApiParam(value = "企业导师ID", required = true) @PathVariable Long mentorId) {
        boolean success = enterpriseMentorService.deleteEnterpriseMentor(mentorId);
        return success ? Result.success("停用成功") : Result.error("停用失败");
    }
    
    @ApiOperation("导出企业导师列表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_ENTERPRISE_ADMIN')")
    @GetMapping("/export")
    public void exportEnterpriseMentors(
            @ApiParam(value = "导师姓名", required = false) @RequestParam(required = false) String mentorName,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "状态：1-启用，0-禁用", required = false) @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        List<EnterpriseMentor> mentors = enterpriseMentorService.getAllEnterpriseMentors(mentorName, enterpriseId, status);
        
        // 填充企业名称
        if (mentors != null && !mentors.isEmpty()) {
            List<Long> enterpriseIds = mentors.stream()
                    .map(EnterpriseMentor::getEnterpriseId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!enterpriseIds.isEmpty()) {
                List<Enterprise> enterprises = enterpriseMapper.selectBatchIds(enterpriseIds);
                if (enterprises != null && !enterprises.isEmpty()) {
                    java.util.Map<Long, String> enterpriseNameMap = enterprises.stream()
                            .filter(e -> e != null && e.getEnterpriseId() != null && e.getEnterpriseName() != null)
                            .collect(Collectors.toMap(Enterprise::getEnterpriseId, Enterprise::getEnterpriseName, (v1, v2) -> v1));
                    
                    for (EnterpriseMentor mentor : mentors) {
                        if (mentor.getEnterpriseId() != null) {
                            mentor.setEnterpriseName(enterpriseNameMap.get(mentor.getEnterpriseId()));
                        }
                    }
                }
            }
        }
        
        // 处理数据，转换状态和时间为文字
        for (EnterpriseMentor mentor : mentors) {
            if (mentor.getStatus() != null) {
                mentor.setStatusText(mentor.getStatus() == 1 ? "启用" : "禁用");
            } else {
                mentor.setStatusText("");
            }
            if (mentor.getCreateTime() != null) {
                mentor.setCreateTimeText(mentor.getCreateTime().format(
                    java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            } else {
                mentor.setCreateTimeText("");
            }
        }
        
        // 定义表头和字段名
        String[] headers = {"导师ID", "导师姓名", "所属企业", "职位", "部门", "联系电话", "邮箱", "状态", "创建时间"};
        String[] fieldNames = {"mentorId", "mentorName", "enterpriseName", "position", "department", "phone", "email", "statusText", "createTimeText"};
        
        ExcelUtil.exportToExcel(response, mentors, headers, fieldNames, "企业导师列表");
    }
}

