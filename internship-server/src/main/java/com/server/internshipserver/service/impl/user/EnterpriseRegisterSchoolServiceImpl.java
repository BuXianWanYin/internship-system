package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.CooperationStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.user.EnterpriseRegisterSchool;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.mapper.user.EnterpriseRegisterSchoolMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.user.EnterpriseRegisterSchoolService;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业注册申请院校关联Service实现类
 */
@Service
public class EnterpriseRegisterSchoolServiceImpl extends ServiceImpl<EnterpriseRegisterSchoolMapper, EnterpriseRegisterSchool> implements EnterpriseRegisterSchoolService {
    
    @Autowired
    private EnterpriseSchoolCooperationService cooperationService;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveEnterpriseRegisterSchools(Long enterpriseId, List<Long> schoolIds) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        if (EntityValidationUtil.isEmpty(schoolIds)) {
            throw new BusinessException("至少选择一个意向合作院校");
        }
        
        // 删除该企业之前的注册申请记录（如果有）
        LambdaQueryWrapper<EnterpriseRegisterSchool> deleteWrapper = QueryWrapperUtil.buildNotDeletedWrapper(EnterpriseRegisterSchool::getDeleteFlag);
        deleteWrapper.eq(EnterpriseRegisterSchool::getEnterpriseId, enterpriseId);
        List<EnterpriseRegisterSchool> existingRecords = this.list(deleteWrapper);
        if (!existingRecords.isEmpty()) {
            existingRecords.forEach(record -> {
                record.setDeleteFlag(DeleteFlag.DELETED.getCode());
            });
            this.updateBatchById(existingRecords);
        }
        
        // 批量保存新的关联记录
        List<EnterpriseRegisterSchool> records = schoolIds.stream()
                .map(schoolId -> {
                    EnterpriseRegisterSchool record = new EnterpriseRegisterSchool();
                    record.setEnterpriseId(enterpriseId);
                    record.setSchoolId(schoolId);
                    record.setAuditStatus(AuditStatus.PENDING.getCode()); // 待审核
                    EntityDefaultValueUtil.setDefaultValues(record);
                    return record;
                })
                .collect(Collectors.toList());
        
        return this.saveBatch(records);
    }
    
    @Override
    public List<EnterpriseRegisterSchool> getByEnterpriseId(Long enterpriseId) {
        if (enterpriseId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<EnterpriseRegisterSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseRegisterSchool::getEnterpriseId, enterpriseId)
               .eq(EnterpriseRegisterSchool::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(EnterpriseRegisterSchool::getCreateTime);
        
        // 数据权限过滤：学校管理员只能看到自己学校的申请
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null) {
                wrapper.eq(EnterpriseRegisterSchool::getSchoolId, currentUserSchoolId);
            } else {
                // 非系统管理员且无法获取学校ID，返回空列表
                return new ArrayList<>();
            }
        }
        
        return this.list(wrapper);
    }
    
    @Override
    public List<EnterpriseRegisterSchool> getPendingBySchoolId(Long schoolId) {
        if (schoolId == null) {
            return new ArrayList<>();
        }
        
        LambdaQueryWrapper<EnterpriseRegisterSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseRegisterSchool::getSchoolId, schoolId)
               .eq(EnterpriseRegisterSchool::getAuditStatus, AuditStatus.PENDING.getCode()) // 待审核
               .eq(EnterpriseRegisterSchool::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(EnterpriseRegisterSchool::getCreateTime);
        return this.list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditEnterpriseRegister(Long id, Integer auditStatus, String auditOpinion, Long auditorId) {
        if (id == null) {
            throw new BusinessException("关联ID不能为空");
        }
        if (auditStatus == null || (!auditStatus.equals(AuditStatus.APPROVED.getCode()) && !auditStatus.equals(AuditStatus.REJECTED.getCode()))) {
            throw new BusinessException("审核状态无效");
        }
        if (auditorId == null) {
            throw new BusinessException("审核人ID不能为空");
        }
        
        EnterpriseRegisterSchool record = this.getById(id);
        EntityValidationUtil.validateEntityExists(record, "记录");
        
        if (!record.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
            throw new BusinessException("该申请已审核，不能重复审核");
        }
        
        // 更新审核信息
        record.setAuditStatus(auditStatus);
        record.setAuditOpinion(auditOpinion);
        record.setAuditorId(auditorId);
        record.setAuditTime(LocalDateTime.now());
        
        boolean updateResult = this.updateById(record);
        
        // 如果审核通过，自动建立合作关系
        if (updateResult && auditStatus.equals(AuditStatus.APPROVED.getCode())) {
            // 检查合作关系是否已存在
            boolean hasCooperation = cooperationService.hasCooperation(record.getEnterpriseId(), record.getSchoolId());
            if (!hasCooperation) {
                // 创建合作关系
                EnterpriseSchoolCooperation cooperation = new EnterpriseSchoolCooperation();
                cooperation.setEnterpriseId(record.getEnterpriseId());
                cooperation.setSchoolId(record.getSchoolId());
                cooperation.setCooperationType("实习基地");
                cooperation.setCooperationStatus(CooperationStatus.IN_PROGRESS.getCode());
                cooperation.setStartTime(LocalDateTime.now());
                cooperationService.addCooperation(cooperation);
            }
            
            // 检查是否有任何一个院校审核通过，如果有则激活企业账号
            Enterprise enterprise = enterpriseMapper.selectById(record.getEnterpriseId());
            if (enterprise != null && enterprise.getUserId() != null) {
                // 查询该企业是否有任何一个院校审核通过
                LambdaQueryWrapper<EnterpriseRegisterSchool> checkWrapper = new LambdaQueryWrapper<>();
                checkWrapper.eq(EnterpriseRegisterSchool::getEnterpriseId, record.getEnterpriseId())
                           .eq(EnterpriseRegisterSchool::getAuditStatus, AuditStatus.APPROVED.getCode()) // 已通过
                           .eq(EnterpriseRegisterSchool::getDeleteFlag, DeleteFlag.NORMAL.getCode());
                long approvedCount = this.count(checkWrapper);
                
                // 如果有任何一个院校审核通过，激活企业账号
                if (approvedCount > 0) {
                    UserInfo user = userMapper.selectById(enterprise.getUserId());
                    if (user != null && user.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode()) && user.getStatus() != null && user.getStatus().equals(UserStatus.DISABLED.getCode())) {
                        user.setStatus(UserStatus.ENABLED.getCode()); // 激活账号
                        userMapper.updateById(user);
                    }
                }
            }
        }
        
        return updateResult;
    }
    
    @Override
    public EnterpriseRegisterSchool getByEnterpriseIdAndSchoolId(Long enterpriseId, Long schoolId) {
        if (enterpriseId == null || schoolId == null) {
            return null;
        }
        
        LambdaQueryWrapper<EnterpriseRegisterSchool> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseRegisterSchool::getEnterpriseId, enterpriseId)
               .eq(EnterpriseRegisterSchool::getSchoolId, schoolId)
               .eq(EnterpriseRegisterSchool::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
}

