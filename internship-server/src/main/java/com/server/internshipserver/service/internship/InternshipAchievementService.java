package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipAchievement;

/**
 * 阶段性成果管理Service接口
 */
public interface InternshipAchievementService extends IService<InternshipAchievement> {
    
    /**
     * 提交阶段性成果
     * @param achievement 成果信息
     * @return 添加的成果信息
     */
    InternshipAchievement addAchievement(InternshipAchievement achievement);
    
    /**
     * 更新阶段性成果
     * @param achievement 成果信息
     * @return 更新后的成果信息
     */
    InternshipAchievement updateAchievement(InternshipAchievement achievement);
    
    /**
     * 根据ID查询成果详情
     * @param achievementId 成果ID
     * @return 成果信息
     */
    InternshipAchievement getAchievementById(Long achievementId);
    
    /**
     * 分页查询成果列表
     * @param page 分页参数
     * @param studentId 学生ID
     * @param applyId 申请ID
     * @param achievementType 成果类型
     * @param reviewStatus 审核状态
     * @return 成果列表
     */
    Page<InternshipAchievement> getAchievementPage(Page<InternshipAchievement> page, Long studentId, Long applyId,
                                                   String achievementType, Integer reviewStatus);
    
    /**
     * 审核成果
     * @param achievementId 成果ID
     * @param reviewStatus 审核状态（1-已通过，2-已拒绝）
     * @param reviewComment 审核意见
     * @return 是否成功
     */
    boolean reviewAchievement(Long achievementId, Integer reviewStatus, String reviewComment);
    
    /**
     * 删除成果（软删除）
     * @param achievementId 成果ID
     * @return 是否成功
     */
    boolean deleteAchievement(Long achievementId);
}

