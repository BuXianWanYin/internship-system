package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipPlan;

/**
 * 实习计划管理Service接口
 */
public interface InternshipPlanService extends IService<InternshipPlan> {
    
    /**
     * 添加实习计划
     * @param plan 实习计划信息
     * @return 添加的实习计划信息
     */
    InternshipPlan addPlan(InternshipPlan plan);
    
    /**
     * 更新实习计划信息
     * @param plan 实习计划信息
     * @return 更新后的实习计划信息
     */
    InternshipPlan updatePlan(InternshipPlan plan);
    
    /**
     * 根据ID查询实习计划详情
     * @param planId 计划ID
     * @return 实习计划信息
     */
    InternshipPlan getPlanById(Long planId);
    
    /**
     * 分页查询实习计划列表
     * @param page 分页参数
     * @param planName 计划名称（可选）
     * @param semesterId 学期ID（可选）
     * @param schoolId 学校ID（可选）
     * @param collegeId 学院ID（可选）
     * @param majorId 专业ID（可选）
     * @param status 状态（可选）
     * @return 实习计划列表
     */
    Page<InternshipPlan> getPlanPage(Page<InternshipPlan> page, String planName, Long semesterId, 
                                     Long schoolId, Long collegeId, Long majorId, Integer status);
    
    /**
     * 提交审核
     * @param planId 计划ID
     * @return 是否成功
     */
    boolean submitPlan(Long planId);
    
    /**
     * 审核实习计划
     * @param planId 计划ID
     * @param auditStatus 审核状态（2-已通过，3-已拒绝）
     * @param auditOpinion 审核意见
     * @return 是否成功
     */
    boolean auditPlan(Long planId, Integer auditStatus, String auditOpinion);
    
    /**
     * 发布实习计划
     * @param planId 计划ID
     * @return 是否成功
     */
    boolean publishPlan(Long planId);
    
    /**
     * 删除实习计划（软删除）
     * @param planId 计划ID
     * @return 是否成功
     */
    boolean deletePlan(Long planId);
}

