package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.service.system.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学院管理Service实现类
 */
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public College addCollege(College college) {
        // 参数校验
        if (!StringUtils.hasText(college.getCollegeName())) {
            throw new BusinessException("学院名称不能为空");
        }
        if (!StringUtils.hasText(college.getCollegeCode())) {
            throw new BusinessException("学院代码不能为空");
        }
        if (college.getSchoolId() == null) {
            throw new BusinessException("所属学校ID不能为空");
        }
        
        // 检查学院代码在同一学校内是否已存在
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(College::getCollegeCode, college.getCollegeCode())
               .eq(College::getSchoolId, college.getSchoolId())
               .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        College existCollege = this.getOne(wrapper);
        if (existCollege != null) {
            throw new BusinessException("该学校下学院代码已存在");
        }
        
        // 设置默认值
        if (college.getStatus() == null) {
            college.setStatus(1); // 默认启用
        }
        college.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(college);
        return college;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public College updateCollege(College college) {
        if (college.getCollegeId() == null) {
            throw new BusinessException("学院ID不能为空");
        }
        
        // 检查学院是否存在
        College existCollege = this.getById(college.getCollegeId());
        if (existCollege == null || existCollege.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学院不存在");
        }
        
        // 如果修改了学院代码，检查新代码在同一学校内是否已存在
        if (StringUtils.hasText(college.getCollegeCode()) 
                && !college.getCollegeCode().equals(existCollege.getCollegeCode())) {
            LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(College::getCollegeCode, college.getCollegeCode())
                   .eq(College::getSchoolId, existCollege.getSchoolId())
                   .ne(College::getCollegeId, college.getCollegeId())
                   .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            College codeExistCollege = this.getOne(wrapper);
            if (codeExistCollege != null) {
                throw new BusinessException("该学校下学院代码已存在");
            }
        }
        
        // 更新
        this.updateById(college);
        return this.getById(college.getCollegeId());
    }
    
    @Override
    public College getCollegeById(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException("学院ID不能为空");
        }
        
        College college = this.getById(collegeId);
        if (college == null || college.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学院不存在");
        }
        
        return college;
    }
    
    @Override
    public Page<College> getCollegePage(Page<College> page, String collegeName, Long schoolId) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(collegeName)) {
            wrapper.like(College::getCollegeName, collegeName);
        }
        
        // 数据权限过滤：根据学校ID过滤
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (currentUserSchoolId != null) {
            // 如果当前用户有学校ID限制，使用当前用户的学校ID
            wrapper.eq(College::getSchoolId, currentUserSchoolId);
        } else if (schoolId != null) {
            // 系统管理员可以指定学校ID查询
            wrapper.eq(College::getSchoolId, schoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(College::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCollege(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException("学院ID不能为空");
        }
        
        College college = this.getById(collegeId);
        if (college == null || college.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("学院不存在");
        }
        
        // 软删除
        college.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(college);
    }
}

