package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.internship.dto.InternshipPlanQueryDTO;

import java.util.List;
import java.time.LocalDate;

/**
 * 实习计划管理Service接口
 * 提供实习计划的创建、审核、发布等业务功能
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
     * @param queryDTO 查询条件
     * @return 实习计划列表
     */
    Page<InternshipPlan> getPlanPage(Page<InternshipPlan> page, InternshipPlanQueryDTO queryDTO);
    
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
    
    /**
     * 根据学生信息获取可用的实习计划列表
     * @param studentId 学生ID
     * @return 实习计划列表
     */
    List<InternshipPlan> getAvailablePlansForStudent(Long studentId);
    
    /**
     * 根据组织架构和时间范围查询已发布的实习计划
     * @param schoolId 学校ID
     * @param collegeId 学院ID
     * @param majorId 专业ID
     * @param currentDate 当前日期（用于判断计划是否有效）
     * @return 实习计划列表
     */
    List<InternshipPlan> getPublishedPlans(Long schoolId, Long collegeId, Long majorId, LocalDate currentDate);
    
    /**
     * 查询所有实习计划列表（用于导出）
     * @param queryDTO 查询条件
     * @return 实习计划列表
     */
    List<InternshipPlan> getAllPlans(InternshipPlanQueryDTO queryDTO);
}

