package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.Interview;
import com.server.internshipserver.domain.internship.dto.ConfirmInterviewDTO;
import com.server.internshipserver.domain.internship.dto.InterviewQueryDTO;
import com.server.internshipserver.domain.internship.dto.SubmitInterviewResultDTO;

/**
 * 面试管理Service接口
 * 提供面试安排、确认、结果提交等业务功能
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
     * @param queryDTO 查询条件
     * @return 面试列表
     */
    Page<Interview> getInterviewPage(Page<Interview> page, InterviewQueryDTO queryDTO);
    
    /**
     * 学生确认面试
     * @param interviewId 面试ID
     * @param confirmDTO 确认信息
     * @return 是否成功
     */
    boolean confirmInterview(Long interviewId, ConfirmInterviewDTO confirmDTO);
    
    /**
     * 提交面试结果
     * @param interviewId 面试ID
     * @param resultDTO 面试结果信息
     * @return 是否成功
     */
    boolean submitInterviewResult(Long interviewId, SubmitInterviewResultDTO resultDTO);
    
    /**
     * 取消面试
     * @param interviewId 面试ID
     * @return 是否成功
     */
    boolean cancelInterview(Long interviewId);
}

