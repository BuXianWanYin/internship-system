package com.server.internshipserver.service.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.evaluation.SelfEvaluation;

/**
 * 学生自评管理Service接口
 */
public interface SelfEvaluationService extends IService<SelfEvaluation> {
    
    /**
     * 添加或更新学生自评（支持草稿保存）
     * @param evaluation 自评信息
     * @return 自评信息
     */
    SelfEvaluation saveOrUpdateEvaluation(SelfEvaluation evaluation);
    
    /**
     * 提交学生自评
     * @param evaluationId 评价ID
     * @return 是否成功
     */
    boolean submitEvaluation(Long evaluationId);
    
    /**
     * 根据ID查询自评详情
     * @param evaluationId 评价ID
     * @return 自评信息
     */
    SelfEvaluation getEvaluationById(Long evaluationId);
    
    /**
     * 根据申请ID查询自评
     * @param applyId 申请ID
     * @return 自评信息
     */
    SelfEvaluation getEvaluationByApplyId(Long applyId);
    
    /**
     * 分页查询自评列表
     * @param page 分页参数
     * @param studentId 学生ID（可选）
     * @param evaluationStatus 评价状态（可选）
     * @return 自评列表
     */
    Page<SelfEvaluation> getEvaluationPage(Page<SelfEvaluation> page, Long studentId, Integer evaluationStatus);
}

