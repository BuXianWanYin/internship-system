package com.server.internshipserver.service.cooperation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.system.School;

import java.util.List;

/**
 * 企业学校合作关系Service接口
 * 提供企业学校合作关系的创建、查询、验证等业务功能
 */
public interface EnterpriseSchoolCooperationService extends IService<EnterpriseSchoolCooperation> {
    
    /**
     * 添加合作关系
     * @param cooperation 合作关系信息
     * @return 添加的合作关系信息
     */
    EnterpriseSchoolCooperation addCooperation(EnterpriseSchoolCooperation cooperation);
    
    /**
     * 更新合作关系信息
     * @param cooperation 合作关系信息
     * @return 更新后的合作关系信息
     */
    EnterpriseSchoolCooperation updateCooperation(EnterpriseSchoolCooperation cooperation);
    
    /**
     * 删除合作关系（软删除）
     * @param id 合作关系ID
     * @return 是否成功
     */
    boolean deleteCooperation(Long id);
    
    /**
     * 根据企业ID查询合作学校列表
     * @param enterpriseId 企业ID
     * @return 合作学校列表
     */
    List<School> getCooperationSchoolsByEnterpriseId(Long enterpriseId);
    
    /**
     * 根据学校ID查询合作企业ID列表
     * @param schoolId 学校ID
     * @return 合作企业ID列表
     */
    List<Long> getCooperationEnterpriseIdsBySchoolId(Long schoolId);
    
    /**
     * 根据企业ID查询合作学校ID列表
     * @param enterpriseId 企业ID
     * @return 合作学校ID列表
     */
    List<Long> getCooperationSchoolIdsByEnterpriseId(Long enterpriseId);
    
    /**
     * 检查企业和学校是否有合作关系
     * @param enterpriseId 企业ID
     * @param schoolId 学校ID
     * @return 是否有合作关系
     */
    boolean hasCooperation(Long enterpriseId, Long schoolId);
    
    /**
     * 检查企业和学校是否有指定类型的合作关系
     * @param enterpriseId 企业ID
     * @param schoolId 学校ID
     * @param cooperationType 合作类型
     * @return 是否有该类型的合作关系
     */
    boolean hasCooperationByType(Long enterpriseId, Long schoolId, String cooperationType);
    
    /**
     * 根据企业ID查询合作关系列表（包含完整信息）
     * @param enterpriseId 企业ID
     * @return 合作关系列表
     */
    List<EnterpriseSchoolCooperation> getCooperationListByEnterpriseId(Long enterpriseId);
}

