package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.AuditApplyDTO;
import com.server.internshipserver.domain.internship.dto.FilterApplyDTO;
import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;

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
     * @param queryDTO 查询条件
     * @return 申请列表
     */
    Page<InternshipApply> getApplyPage(Page<InternshipApply> page, InternshipApplyQueryDTO queryDTO);
    
    /**
     * 审核实习申请（自主实习）
     * @param applyId 申请ID
     * @param auditDTO 审核信息
     * @return 是否成功
     */
    boolean auditApply(Long applyId, AuditApplyDTO auditDTO);
    
    /**
     * 企业筛选操作
     * @param applyId 申请ID
     * @param filterDTO 筛选信息
     * @return 是否成功
     */
    boolean filterApply(Long applyId, FilterApplyDTO filterDTO);
    
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
    
    /**
     * 查询企业实习学生列表
     * @param page 分页参数
     * @param studentName 学生姓名（可选）
     * @param studentNo 学号（可选）
     * @param postId 岗位ID（可选）
     * @param status 申请状态（可选，不传则默认查询已录用的学生）
     * @return 申请列表
     */
    Page<InternshipApply> getEnterpriseStudents(Page<InternshipApply> page, String studentName, String studentNo, Long postId, Integer status);
    
    /**
     * 查询企业导师指导的学生列表
     * @param page 分页参数
     * @param studentName 学生姓名（可选）
     * @param studentNo 学号（可选）
     * @param status 申请状态（可选）
     * @return 申请列表
     */
    Page<InternshipApply> getMentorStudents(Page<InternshipApply> page, String studentName, String studentNo, Integer status);
    
    /**
     * 给学生分配企业导师
     * @param applyId 申请ID
     * @param mentorId 企业导师ID
     * @return 是否成功
     */
    boolean assignMentor(Long applyId, Long mentorId);
    
    /**
     * 学生确认上岗
     * @param applyId 申请ID
     * @return 是否成功
     */
    boolean confirmOnboard(Long applyId);
    
    /**
     * 解绑企业（班主任/管理员）
     * @param applyId 申请ID
     * @param reason 解绑原因（可选）
     * @param remark 备注（可选）
     * @return 是否成功
     */
    boolean unbindInternship(Long applyId, String reason, String remark);
    
    /**
     * 获取当前学生的实习申请（已确认上岗的）
     * @return 申请信息，如果没有则返回null
     */
    InternshipApply getCurrentInternship();
}

