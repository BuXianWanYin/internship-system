package com.server.internshipserver.service.report;

import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * 报表管理Service接口
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

