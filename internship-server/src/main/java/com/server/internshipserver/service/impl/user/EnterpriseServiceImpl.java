package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.User;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.user.EnterpriseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 企业管理Service实现类
 */
@Service
public class EnterpriseServiceImpl extends ServiceImpl<EnterpriseMapper, Enterprise> implements EnterpriseService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public Enterprise getEnterpriseByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getUserId, userId)
               .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public Enterprise getEnterpriseByEnterpriseCode(String enterpriseCode) {
        if (!StringUtils.hasText(enterpriseCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Enterprise::getEnterpriseCode, enterpriseCode)
               .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise registerEnterprise(Enterprise enterprise) {
        // 参数校验
        if (!StringUtils.hasText(enterprise.getEnterpriseName())) {
            throw new BusinessException("企业名称不能为空");
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise existEnterprise = this.getOne(wrapper);
            if (existEnterprise != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 设置默认值
        enterprise.setAuditStatus(0); // 待审核
        if (enterprise.getStatus() == null) {
            enterprise.setStatus(1); // 默认启用
        }
        enterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(enterprise);
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise addEnterprise(Enterprise enterprise) {
        // 参数校验
        if (!StringUtils.hasText(enterprise.getEnterpriseName())) {
            throw new BusinessException("企业名称不能为空");
        }
        
        // 检查企业代码是否已存在
        if (StringUtils.hasText(enterprise.getEnterpriseCode())) {
            Enterprise existEnterprise = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
            if (existEnterprise != null) {
                throw new BusinessException("企业代码已存在");
            }
        }
        
        // 检查统一社会信用代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise existEnterprise = this.getOne(wrapper);
            if (existEnterprise != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 设置默认值
        if (enterprise.getAuditStatus() == null) {
            enterprise.setAuditStatus(1); // 默认已通过
        }
        if (enterprise.getStatus() == null) {
            enterprise.setStatus(1); // 默认启用
        }
        enterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(enterprise);
        return enterprise;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Enterprise updateEnterprise(Enterprise enterprise) {
        if (enterprise.getEnterpriseId() == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        // 检查企业是否存在
        Enterprise existEnterprise = this.getById(enterprise.getEnterpriseId());
        if (existEnterprise == null || existEnterprise.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("企业不存在");
        }
        
        // 如果修改了企业代码，检查新代码是否已存在
        if (StringUtils.hasText(enterprise.getEnterpriseCode()) 
                && !enterprise.getEnterpriseCode().equals(existEnterprise.getEnterpriseCode())) {
            Enterprise codeExist = getEnterpriseByEnterpriseCode(enterprise.getEnterpriseCode());
            if (codeExist != null) {
                throw new BusinessException("企业代码已存在");
            }
        }
        
        // 如果修改了统一社会信用代码，检查新代码是否已存在
        if (StringUtils.hasText(enterprise.getUnifiedSocialCreditCode()) 
                && !enterprise.getUnifiedSocialCreditCode().equals(existEnterprise.getUnifiedSocialCreditCode())) {
            LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Enterprise::getUnifiedSocialCreditCode, enterprise.getUnifiedSocialCreditCode())
                   .ne(Enterprise::getEnterpriseId, enterprise.getEnterpriseId())
                   .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Enterprise codeExist = this.getOne(wrapper);
            if (codeExist != null) {
                throw new BusinessException("统一社会信用代码已存在");
            }
        }
        
        // 更新
        this.updateById(enterprise);
        return this.getById(enterprise.getEnterpriseId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditEnterprise(Long enterpriseId, Integer auditStatus, String auditOpinion, Long auditorId) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            throw new BusinessException("审核状态无效");
        }
        if (auditorId == null) {
            throw new BusinessException("审核人ID不能为空");
        }
        
        Enterprise enterprise = this.getById(enterpriseId);
        if (enterprise == null || enterprise.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("企业不存在");
        }
        
        // 更新审核信息
        enterprise.setAuditStatus(auditStatus);
        enterprise.setAuditOpinion(auditOpinion);
        enterprise.setAuditTime(LocalDateTime.now());
        enterprise.setAuditorId(auditorId);
        
        // 如果审核通过，激活企业管理员账号
        if (auditStatus == 1 && enterprise.getUserId() != null) {
            User user = userMapper.selectById(enterprise.getUserId());
            if (user != null && user.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                // 激活账号：设置状态为启用
                user.setStatus(1);
                userMapper.updateById(user);
            }
        }
        
        return this.updateById(enterprise);
    }
    
    @Override
    public Enterprise getEnterpriseById(Long enterpriseId) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        Enterprise enterprise = this.getById(enterpriseId);
        if (enterprise == null || enterprise.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("企业不存在");
        }
        
        return enterprise;
    }
    
    @Override
    public Page<Enterprise> getEnterprisePage(Page<Enterprise> page, String enterpriseName, 
                                               String enterpriseCode, Integer auditStatus) {
        LambdaQueryWrapper<Enterprise> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(enterpriseName)) {
            wrapper.like(Enterprise::getEnterpriseName, enterpriseName);
        }
        if (StringUtils.hasText(enterpriseCode)) {
            wrapper.eq(Enterprise::getEnterpriseCode, enterpriseCode);
        }
        if (auditStatus != null) {
            wrapper.eq(Enterprise::getAuditStatus, auditStatus);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Enterprise::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEnterprise(Long enterpriseId) {
        if (enterpriseId == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        Enterprise enterprise = this.getById(enterpriseId);
        if (enterprise == null || enterprise.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("企业不存在");
        }
        
        // 软删除
        enterprise.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(enterprise);
    }
}

