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
        if (!StringUtils.hasText(school.getSchoolName())) {
            throw new BusinessException("学校名称不能为空");
        }
        if (!StringUtils.hasText(school.getSchoolCode())) {
            throw new BusinessException("学校代码不能为空");
        }
        
        // 检查学校代码是否已存在
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(School::getSchoolCode, school.getSchoolCode())
               .eq(School::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        School existSchool = this.getOne(wrapper);
        if (existSchool != null) {
            throw new BusinessException("学校代码已存在");
        }
        
        // 设置默认值
        if (school.getStatus() == null) {
            school.setStatus(1); // 默认启用
        }
        school.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(school);
        return school;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public School updateSchool(School school) {
        if (school.getSchoolId() == null) {
            throw new BusinessException("学校ID不能为空");
        }
        
        // 检查学校是否存在
        School existSchool = this.getById(school.getSchoolId());
        if (existSchool == null || existSchool.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学校不存在");
        }
        
        // 如果修改了学校代码，检查新代码是否已存在
        if (StringUtils.hasText(school.getSchoolCode()) 
                && !school.getSchoolCode().equals(existSchool.getSchoolCode())) {
            LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(School::getSchoolCode, school.getSchoolCode())
                   .ne(School::getSchoolId, school.getSchoolId())
                   .eq(School::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            School codeExistSchool = this.getOne(wrapper);
            if (codeExistSchool != null) {
                throw new BusinessException("学校代码已存在");
            }
        }
        
        // 更新
        this.updateById(school);
        return this.getById(school.getSchoolId());
    }
    
    @Override
    public School getSchoolById(Long schoolId) {
        if (schoolId == null) {
            throw new BusinessException("学校ID不能为空");
        }
        
        School school = this.getById(schoolId);
        if (school == null || school.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学校不存在");
        }
        
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
        LambdaQueryWrapper<School> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(School::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
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
        if (school == null || school.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学校不存在");
        }
        
        // 软删除
        school.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(school);
    }
}

