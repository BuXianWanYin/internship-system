package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipApply;

/**
 * 实习申请管理Service接口
 */
public interface InternshipApplyService extends IService<InternshipApply> {
    
    /**
     * 提交实习申请（选择合作企业）
     * @param apply 申请信息
     * @return 添加的申请信息
     */
    InternshipApply addCooperationApply(InternshipApply apply);
    
    /**
     * 提交实习申请（自主实习）
     * @param apply 申请信息
     * @return 添加的申请信息
     */
    InternshipApply addSelfApply(InternshipApply apply);
    
    /**
     * 更新实习申请信息
     * @param apply 申请信息
     * @return 更新后的申请信息
     */
    InternshipApply updateApply(InternshipApply apply);
    
    /**
     * 根据ID查询申请详情
     * @param applyId 申请ID
     * @return 申请信息
     */
    InternshipApply getApplyById(Long applyId);
    
    /**
     * 分页查询申请列表
     * @param page 分页参数
     * @param studentId 学生ID（可选）
     * @param enterpriseId 企业ID（可选）
     * @param postId 岗位ID（可选）
     * @param applyType 申请类型（可选）
     * @param status 状态（可选）
     * @return 申请列表
     */
    Page<InternshipApply> getApplyPage(Page<InternshipApply> page, Long studentId, Long enterpriseId,
                                       Long postId, Integer applyType, Integer status);
    
    /**
     * 审核实习申请（自主实习）
     * @param applyId 申请ID
     * @param auditStatus 审核状态（1-已通过，2-已拒绝）
     * @param auditOpinion 审核意见
     * @return 是否成功
     */
    boolean auditApply(Long applyId, Integer auditStatus, String auditOpinion);
    
    /**
     * 企业筛选操作
     * @param applyId 申请ID
     * @param action 操作类型（1-标记感兴趣，2-安排面试，3-录用，4-拒绝）
     * @param comment 备注
     * @return 是否成功
     */
    boolean filterApply(Long applyId, Integer action, String comment);
    
    /**
     * 取消申请
     * @param applyId 申请ID
     * @return 是否成功
     */
    boolean cancelApply(Long applyId);
    
    /**
     * 删除申请（软删除）
     * @param applyId 申请ID
     * @return 是否成功
     */
    boolean deleteApply(Long applyId);
}

