package com.server.internshipserver.service.evaluation;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;

/**
 * 综合成绩管理Service接口
 */
public interface ComprehensiveScoreService extends IService<ComprehensiveScore> {
    
    /**
     * 计算综合成绩
     * @param applyId 申请ID
     * @return 综合成绩信息
     */
    ComprehensiveScore calculateComprehensiveScore(Long applyId);
    
    /**
     * 根据ID查询综合成绩详情
     * @param scoreId 成绩ID
     * @return 综合成绩信息
     */
    ComprehensiveScore getScoreById(Long scoreId);
    
    /**
     * 根据申请ID查询综合成绩
     * @param applyId 申请ID
     * @return 综合成绩信息
     */
    ComprehensiveScore getScoreByApplyId(Long applyId);
    
    /**
     * 分页查询综合成绩列表
     * @param page 分页参数
     * @param studentName 学生姓名
     * @return 综合成绩列表
     */
    Page<ComprehensiveScore> getScorePage(Page<ComprehensiveScore> page, String studentName);
    
    /**
     * 检查三个评价是否都完成
     * @param applyId 申请ID
     * @return true-都完成，false-未完成
     */
    boolean checkAllEvaluationsCompleted(Long applyId);
}

