package com.server.internshipserver.controller.report;

import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;
import com.server.internshipserver.service.report.ReportService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 报表管理控制器
 */
@Api(tags = "报表管理")
@RestController
@RequestMapping("/report")
public class ReportController {
    
    @Autowired
    private ReportService reportService;
    
    @ApiOperation("导出实习情况汇总表")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/internship-summary/export")
    public void exportInternshipSummaryReport(
            @ApiParam(value = "学生ID") @RequestParam(required = false) Long studentId,
            @ApiParam(value = "企业ID") @RequestParam(required = false) Long enterpriseId,
            @ApiParam(value = "岗位ID") @RequestParam(required = false) Long postId,
            @ApiParam(value = "申请类型") @RequestParam(required = false) Integer applyType,
            @ApiParam(value = "状态") @RequestParam(required = false) Integer status,
            HttpServletResponse response) throws IOException {
        InternshipApplyQueryDTO queryDTO = new InternshipApplyQueryDTO();
        queryDTO.setStudentId(studentId);
        queryDTO.setEnterpriseId(enterpriseId);
        queryDTO.setPostId(postId);
        queryDTO.setApplyType(applyType);
        queryDTO.setStatus(status);
        
        reportService.exportInternshipSummaryReport(queryDTO, response);
    }
}

