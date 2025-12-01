package com.server.internshipserver.service.impl.cooperation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.CooperationStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.mapper.cooperation.EnterpriseSchoolCooperationMapper;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 企业学校合作关系Service实现类
 */
@Service
public class EnterpriseSchoolCooperationServiceImpl extends ServiceImpl<EnterpriseSchoolCooperationMapper, EnterpriseSchoolCooperation> implements EnterpriseSchoolCooperationService {
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseSchoolCooperation addCooperation(EnterpriseSchoolCooperation cooperation) {
        // 参数校验
        if (cooperation.getEnterpriseId() == null) {
            throw new BusinessException("企业ID不能为空");
        }
        if (cooperation.getSchoolId() == null) {
            throw new BusinessException("学校ID不能为空");
        }
        
        // 数据权限检查：学校管理员只能添加自己学校的合作关系
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId == null || !currentUserSchoolId.equals(cooperation.getSchoolId())) {
                throw new BusinessException("无权为该学校添加合作关系");
            }
        }
        
        // 检查相同合作类型的合作关系是否已存在（允许不同合作类型）
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, cooperation.getEnterpriseId())
               .eq(EnterpriseSchoolCooperation::getSchoolId, cooperation.getSchoolId())
               .eq(EnterpriseSchoolCooperation::getCooperationType, cooperation.getCooperationType())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        EnterpriseSchoolCooperation existCooperation = this.getOne(wrapper);
        if (existCooperation != null) {
            throw new BusinessException("该合作类型的合作关系已存在");
        }
        
        // 设置默认值
        if (cooperation.getCooperationStatus() == null) {
            cooperation.setCooperationStatus(CooperationStatus.IN_PROGRESS.getCode());
        }
        EntityDefaultValueUtil.setDefaultValues(cooperation);
        
        // 保存
        this.save(cooperation);
        return cooperation;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseSchoolCooperation updateCooperation(EnterpriseSchoolCooperation cooperation) {
        if (cooperation.getId() == null) {
            throw new BusinessException("合作关系ID不能为空");
        }
        
        // 检查合作关系是否存在
        EnterpriseSchoolCooperation existCooperation = this.getById(cooperation.getId());
        EntityValidationUtil.validateEntityExists(existCooperation, "合作关系");
        
        // 数据权限检查：学校管理员只能编辑自己学校的合作关系
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId == null || !currentUserSchoolId.equals(existCooperation.getSchoolId())) {
                throw new BusinessException("无权编辑该合作关系");
            }
            // 如果修改了学校ID，检查新学校ID是否属于当前用户
            Long newSchoolId = cooperation.getSchoolId() != null ? cooperation.getSchoolId() : existCooperation.getSchoolId();
            if (!newSchoolId.equals(currentUserSchoolId)) {
                throw new BusinessException("无权将该合作关系转移到其他学校");
            }
        }
        
        // 如果修改了企业ID、学校ID或合作类型，检查新关系是否已存在
        Long newEnterpriseId = cooperation.getEnterpriseId() != null ? cooperation.getEnterpriseId() : existCooperation.getEnterpriseId();
        Long newSchoolId = cooperation.getSchoolId() != null ? cooperation.getSchoolId() : existCooperation.getSchoolId();
        String newCooperationType = cooperation.getCooperationType() != null ? cooperation.getCooperationType() : existCooperation.getCooperationType();
        
        if ((cooperation.getEnterpriseId() != null && !cooperation.getEnterpriseId().equals(existCooperation.getEnterpriseId()))
                || (cooperation.getSchoolId() != null && !cooperation.getSchoolId().equals(existCooperation.getSchoolId()))
                || (cooperation.getCooperationType() != null && !cooperation.getCooperationType().equals(existCooperation.getCooperationType()))) {
            LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, newEnterpriseId)
                   .eq(EnterpriseSchoolCooperation::getSchoolId, newSchoolId)
                   .eq(EnterpriseSchoolCooperation::getCooperationType, newCooperationType)
                   .ne(EnterpriseSchoolCooperation::getId, cooperation.getId())
                   .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            EnterpriseSchoolCooperation duplicateCooperation = this.getOne(wrapper);
            if (duplicateCooperation != null) {
                throw new BusinessException("该合作类型的合作关系已存在");
            }
        }
        
        // 更新
        this.updateById(cooperation);
        return this.getById(cooperation.getId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCooperation(Long id) {
        if (id == null) {
            throw new BusinessException("合作关系ID不能为空");
        }
        
        EnterpriseSchoolCooperation cooperation = this.getById(id);
        EntityValidationUtil.validateEntityExists(cooperation, "合作关系");
        
        // 数据权限检查：学校管理员只能删除自己学校的合作关系
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId == null || !currentUserSchoolId.equals(cooperation.getSchoolId())) {
                throw new BusinessException("无权删除该合作关系");
            }
        }
        
        // 软删除
        cooperation.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(cooperation);
    }
    
    @Override
    public List<School> getCooperationSchoolsByEnterpriseId(Long enterpriseId) {
        if (enterpriseId == null) {
            return Collections.emptyList();
        }
        
        // 查询合作关系
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, enterpriseId)
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤：学校管理员只能看到自己学校的合作关系
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null) {
                wrapper.eq(EnterpriseSchoolCooperation::getSchoolId, currentUserSchoolId);
            }
        }
        
        List<EnterpriseSchoolCooperation> cooperations = this.list(wrapper);
        
        if (cooperations.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取学校ID列表
        List<Long> schoolIds = cooperations.stream()
                .map(EnterpriseSchoolCooperation::getSchoolId)
                .distinct()
                .collect(Collectors.toList());
        
        // 查询学校信息
        LambdaQueryWrapper<School> schoolWrapper = new LambdaQueryWrapper<>();
        schoolWrapper.in(School::getSchoolId, schoolIds)
                    .eq(School::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return schoolMapper.selectList(schoolWrapper);
    }
    
    @Override
    public List<Long> getCooperationEnterpriseIdsBySchoolId(Long schoolId) {
        if (schoolId == null) {
            return Collections.emptyList();
        }
        
        // 查询合作关系
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getSchoolId, schoolId)
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<EnterpriseSchoolCooperation> cooperations = this.list(wrapper);
        
        return cooperations.stream()
                .map(EnterpriseSchoolCooperation::getEnterpriseId)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> getCooperationSchoolIdsByEnterpriseId(Long enterpriseId) {
        if (enterpriseId == null) {
            return Collections.emptyList();
        }
        
        // 查询合作关系
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, enterpriseId)
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<EnterpriseSchoolCooperation> cooperations = this.list(wrapper);
        
        return cooperations.stream()
                .map(EnterpriseSchoolCooperation::getSchoolId)
                .distinct()
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean hasCooperation(Long enterpriseId, Long schoolId) {
        if (enterpriseId == null || schoolId == null) {
            return false;
        }
        
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, enterpriseId)
               .eq(EnterpriseSchoolCooperation::getSchoolId, schoolId)
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.count(wrapper) > 0;
    }
    
    @Override
    public boolean hasCooperationByType(Long enterpriseId, Long schoolId, String cooperationType) {
        if (enterpriseId == null || schoolId == null || cooperationType == null) {
            return false;
        }
        
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, enterpriseId)
               .eq(EnterpriseSchoolCooperation::getSchoolId, schoolId)
               .eq(EnterpriseSchoolCooperation::getCooperationType, cooperationType)
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, CooperationStatus.IN_PROGRESS.getCode())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.count(wrapper) > 0;
    }
    
    @Override
    public List<EnterpriseSchoolCooperation> getCooperationListByEnterpriseId(Long enterpriseId) {
        if (enterpriseId == null) {
            return Collections.emptyList();
        }
        
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, enterpriseId)
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤：
        // 1. 系统管理员：不需要过滤
        // 2. 企业管理员：查询自己企业的合作关系，不需要按学校过滤
        // 3. 学校管理员：只能看到自己学校的合作关系
        if (!dataPermissionUtil.isSystemAdmin()) {
            // 检查是否为学校管理员（企业管理员不需要学校过滤）
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null) {
                // 是学校管理员，按学校过滤
                wrapper.eq(EnterpriseSchoolCooperation::getSchoolId, currentUserSchoolId);
            }
            // 如果是企业管理员（没有学校ID），不需要过滤，可以查看自己企业的所有合作关系
        }
        
        wrapper.orderByDesc(EnterpriseSchoolCooperation::getCreateTime);
        List<EnterpriseSchoolCooperation> cooperations = this.list(wrapper);
        
        // 填充学校名称
        for (EnterpriseSchoolCooperation cooperation : cooperations) {
            if (cooperation.getSchoolId() != null) {
                School school = schoolMapper.selectById(cooperation.getSchoolId());
                if (school != null) {
                    cooperation.setSchoolName(school.getSchoolName());
                }
            }
        }
        
        return cooperations;
    }
}

