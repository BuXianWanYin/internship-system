package com.server.internshipserver.service.impl.cooperation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.CooperationStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperationApply;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.cooperation.EnterpriseSchoolCooperationApplyMapper;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationApplyService;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 企业学校合作申请Service实现类
 */
@Service
public class EnterpriseSchoolCooperationApplyServiceImpl extends ServiceImpl<EnterpriseSchoolCooperationApplyMapper, EnterpriseSchoolCooperationApply> implements EnterpriseSchoolCooperationApplyService {
    
    @Autowired
    private EnterpriseSchoolCooperationService cooperationService;
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseSchoolCooperationApply applyCooperation(EnterpriseSchoolCooperationApply apply) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(apply.getEnterpriseId(), "企业ID");
        EntityValidationUtil.validateIdNotNull(apply.getSchoolId(), "学校ID");
        
        if (!StringUtils.hasText(apply.getCooperationType())) {
            throw new BusinessException("合作类型不能为空");
        }
        if (apply.getStartTime() == null) {
            throw new BusinessException("合作开始时间不能为空");
        }
        if (apply.getEndTime() == null) {
            throw new BusinessException("合作结束时间不能为空");
        }
        if (!StringUtils.hasText(apply.getCooperationDesc())) {
            throw new BusinessException("合作描述不能为空");
        }
        
        // 检查是否已存在合作关系
        if (cooperationService.hasCooperation(apply.getEnterpriseId(), apply.getSchoolId())) {
            throw new BusinessException("该企业已与该学校建立合作关系");
        }
        
        // 检查是否已有待审核或已通过的申请
        if (hasApplied(apply.getEnterpriseId(), apply.getSchoolId())) {
            throw new BusinessException("该企业已向该学校提交过合作申请，请勿重复申请");
        }
        
        // 设置申请状态为待审核
        apply.setApplyStatus(AuditStatus.PENDING.getCode());
        EntityDefaultValueUtil.setDefaultValues(apply);
        
        // 保存申请记录
        this.save(apply);
        
        return apply;
    }
    
    @Override
    public List<EnterpriseSchoolCooperationApply> getPendingApplyListBySchoolId(Long schoolId) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<EnterpriseSchoolCooperationApply> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(EnterpriseSchoolCooperationApply::getDeleteFlag);
        wrapper.eq(EnterpriseSchoolCooperationApply::getSchoolId, schoolId)
               .eq(EnterpriseSchoolCooperationApply::getApplyStatus, AuditStatus.PENDING.getCode())
               .orderByDesc(EnterpriseSchoolCooperationApply::getCreateTime);
        
        List<EnterpriseSchoolCooperationApply> applies = this.list(wrapper);
        
        // 填充企业名称
        for (EnterpriseSchoolCooperationApply apply : applies) {
            if (apply.getEnterpriseId() != null) {
                Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                if (enterprise != null) {
                    apply.setEnterpriseName(enterprise.getEnterpriseName());
                }
            }
        }
        
        return applies;
    }
    
    @Override
    public List<EnterpriseSchoolCooperationApply> getApplyListByEnterpriseId(Long enterpriseId) {
        if (enterpriseId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<EnterpriseSchoolCooperationApply> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(EnterpriseSchoolCooperationApply::getDeleteFlag);
        wrapper.eq(EnterpriseSchoolCooperationApply::getEnterpriseId, enterpriseId)
               // 只返回待审核和已拒绝的申请，已通过的申请不应该显示在这里（因为已经建立了合作关系）
               .in(EnterpriseSchoolCooperationApply::getApplyStatus, 
                   AuditStatus.PENDING.getCode(), AuditStatus.REJECTED.getCode())
               .orderByDesc(EnterpriseSchoolCooperationApply::getCreateTime);
        
        List<EnterpriseSchoolCooperationApply> applies = this.list(wrapper);
        
        // 填充学校名称
        for (EnterpriseSchoolCooperationApply apply : applies) {
            if (apply.getSchoolId() != null) {
                School school = schoolMapper.selectById(apply.getSchoolId());
                if (school != null) {
                    apply.setSchoolName(school.getSchoolName());
                }
            }
        }
        
        return applies;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditCooperationApply(Long id, Integer auditStatus, String auditOpinion, Long auditorId) {
        EntityValidationUtil.validateIdNotNull(id, "申请ID");
        EntityValidationUtil.validateIdNotNull(auditorId, "审核人ID");
        
        if (auditStatus == null || (!auditStatus.equals(AuditStatus.APPROVED.getCode()) && !auditStatus.equals(AuditStatus.REJECTED.getCode()))) {
            throw new BusinessException("审核状态无效");
        }
        
        EnterpriseSchoolCooperationApply apply = this.getById(id);
        EntityValidationUtil.validateEntityExists(apply, "合作申请");
        
        if (!apply.getApplyStatus().equals(AuditStatus.PENDING.getCode())) {
            throw new BusinessException("该申请已审核，不能重复审核");
        }
        
        // 更新审核信息
        apply.setApplyStatus(auditStatus);
        apply.setAuditOpinion(auditOpinion);
        apply.setAuditorId(auditorId);
        apply.setAuditTime(LocalDateTime.now());
        
        boolean updateResult = this.updateById(apply);
        
        // 如果审核通过，创建合作关系
        if (updateResult && auditStatus.equals(AuditStatus.APPROVED.getCode())) {
            // 检查合作关系是否已存在
            if (!cooperationService.hasCooperation(apply.getEnterpriseId(), apply.getSchoolId())) {
                // 创建合作关系
                EnterpriseSchoolCooperation cooperation = new EnterpriseSchoolCooperation();
                cooperation.setEnterpriseId(apply.getEnterpriseId());
                cooperation.setSchoolId(apply.getSchoolId());
                cooperation.setCooperationType(apply.getCooperationType());
                cooperation.setCooperationStatus(CooperationStatus.IN_PROGRESS.getCode());
                cooperation.setStartTime(apply.getStartTime());
                cooperation.setEndTime(apply.getEndTime());
                cooperation.setCooperationDesc(apply.getCooperationDesc());
                cooperationService.addCooperation(cooperation);
            }
        }
        
        return updateResult;
    }
    
    @Override
    public boolean hasApplied(Long enterpriseId, Long schoolId) {
        if (enterpriseId == null || schoolId == null) {
            return false;
        }
        
        LambdaQueryWrapper<EnterpriseSchoolCooperationApply> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(EnterpriseSchoolCooperationApply::getDeleteFlag);
        wrapper.eq(EnterpriseSchoolCooperationApply::getEnterpriseId, enterpriseId)
               .eq(EnterpriseSchoolCooperationApply::getSchoolId, schoolId)
               .in(EnterpriseSchoolCooperationApply::getApplyStatus, 
                   AuditStatus.PENDING.getCode(), AuditStatus.APPROVED.getCode());
        
        return this.count(wrapper) > 0;
    }
}

