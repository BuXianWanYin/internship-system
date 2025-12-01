package com.server.internshipserver.service.report;

import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 报表管理Service接口
 * 提供各类报表的导出功能
 */
public interface ReportService {
    
    /**
     * 导出实习情况汇总表
     * @param queryDTO 查询条件
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    void exportInternshipSummaryReport(InternshipApplyQueryDTO queryDTO, HttpServletResponse response) throws IOException;
}

