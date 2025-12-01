package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipLog;

/**
 * 实习日志管理Service接口
 * 提供实习日志的提交、批阅、查询等业务功能
 */
public interface InternshipLogService extends IService<InternshipLog> {
    
    /**
     * 提交实习日志
     * @param log 日志信息
     * @return 添加的日志信息
     */
    InternshipLog addLog(InternshipLog log);
    
    /**
     * 更新实习日志
     * @param log 日志信息
     * @return 更新后的日志信息
     */
    InternshipLog updateLog(InternshipLog log);
    
    /**
     * 根据ID查询日志详情
     * @param logId 日志ID
     * @return 日志信息
     */
    InternshipLog getLogById(Long logId);
    
    /**
     * 分页查询日志列表
     * @param page 分页参数
     * @param studentId 学生ID
     * @param applyId 申请ID
     * @param logDate 日志日期
     * @param reviewStatus 批阅状态
     * @return 日志列表
     */
    Page<InternshipLog> getLogPage(Page<InternshipLog> page, Long studentId, Long applyId, 
                                   java.time.LocalDate logDate, Integer reviewStatus);
    
    /**
     * 批阅日志
     * @param logId 日志ID
     * @param reviewComment 批阅意见
     * @param reviewScore 批阅评分（0-100）
     * @return 是否成功
     */
    boolean reviewLog(Long logId, String reviewComment, java.math.BigDecimal reviewScore);
    
    /**
     * 删除日志（软删除）
     * @param logId 日志ID
     * @return 是否成功
     */
    boolean deleteLog(Long logId);
}

