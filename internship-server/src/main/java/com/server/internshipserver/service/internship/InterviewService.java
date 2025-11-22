package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.Interview;

/**
 * 面试管理Service接口
 */
public interface InterviewService extends IService<Interview> {
    
    /**
     * 安排面试
     * @param interview 面试信息
     * @return 添加的面试信息
     */
    Interview addInterview(Interview interview);
    
    /**
     * 更新面试信息
     * @param interview 面试信息
     * @return 更新后的面试信息
     */
    Interview updateInterview(Interview interview);
    
    /**
     * 根据ID查询面试详情
     * @param interviewId 面试ID
     * @return 面试信息
     */
    Interview getInterviewById(Long interviewId);
    
    /**
     * 分页查询面试列表
     * @param page 分页参数
     * @param applyId 申请ID（可选）
     * @param enterpriseId 企业ID（可选）
     * @param studentId 学生ID（可选）
     * @param status 状态（可选）
     * @return 面试列表
     */
    Page<Interview> getInterviewPage(Page<Interview> page, Long applyId, Long enterpriseId,
                                     Long studentId, Integer status);
    
    /**
     * 学生确认面试
     * @param interviewId 面试ID
     * @param confirm 确认（1-已确认，2-已拒绝）
     * @return 是否成功
     */
    boolean confirmInterview(Long interviewId, Integer confirm);
    
    /**
     * 提交面试结果
     * @param interviewId 面试ID
     * @param interviewResult 面试结果（1-通过，2-不通过，3-待定）
     * @param interviewComment 面试评价
     * @return 是否成功
     */
    boolean submitInterviewResult(Long interviewId, Integer interviewResult, String interviewComment);
    
    /**
     * 取消面试
     * @param interviewId 面试ID
     * @return 是否成功
     */
    boolean cancelInterview(Long interviewId);
}

