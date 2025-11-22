package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipFeedback;

/**
 * 问题反馈管理Service接口
 */
public interface InternshipFeedbackService extends IService<InternshipFeedback> {
    
    /**
     * 提交问题反馈
     * @param feedback 反馈信息
     * @return 添加的反馈信息
     */
    InternshipFeedback addFeedback(InternshipFeedback feedback);
    
    /**
     * 更新问题反馈
     * @param feedback 反馈信息
     * @return 更新后的反馈信息
     */
    InternshipFeedback updateFeedback(InternshipFeedback feedback);
    
    /**
     * 根据ID查询反馈详情
     * @param feedbackId 反馈ID
     * @return 反馈信息
     */
    InternshipFeedback getFeedbackById(Long feedbackId);
    
    /**
     * 分页查询反馈列表
     * @param page 分页参数
     * @param studentId 学生ID（可选）
     * @param applyId 申请ID（可选）
     * @param feedbackType 反馈类型（可选）
     * @param feedbackStatus 反馈状态（可选）
     * @return 反馈列表
     */
    Page<InternshipFeedback> getFeedbackPage(Page<InternshipFeedback> page, Long studentId, Long applyId,
                                             Integer feedbackType, Integer feedbackStatus);
    
    /**
     * 回复问题反馈
     * @param feedbackId 反馈ID
     * @param replyContent 回复内容
     * @param replyUserType 回复人类型（1-指导教师，2-企业导师）
     * @return 是否成功
     */
    boolean replyFeedback(Long feedbackId, String replyContent, Integer replyUserType);
    
    /**
     * 标记问题已解决
     * @param feedbackId 反馈ID
     * @return 是否成功
     */
    boolean solveFeedback(Long feedbackId);
    
    /**
     * 关闭问题反馈
     * @param feedbackId 反馈ID
     * @return 是否成功
     */
    boolean closeFeedback(Long feedbackId);
    
    /**
     * 删除反馈（软删除）
     * @param feedbackId 反馈ID
     * @return 是否成功
     */
    boolean deleteFeedback(Long feedbackId);
}

