package com.server.internshipserver.service.impl.cooperation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.cooperation.EnterpriseSchoolCooperation;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.mapper.cooperation.EnterpriseSchoolCooperationMapper;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
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
        
        // 检查合作关系是否已存在
        LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, cooperation.getEnterpriseId())
               .eq(EnterpriseSchoolCooperation::getSchoolId, cooperation.getSchoolId())
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        EnterpriseSchoolCooperation existCooperation = this.getOne(wrapper);
        if (existCooperation != null) {
            throw new BusinessException("该合作关系已存在");
        }
        
        // 设置默认值
        if (cooperation.getCooperationStatus() == null) {
            cooperation.setCooperationStatus(1); // 默认进行中
        }
        cooperation.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
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
        if (existCooperation == null || existCooperation.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("合作关系不存在");
        }
        
        // 如果修改了企业ID或学校ID，检查新关系是否已存在
        if ((cooperation.getEnterpriseId() != null && !cooperation.getEnterpriseId().equals(existCooperation.getEnterpriseId()))
                || (cooperation.getSchoolId() != null && !cooperation.getSchoolId().equals(existCooperation.getSchoolId()))) {
            LambdaQueryWrapper<EnterpriseSchoolCooperation> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(EnterpriseSchoolCooperation::getEnterpriseId, 
                    cooperation.getEnterpriseId() != null ? cooperation.getEnterpriseId() : existCooperation.getEnterpriseId())
                   .eq(EnterpriseSchoolCooperation::getSchoolId, 
                    cooperation.getSchoolId() != null ? cooperation.getSchoolId() : existCooperation.getSchoolId())
                   .ne(EnterpriseSchoolCooperation::getId, cooperation.getId())
                   .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            EnterpriseSchoolCooperation duplicateCooperation = this.getOne(wrapper);
            if (duplicateCooperation != null) {
                throw new BusinessException("该合作关系已存在");
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
        if (cooperation == null || cooperation.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("合作关系不存在");
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
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, 1) // 只查询进行中的合作
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
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
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, 1) // 只查询进行中的合作
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
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, 1) // 只查询进行中的合作
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
               .eq(EnterpriseSchoolCooperation::getCooperationStatus, 1) // 只查询进行中的合作
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
               .eq(EnterpriseSchoolCooperation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(EnterpriseSchoolCooperation::getCreateTime);
        return this.list(wrapper);
    }
}

