package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.service.system.SchoolService;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学校管理Service实现类
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public School addSchool(School school) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(school.getSchoolName(), "学校名称");
        EntityValidationUtil.validateStringNotBlank(school.getSchoolCode(), "学校代码");
        
        // 检查学校代码是否已存在
        UniquenessValidationUtil.validateUnique(this, School::getSchoolCode, school.getSchoolCode(), 
                School::getDeleteFlag, "学校代码");
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(school);
        
        // 保存
        this.save(school);
        return school;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public School updateSchool(School school) {
        EntityValidationUtil.validateIdNotNull(school.getSchoolId(), "学校ID");
        
        // 检查学校是否存在
        School existSchool = this.getById(school.getSchoolId());
        EntityValidationUtil.validateEntityExists(existSchool, "学校");
        
        // 如果修改了学校代码，检查新代码是否已存在
        if (StringUtils.hasText(school.getSchoolCode()) 
                && !school.getSchoolCode().equals(existSchool.getSchoolCode())) {
            UniquenessValidationUtil.validateUniqueExcludeId(this, School::getSchoolCode, school.getSchoolCode(),
                    School::getSchoolId, school.getSchoolId(), School::getDeleteFlag, "学校代码");
        }
        
        // 更新
        this.updateById(school);
        return this.getById(school.getSchoolId());
    }
    
    @Override
    public School getSchoolById(Long schoolId) {
        EntityValidationUtil.validateIdNotNull(schoolId, "学校ID");
        
        School school = this.getById(schoolId);
        EntityValidationUtil.validateEntityExists(school, "学校");
        
        // 数据权限检查：非系统管理员只能查看自己学校的学校信息
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null && !currentUserSchoolId.equals(schoolId)) {
                throw new BusinessException("无权查看该学校信息");
            }
        }
        
        return school;
    }
    
    @Override
    public Page<School> getSchoolPage(Page<School> page, String schoolName, String schoolCode) {
        LambdaQueryWrapper<School> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(School::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(schoolName)) {
            wrapper.like(School::getSchoolName, schoolName);
        }
        if (StringUtils.hasText(schoolCode)) {
            wrapper.eq(School::getSchoolCode, schoolCode);
        }
        
        // 数据权限过滤：学校管理员只能看到自己的学校
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null) {
                wrapper.eq(School::getSchoolId, currentUserSchoolId);
            } else {
                // 如果没有学校ID，返回空结果
                wrapper.eq(School::getSchoolId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(School::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchool(Long schoolId) {
        if (schoolId == null) {
            throw new BusinessException("学校ID不能为空");
        }
        
        School school = this.getById(schoolId);
        EntityValidationUtil.validateEntityExists(school, "学校");
        
        // 软删除
        school.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(school);
    }
}

