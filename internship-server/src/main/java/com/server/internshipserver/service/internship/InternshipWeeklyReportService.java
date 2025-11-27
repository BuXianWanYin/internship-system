package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.internship.dto.InternshipWeeklyReportQueryDTO;

/**
 * 周报管理Service接口
 */
public interface InternshipWeeklyReportService extends IService<InternshipWeeklyReport> {
    
    /**
     * 提交周报
     * @param report 周报信息
     * @return 添加的周报信息
     */
    InternshipWeeklyReport addReport(InternshipWeeklyReport report);
    
    /**
     * 更新周报
     * @param report 周报信息
     * @return 更新后的周报信息
     */
    InternshipWeeklyReport updateReport(InternshipWeeklyReport report);
    
    /**
     * 根据ID查询周报详情
     * @param reportId 周报ID
     * @return 周报信息
     */
    InternshipWeeklyReport getReportById(Long reportId);
    
    /**
     * 分页查询周报列表
     * @param page 分页参数
     * @param queryDTO 查询条件
     * @return 周报列表
     */
    Page<InternshipWeeklyReport> getReportPage(Page<InternshipWeeklyReport> page, InternshipWeeklyReportQueryDTO queryDTO);
    
    /**
     * 批阅周报
     * @param reportId 周报ID
     * @param reviewComment 批阅意见
     * @param reviewScore 批阅评分（0-100）
     * @return 是否成功
     */
    boolean reviewReport(Long reportId, String reviewComment, java.math.BigDecimal reviewScore);
    
    /**
     * 删除周报（软删除）
     * @param reportId 周报ID
     * @return 是否成功
     */
    boolean deleteReport(Long reportId);
}

