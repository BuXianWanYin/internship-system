package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.domain.user.SchoolAdmin;
import com.server.internshipserver.mapper.user.SchoolAdminMapper;
import com.server.internshipserver.service.user.SchoolAdminService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 学校管理员管理Service实现类
 */
@Service
public class SchoolAdminServiceImpl extends ServiceImpl<SchoolAdminMapper, SchoolAdmin> implements SchoolAdminService {
    
    @Override
    public SchoolAdmin getSchoolAdminByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<SchoolAdmin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolAdmin::getUserId, userId)
               .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolAdmin addSchoolAdmin(SchoolAdmin schoolAdmin) {
        // 参数校验
        if (schoolAdmin.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (schoolAdmin.getSchoolId() == null) {
            throw new BusinessException("学校ID不能为空");
        }
        
        // 检查用户ID是否已被使用
        SchoolAdmin existAdmin = getSchoolAdminByUserId(schoolAdmin.getUserId());
        if (existAdmin != null) {
            throw new BusinessException("该用户已经是学校管理员");
        }
        
        // 设置默认值
        if (schoolAdmin.getStatus() == null) {
            schoolAdmin.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        }
        schoolAdmin.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(schoolAdmin);
        return schoolAdmin;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolAdmin updateSchoolAdmin(SchoolAdmin schoolAdmin) {
        if (schoolAdmin.getAdminId() == null) {
            throw new BusinessException("管理员ID不能为空");
        }
        
        // 检查管理员是否存在
        SchoolAdmin existAdmin = this.getById(schoolAdmin.getAdminId());
        EntityValidationUtil.validateEntityExists(existAdmin, "学校管理员");
        
        // 更新
        this.updateById(schoolAdmin);
        return this.getById(schoolAdmin.getAdminId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchoolAdmin(Long adminId) {
        EntityValidationUtil.validateIdNotNull(adminId, "管理员ID");
        
        SchoolAdmin schoolAdmin = this.getById(adminId);
        EntityValidationUtil.validateEntityExists(schoolAdmin, "学校管理员");
        
        // 软删除
        schoolAdmin.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(schoolAdmin);
    }
}

