package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.IsCurrent;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.DateValidationUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.domain.system.dto.SemesterQueryDTO;
import com.server.internshipserver.mapper.system.SemesterMapper;
import com.server.internshipserver.service.system.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 学期管理Service实现类
 * 实现学期信息的增删改查等业务功能
 */
@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Semester addSemester(Semester semester) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(semester.getSemesterName(), "学期名称");
        if (semester.getStartDate() == null) {
            throw new BusinessException("开始日期不能为空");
        }
        if (semester.getEndDate() == null) {
            throw new BusinessException("结束日期不能为空");
        }
        DateValidationUtil.validateDateRange(semester.getStartDate(), semester.getEndDate(), "学期");
        EntityValidationUtil.validateIdNotNull(semester.getSchoolId(), "所属学校ID");
        
        // 检查学期名称在同一学校内是否已存在
        UniquenessValidationUtil.validateUniqueInScope(this, Semester::getSemesterName, semester.getSemesterName(),
                Semester::getSchoolId, semester.getSchoolId(), Semester::getDeleteFlag, "学期名称", "学校");
        
        // 设置默认值
        if (semester.getIsCurrent() == null) {
            semester.setIsCurrent(IsCurrent.NO.getCode()); // 默认不是当前学期
        }
        EntityDefaultValueUtil.setDefaultValues(semester);
        
        // 如果设置为当前学期，需要先取消该学校的其他当前学期
        if (semester.getIsCurrent() != null && semester.getIsCurrent().equals(IsCurrent.YES.getCode())) {
            setCurrentSemesterForSchool(semester.getSchoolId(), null); // 先取消该学校的所有当前学期
        }
        
        // 保存
        this.save(semester);
        return semester;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Semester updateSemester(Semester semester) {
        if (semester.getSemesterId() == null) {
            throw new BusinessException("学期ID不能为空");
        }
        
        // 检查学期是否存在
        Semester existSemester = this.getById(semester.getSemesterId());
        EntityValidationUtil.validateEntityExists(existSemester, "学期");
        
        // 如果修改了学期名称，检查新名称在同一学校内是否已存在
        if (StringUtils.hasText(semester.getSemesterName()) 
                && !semester.getSemesterName().equals(existSemester.getSemesterName())) {
            LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Semester::getSemesterName, semester.getSemesterName())
                   .eq(Semester::getSchoolId, existSemester.getSchoolId())
                   .ne(Semester::getSemesterId, semester.getSemesterId())
                   .eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Semester nameExistSemester = this.getOne(wrapper);
            if (nameExistSemester != null) {
                throw new BusinessException("该学校下学期名称已存在");
            }
        }
        
        // 如果设置了schoolId，检查是否修改了学校
        if (semester.getSchoolId() != null && !semester.getSchoolId().equals(existSemester.getSchoolId())) {
            // 如果修改了学校，需要重新检查学期名称
            LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Semester::getSemesterName, semester.getSemesterName())
                   .eq(Semester::getSchoolId, semester.getSchoolId())
                   .ne(Semester::getSemesterId, semester.getSemesterId())
                   .eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Semester nameExistSemester = this.getOne(wrapper);
            if (nameExistSemester != null) {
                throw new BusinessException("该学校下学期名称已存在");
            }
        }
        
        // 如果设置为当前学期，需要先取消该学校的其他当前学期
        if (semester.getIsCurrent() != null && semester.getIsCurrent().equals(IsCurrent.YES.getCode())) {
            Long schoolId = semester.getSchoolId() != null ? semester.getSchoolId() : existSemester.getSchoolId();
            setCurrentSemesterForSchool(schoolId, semester.getSemesterId());
        }
        
        // 更新
        this.updateById(semester);
        return this.getById(semester.getSemesterId());
    }
    
    @Override
    public Semester getSemesterById(Long semesterId) {
        if (semesterId == null) {
            throw new BusinessException("学期ID不能为空");
        }
        
        Semester semester = this.getById(semesterId);
        EntityValidationUtil.validateEntityExists(semester, "学期");
        
        return semester;
    }
    
    @Override
    public Page<Semester> getSemesterPage(Page<Semester> page, SemesterQueryDTO queryDTO) {
        LambdaQueryWrapper<Semester> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(Semester::getDeleteFlag);
        
        // 条件查询
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getSemesterName())) {
                wrapper.like(Semester::getSemesterName, queryDTO.getSemesterName());
            }
            
            // 学校ID筛选
            if (queryDTO.getSchoolId() != null) {
                wrapper.eq(Semester::getSchoolId, queryDTO.getSchoolId());
            }
            
            // 年份筛选
            if (queryDTO.getYear() != null) {
                wrapper.and(w -> w
                    .apply("YEAR(start_date) = {0}", queryDTO.getYear())
                    .or()
                    .apply("YEAR(end_date) = {0}", queryDTO.getYear())
                );
            }
            
            // 是否当前学期筛选
            if (queryDTO.getIsCurrent() != null) {
                wrapper.eq(Semester::getIsCurrent, queryDTO.getIsCurrent());
            }
            
            // 日期范围筛选
            if (StringUtils.hasText(queryDTO.getStartDate())) {
                wrapper.ge(Semester::getStartDate, queryDTO.getStartDate());
            }
            if (StringUtils.hasText(queryDTO.getEndDate())) {
                wrapper.le(Semester::getEndDate, queryDTO.getEndDate());
            }
        }
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的学期
            wrapper.eq(Semester::getSchoolId, currentUserSchoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Semester::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean setCurrentSemester(Long semesterId) {
        if (semesterId == null) {
            throw new BusinessException("学期ID不能为空");
        }
        
        Semester semester = getSemesterById(semesterId);
        if (semester.getSchoolId() == null) {
            throw new BusinessException("学期所属学校ID不能为空");
        }
        
        // 先取消该学校的所有当前学期
        setCurrentSemesterForSchool(semester.getSchoolId(), semesterId);
        
        return true;
    }
    
    /**
     * 设置指定学校的当前学期（内部方法）
     * @param schoolId 学校ID
     * @param semesterId 学期ID（如果为null，则只取消当前学期）
     */
    private void setCurrentSemesterForSchool(Long schoolId, Long semesterId) {
        // 先取消该学校的所有当前学期
        LambdaUpdateWrapper<Semester> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(Semester::getIsCurrent, IsCurrent.NO.getCode())
                     .eq(Semester::getSchoolId, schoolId)
                     .eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        this.update(updateWrapper);
        
        // 如果指定了学期ID，设置为当前学期
        if (semesterId != null) {
            Semester semester = getSemesterById(semesterId);
            semester.setIsCurrent(IsCurrent.YES.getCode());
            this.updateById(semester);
        }
    }
    
    @Override
    public Semester getCurrentSemester() {
        // 获取当前用户的学校ID
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Semester::getIsCurrent, IsCurrent.YES.getCode())
               .eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 如果有学校ID，只查询该学校的当前学期
        if (currentUserSchoolId != null) {
            wrapper.eq(Semester::getSchoolId, currentUserSchoolId);
        }
        
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSemester(Long semesterId) {
        if (semesterId == null) {
            throw new BusinessException("学期ID不能为空");
        }
        
        Semester semester = this.getById(semesterId);
        EntityValidationUtil.validateEntityExists(semester, "学期");
        
        // 如果是当前学期，不能删除
        if (semester.getIsCurrent() != null && semester.getIsCurrent().equals(IsCurrent.YES.getCode())) {
            throw new BusinessException("不能删除当前学期，请先设置其他学期为当前学期");
        }
        
        // 软删除
        semester.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(semester);
    }
    
    @Override
    public List<Semester> getAllSemesters(SemesterQueryDTO queryDTO) {
        LambdaQueryWrapper<Semester> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(Semester::getDeleteFlag);
        
        // 条件查询
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getSemesterName())) {
                wrapper.like(Semester::getSemesterName, queryDTO.getSemesterName());
            }
            
            // 学校ID筛选
            if (queryDTO.getSchoolId() != null) {
                wrapper.eq(Semester::getSchoolId, queryDTO.getSchoolId());
            }
            
            // 年份筛选
            if (queryDTO.getYear() != null) {
                wrapper.and(w -> w
                    .apply("YEAR(start_date) = {0}", queryDTO.getYear())
                    .or()
                    .apply("YEAR(end_date) = {0}", queryDTO.getYear())
                );
            }
            
            // 是否当前学期筛选
            if (queryDTO.getIsCurrent() != null) {
                wrapper.eq(Semester::getIsCurrent, queryDTO.getIsCurrent());
            }
            
            // 日期范围筛选
            if (StringUtils.hasText(queryDTO.getStartDate())) {
                wrapper.ge(Semester::getStartDate, queryDTO.getStartDate());
            }
            if (StringUtils.hasText(queryDTO.getEndDate())) {
                wrapper.le(Semester::getEndDate, queryDTO.getEndDate());
            }
        }
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的学期
            wrapper.eq(Semester::getSchoolId, currentUserSchoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Semester::getCreateTime);
        
        return this.list(wrapper);
    }
}

