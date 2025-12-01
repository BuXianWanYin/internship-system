package com.server.internshipserver.service.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.evaluation.EnterpriseEvaluation;

/**
 * 企业评价管理Service接口
 * 提供企业评价的创建、提交、查询等业务功能
 */
public interface EnterpriseEvaluationService extends IService<EnterpriseEvaluation> {
    
    /**
     * 添加或更新企业评价（支持草稿保存）
     * @param evaluation 评价信息
     * @return 评价信息
     */
    EnterpriseEvaluation saveOrUpdateEvaluation(EnterpriseEvaluation evaluation);
    
    /**
     * 提交企业评价
     * @param evaluationId 评价ID
     * @return 是否成功
     */
    boolean submitEvaluation(Long evaluationId);
    
    /**
     * 根据ID查询评价详情
     * @param evaluationId 评价ID
     * @return 评价信息
     */
    EnterpriseEvaluation getEvaluationById(Long evaluationId);
    
    /**
     * 根据申请ID查询评价
     * @param applyId 申请ID
     * @return 评价信息
     */
    EnterpriseEvaluation getEvaluationByApplyId(Long applyId);
    
    /**
     * 分页查询待评价学生列表
     * @param page 分页参数
     * @param enterpriseId 企业ID
     * @param studentName 学生姓名
     * @param evaluationStatus 评价状态
     * @return 评价列表
     */
    Page<EnterpriseEvaluation> getEvaluationPage(Page<EnterpriseEvaluation> page, Long enterpriseId, 
                                                 String studentName, Integer evaluationStatus);
}

