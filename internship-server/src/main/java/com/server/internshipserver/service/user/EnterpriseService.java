package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.system.School;

import java.util.List;

/**
 * 企业管理Service接口
 */
public interface EnterpriseService extends IService<Enterprise> {
    
    /**
     * 根据用户ID查询企业信息
     * @param userId 用户ID
     * @return 企业信息
     */
    Enterprise getEnterpriseByUserId(Long userId);
    
    /**
     * 根据企业代码查询企业信息
     * @param enterpriseCode 企业代码
     * @return 企业信息
     */
    Enterprise getEnterpriseByEnterpriseCode(String enterpriseCode);
    
    /**
     * 企业注册
     * @param enterprise 企业信息
     * @return 注册的企业信息
     */
    Enterprise registerEnterprise(Enterprise enterprise);
    
    /**
     * 添加企业
     * @param enterprise 企业信息
     * @return 添加的企业信息
     */
    Enterprise addEnterprise(Enterprise enterprise);
    
    /**
     * 更新企业信息
     * @param enterprise 企业信息
     * @return 更新后的企业信息
     */
    Enterprise updateEnterprise(Enterprise enterprise);
    
    /**
     * 审核企业（自动获取当前登录用户作为审核人）
     * @param enterpriseId 企业ID
     * @param auditStatus 审核状态：1-通过，2-拒绝
     * @param auditOpinion 审核意见
     * @return 是否成功
     */
    boolean auditEnterprise(Long enterpriseId, Integer auditStatus, String auditOpinion);
    
    /**
     * 根据ID查询企业详情
     * @param enterpriseId 企业ID
     * @return 企业信息
     */
    Enterprise getEnterpriseById(Long enterpriseId);
    
    /**
     * 分页查询企业列表
     * @param page 分页参数
     * @param enterpriseName 企业名称（可选）
     * @param enterpriseCode 企业代码（可选）
     * @param auditStatus 审核状态（可选）
     * @return 企业列表
     */
    Page<Enterprise> getEnterprisePage(Page<Enterprise> page, String enterpriseName, 
                                         String enterpriseCode, Integer auditStatus);
    
    /**
     * 停用企业（软删除）
     * @param enterpriseId 企业ID
     * @return 是否成功
     */
    boolean deleteEnterprise(Long enterpriseId);
    
    /**
     * 根据企业ID查询合作学校列表
     * @param enterpriseId 企业ID
     * @return 合作学校列表
     */
    List<School> getCooperationSchoolsByEnterpriseId(Long enterpriseId);
}

