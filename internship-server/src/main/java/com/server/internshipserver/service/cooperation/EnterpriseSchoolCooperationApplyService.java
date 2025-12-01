package com.server.internshipserver.service.cooperation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperationApply;

import java.util.List;

/**
 * 企业学校合作申请Service接口
 * 提供企业学校合作申请的提交、审核等业务功能
 */
public interface EnterpriseSchoolCooperationApplyService extends IService<EnterpriseSchoolCooperationApply> {
    
    /**
     * 企业申请合作
     * @param apply 合作申请信息
     * @return 申请记录
     */
    EnterpriseSchoolCooperationApply applyCooperation(EnterpriseSchoolCooperationApply apply);
    
    /**
     * 根据学校ID查询待审核的合作申请列表
     * @param schoolId 学校ID
     * @return 待审核申请列表
     */
    List<EnterpriseSchoolCooperationApply> getPendingApplyListBySchoolId(Long schoolId);
    
    /**
     * 根据企业ID查询合作申请列表（包括所有状态）
     * @param enterpriseId 企业ID
     * @return 合作申请列表
     */
    List<EnterpriseSchoolCooperationApply> getApplyListByEnterpriseId(Long enterpriseId);
    
    /**
     * 审核合作申请
     * @param id 申请ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditOpinion 审核意见
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean auditCooperationApply(Long id, Integer auditStatus, String auditOpinion, Long auditorId);
    
    /**
     * 检查企业是否已申请与某学校合作（待审核或已通过）
     * @param enterpriseId 企业ID
     * @param schoolId 学校ID
     * @return 是否已申请
     */
    boolean hasApplied(Long enterpriseId, Long schoolId);
}

