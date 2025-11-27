package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.IsCurrent;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.mapper.system.SemesterMapper;
import com.server.internshipserver.service.system.SemesterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 学期管理Service实现类
 */
@Service
public class SemesterServiceImpl extends ServiceImpl<SemesterMapper, Semester> implements SemesterService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Semester addSemester(Semester semester) {
        // 参数校验
        if (!StringUtils.hasText(semester.getSemesterName())) {
            throw new BusinessException("学期名称不能为空");
        }
        if (semester.getStartDate() == null) {
            throw new BusinessException("开始日期不能为空");
        }
        if (semester.getEndDate() == null) {
            throw new BusinessException("结束日期不能为空");
        }
        if (semester.getStartDate().isAfter(semester.getEndDate())) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        if (semester.getSchoolId() == null) {
            throw new BusinessException("所属学校ID不能为空");
        }
        
        // 检查学期名称在同一学校内是否已存在
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Semester::getSemesterName, semester.getSemesterName())
               .eq(Semester::getSchoolId, semester.getSchoolId())
               .eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Semester existSemester = this.getOne(wrapper);
        if (existSemester != null) {
            throw new BusinessException("该学校下学期名称已存在");
        }
        
        // 设置默认值
        if (semester.getIsCurrent() == null) {
            semester.setIsCurrent(IsCurrent.NO.getCode()); // 默认不是当前学期
        }
        semester.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
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
    public Page<Semester> getSemesterPage(Page<Semester> page, String semesterName, Integer year, Integer isCurrent, String startDate, String endDate, Long schoolId) {
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(semesterName)) {
            wrapper.like(Semester::getSemesterName, semesterName);
        }
        
        // 学校ID筛选
        if (schoolId != null) {
            wrapper.eq(Semester::getSchoolId, schoolId);
        }
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的学期
            wrapper.eq(Semester::getSchoolId, currentUserSchoolId);
        }
        
        // 年份筛选
        if (year != null) {
            wrapper.and(w -> w
                .apply("YEAR(start_date) = {0}", year)
                .or()
                .apply("YEAR(end_date) = {0}", year)
            );
        }
        
        // 是否当前学期筛选
        if (isCurrent != null) {
            wrapper.eq(Semester::getIsCurrent, isCurrent);
        }
        
        // 日期范围筛选
        if (StringUtils.hasText(startDate)) {
            wrapper.ge(Semester::getStartDate, startDate);
        }
        if (StringUtils.hasText(endDate)) {
            wrapper.le(Semester::getEndDate, endDate);
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
        updateWrapper.set(Semester::getIsCurrent, 0)
                     .eq(Semester::getSchoolId, schoolId)
                     .eq(Semester::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        this.update(updateWrapper);
        
        // 如果指定了学期ID，设置为当前学期
        if (semesterId != null) {
            Semester semester = getSemesterById(semesterId);
            semester.setIsCurrent(1);
            this.updateById(semester);
        }
    }
    
    @Override
    public Semester getCurrentSemester() {
        // 获取当前用户的学校ID
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        LambdaQueryWrapper<Semester> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Semester::getIsCurrent, 1)
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
}

