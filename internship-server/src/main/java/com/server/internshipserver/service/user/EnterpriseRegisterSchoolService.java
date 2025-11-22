package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.EnterpriseRegisterSchool;

import java.util.List;

/**
 * 企业注册申请院校关联Service接口
 */
public interface EnterpriseRegisterSchoolService extends IService<EnterpriseRegisterSchool> {
    
    /**
     * 批量保存企业注册申请院校关联
     * @param enterpriseId 企业ID
     * @param schoolIds 学校ID列表
     * @return 是否成功
     */
    boolean saveEnterpriseRegisterSchools(Long enterpriseId, List<Long> schoolIds);
    
    /**
     * 根据企业ID查询注册申请院校列表
     * @param enterpriseId 企业ID
     * @return 注册申请院校列表
     */
    List<EnterpriseRegisterSchool> getByEnterpriseId(Long enterpriseId);
    
    /**
     * 根据学校ID查询待审核的企业注册申请列表
     * @param schoolId 学校ID
     * @return 企业注册申请列表
     */
    List<EnterpriseRegisterSchool> getPendingBySchoolId(Long schoolId);
    
    /**
     * 审核企业注册申请（按院校）
     * @param id 关联ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditOpinion 审核意见
     * @param auditorId 审核人ID
     * @return 是否成功
     */
    boolean auditEnterpriseRegister(Long id, Integer auditStatus, String auditOpinion, Long auditorId);
    
    /**
     * 根据企业ID和学校ID查询关联记录
     * @param enterpriseId 企业ID
     * @param schoolId 学校ID
     * @return 关联记录
     */
    EnterpriseRegisterSchool getByEnterpriseIdAndSchoolId(Long enterpriseId, Long schoolId);
}

