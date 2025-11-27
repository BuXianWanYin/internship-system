package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.InternshipPlanStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.AuditUtil;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.DateValidationUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.internship.dto.InternshipPlanQueryDTO;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.mapper.internship.InternshipPlanMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.system.SemesterMapper;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.service.internship.InternshipPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

/**
 * 实习计划管理Service实现类
 */
@Service
public class InternshipPlanServiceImpl extends ServiceImpl<InternshipPlanMapper, InternshipPlan> implements InternshipPlanService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SemesterMapper semesterMapper;
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipPlan addPlan(InternshipPlan plan) {
        // 参数校验
        validatePlanParams(plan);
        
        // 获取当前登录用户信息
        UserInfo currentUser = getCurrentUser();
        
        // 权限和数据权限处理
        List<String> roles = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        
        // 根据角色设置组织架构信息
        setOrgInfoByRole(plan, roles);
        
        // 验证计划范围的一致性
        validatePlanScope(plan);
        
        // 检查计划编号是否已存在
        validatePlanCode(plan);
        
        // 设置默认值和创建人
        setDefaultValues(plan, currentUser);
        
        // 保存
        this.save(plan);
        return plan;
    }
    
    /**
     * 验证计划参数
     */
    private void validatePlanParams(InternshipPlan plan) {
        EntityValidationUtil.validateStringNotBlank(plan.getPlanName(), "实习计划名称");
        EntityValidationUtil.validateStringNotBlank(plan.getPlanCode(), "实习计划编号");
        if (plan.getStartDate() == null || plan.getEndDate() == null) {
            throw new BusinessException("实习开始日期和结束日期不能为空");
        }
        DateValidationUtil.validateDateRange(plan.getStartDate(), plan.getEndDate(), "实习计划");
    }
    
    /**
     * 获取当前登录用户
     */
    private UserInfo getCurrentUser() {
        return UserUtil.getCurrentUser(userMapper);
    }
    
    /**
     * 根据角色设置组织架构信息
     */
    private void setOrgInfoByRole(InternshipPlan plan, List<String> roles) {
        if (roles.contains(Constants.ROLE_COLLEGE_LEADER)) {
            setOrgInfoForCollegeLeader(plan);
        } else if (roles.contains(Constants.ROLE_SCHOOL_ADMIN)) {
            setOrgInfoForSchoolAdmin(plan);
        } else if (roles.contains(Constants.ROLE_SYSTEM_ADMIN)) {
            setOrgInfoForSystemAdmin(plan);
        } else {
            throw new BusinessException("无权限创建实习计划");
        }
    }
    
    /**
     * 设置学院负责人的组织架构信息
     */
    private void setOrgInfoForCollegeLeader(InternshipPlan plan) {
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (currentUserCollegeId == null) {
            throw new BusinessException("学院负责人必须关联学院");
        }
        
        // 自动设置学院ID，不允许修改
        plan.setCollegeId(currentUserCollegeId);
        
        // 自动设置学校ID（从学院获取）
        College college = collegeMapper.selectById(currentUserCollegeId);
        if (college == null) {
            throw new BusinessException("学院信息不存在");
        }
        plan.setSchoolId(college.getSchoolId());
        
        // 学院负责人创建的计划，专业ID可以为NULL（表示全院）或指定专业
        // 如果指定了专业，需要验证专业是否属于本学院
        if (plan.getMajorId() != null) {
            Major major = majorMapper.selectById(plan.getMajorId());
            if (major == null || !major.getCollegeId().equals(currentUserCollegeId)) {
                throw new BusinessException("专业不属于当前学院");
            }
        }
    }
    
    /**
     * 设置学校管理员的组织架构信息
     */
    private void setOrgInfoForSchoolAdmin(InternshipPlan plan) {
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (currentUserSchoolId == null) {
            throw new BusinessException("学校管理员必须关联学校");
        }
        
        // 自动设置学校ID
        plan.setSchoolId(currentUserSchoolId);
        
        // 如果指定了学院，需要验证学院是否属于本校
        if (plan.getCollegeId() != null) {
            College college = collegeMapper.selectById(plan.getCollegeId());
            if (college == null || !college.getSchoolId().equals(currentUserSchoolId)) {
                throw new BusinessException("学院不属于当前学校");
            }
        }
        
        // 如果指定了专业，需要验证专业是否属于指定的学院
        if (plan.getMajorId() != null) {
            validateMajorForSchoolAdmin(plan, currentUserSchoolId);
        }
    }
    
    /**
     * 验证学校管理员指定的专业
     */
    private void validateMajorForSchoolAdmin(InternshipPlan plan, Long schoolId) {
        Major major = majorMapper.selectById(plan.getMajorId());
        if (major == null) {
            throw new BusinessException("专业不存在");
        }
        if (plan.getCollegeId() != null && !major.getCollegeId().equals(plan.getCollegeId())) {
            throw new BusinessException("专业不属于指定的学院");
        }
        if (plan.getCollegeId() == null) {
            // 如果学院为NULL，需要验证专业所属学院是否属于本校
            College majorCollege = collegeMapper.selectById(major.getCollegeId());
            if (majorCollege == null || !majorCollege.getSchoolId().equals(schoolId)) {
                throw new BusinessException("专业所属学院不属于当前学校");
            }
        }
    }
    
    /**
     * 设置系统管理员的组织架构信息
     */
    private void setOrgInfoForSystemAdmin(InternshipPlan plan) {
        if (plan.getSchoolId() == null) {
            throw new BusinessException("所属学校ID不能为空");
        }
        // 验证学校是否存在
        School school = schoolMapper.selectById(plan.getSchoolId());
        if (school == null) {
            throw new BusinessException("学校不存在");
        }
        
        // 如果指定了学院，验证学院是否属于指定的学校
        if (plan.getCollegeId() != null) {
            College college = collegeMapper.selectById(plan.getCollegeId());
            if (college == null || !college.getSchoolId().equals(plan.getSchoolId())) {
                throw new BusinessException("学院不属于指定的学校");
            }
        }
        
        // 如果指定了专业，验证专业是否属于指定的学院
        if (plan.getMajorId() != null) {
            Major major = majorMapper.selectById(plan.getMajorId());
            if (major == null) {
                throw new BusinessException("专业不存在");
            }
            if (plan.getCollegeId() != null && !major.getCollegeId().equals(plan.getCollegeId())) {
                throw new BusinessException("专业不属于指定的学院");
            }
        }
    }
    
    /**
     * 验证计划范围的一致性
     */
    private void validatePlanScope(InternshipPlan plan) {
        // 如果collegeId为NULL，majorId也必须为NULL（不能只有专业没有学院）
        if (plan.getCollegeId() == null && plan.getMajorId() != null) {
            throw new BusinessException("专业计划必须指定所属学院");
        }
        // 如果majorId不为NULL，collegeId也不能为NULL（专业必须属于某个学院）
        if (plan.getMajorId() != null && plan.getCollegeId() == null) {
            throw new BusinessException("专业计划必须指定所属学院");
        }
    }
    
    /**
     * 验证计划编号是否已存在
     */
    private void validatePlanCode(InternshipPlan plan) {
        LambdaQueryWrapper<InternshipPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipPlan::getPlanCode, plan.getPlanCode())
               .eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipPlan existPlan = this.getOne(wrapper);
        if (existPlan != null) {
            throw new BusinessException("实习计划编号已存在");
        }
    }
    
    /**
     * 设置默认值和创建人
     */
    private void setDefaultValues(InternshipPlan plan, UserInfo currentUser) {
        if (plan.getStatus() == null) {
            plan.setStatus(InternshipPlanStatus.DRAFT.getCode()); // 默认草稿
        }
        EntityDefaultValueUtil.setDefaultValues(plan);
        
        // 设置创建人ID
        plan.setCreateUserId(currentUser.getUserId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipPlan updatePlan(InternshipPlan plan) {
        if (plan.getPlanId() == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        // 检查计划是否存在
        InternshipPlan existPlan = this.getById(plan.getPlanId());
        EntityValidationUtil.validateEntityExists(existPlan, "实习计划");
        
        // 权限检查：只有创建者或更高级别的管理员可以修改
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        
        if (currentUser != null) {
            List<String> roles = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            
            // 系统管理员和学校管理员可以修改所有计划
            boolean canModify = roles.contains(Constants.ROLE_SYSTEM_ADMIN) || 
                               roles.contains(Constants.ROLE_SCHOOL_ADMIN);
            
            // 学院负责人只能修改自己创建的计划
            if (!canModify && roles.contains(Constants.ROLE_COLLEGE_LEADER)) {
                if (!existPlan.getCreateUserId().equals(currentUser.getUserId())) {
                    throw new BusinessException("只能修改自己创建的实习计划");
                }
                
                // 学院负责人不能修改组织架构信息
                if (plan.getSchoolId() != null && !plan.getSchoolId().equals(existPlan.getSchoolId())) {
                    throw new BusinessException("不能修改计划的学校");
                }
                if (plan.getCollegeId() != null && !plan.getCollegeId().equals(existPlan.getCollegeId())) {
                    throw new BusinessException("不能修改计划的学院");
                }
            }
            
            if (!canModify && !roles.contains(Constants.ROLE_COLLEGE_LEADER)) {
                throw new BusinessException("无权限修改实习计划");
            }
        }
        
        // 如果修改了计划编号，检查新编号是否已存在
        if (StringUtils.hasText(plan.getPlanCode()) 
                && !plan.getPlanCode().equals(existPlan.getPlanCode())) {
            LambdaQueryWrapper<InternshipPlan> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(InternshipPlan::getPlanCode, plan.getPlanCode())
                   .ne(InternshipPlan::getPlanId, plan.getPlanId())
                   .eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            InternshipPlan codeExistPlan = this.getOne(wrapper);
            if (codeExistPlan != null) {
                throw new BusinessException("实习计划编号已存在");
            }
        }
        
        // 验证计划范围的一致性
        validatePlanScope(plan);
        
        // 使用 UpdateWrapper 确保 null 值也能更新
        LambdaUpdateWrapper<InternshipPlan> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(InternshipPlan::getPlanId, plan.getPlanId())
                    .set(InternshipPlan::getPlanName, plan.getPlanName())
                    .set(InternshipPlan::getPlanCode, plan.getPlanCode())
                    .set(InternshipPlan::getSemesterId, plan.getSemesterId())
                    .set(InternshipPlan::getSchoolId, plan.getSchoolId())
                    .set(InternshipPlan::getCollegeId, plan.getCollegeId())  // 允许设置为null
                    .set(InternshipPlan::getMajorId, plan.getMajorId())  // 允许设置为null
                    .set(InternshipPlan::getPlanType, plan.getPlanType())
                    .set(InternshipPlan::getStartDate, plan.getStartDate())
                    .set(InternshipPlan::getEndDate, plan.getEndDate())
                    .set(InternshipPlan::getPlanOutline, plan.getPlanOutline())
                    .set(InternshipPlan::getTaskRequirements, plan.getTaskRequirements())
                    .set(InternshipPlan::getAssessmentStandards, plan.getAssessmentStandards());
        
        // 更新
        this.update(updateWrapper);
        return this.getById(plan.getPlanId());
    }
    
    @Override
    public InternshipPlan getPlanById(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        
        if (currentUserSchoolId != null && !currentUserSchoolId.equals(plan.getSchoolId())) {
            throw new BusinessException("无权查看该实习计划");
        }
        if (currentUserCollegeId != null && plan.getCollegeId() != null 
                && !currentUserCollegeId.equals(plan.getCollegeId())) {
            throw new BusinessException("无权查看该实习计划");
        }
        
        // 填充关联字段
        fillPlanRelatedFields(plan);
        
        return plan;
    }
    
    @Override
    public Page<InternshipPlan> getPlanPage(Page<InternshipPlan> page, InternshipPlanQueryDTO queryDTO) {
        LambdaQueryWrapper<InternshipPlan> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipPlan::getDeleteFlag);
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        
        if (currentUserSchoolId != null) {
            wrapper.eq(InternshipPlan::getSchoolId, currentUserSchoolId);
        }
        if (currentUserCollegeId != null) {
            wrapper.eq(InternshipPlan::getCollegeId, currentUserCollegeId);
        }
        
        // 条件查询
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getPlanName())) {
                wrapper.like(InternshipPlan::getPlanName, queryDTO.getPlanName());
            }
            if (queryDTO.getSemesterId() != null) {
                wrapper.eq(InternshipPlan::getSemesterId, queryDTO.getSemesterId());
            }
            if (queryDTO.getSchoolId() != null) {
                wrapper.eq(InternshipPlan::getSchoolId, queryDTO.getSchoolId());
            }
            if (queryDTO.getCollegeId() != null) {
                wrapper.eq(InternshipPlan::getCollegeId, queryDTO.getCollegeId());
            }
            if (queryDTO.getMajorId() != null) {
                wrapper.eq(InternshipPlan::getMajorId, queryDTO.getMajorId());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(InternshipPlan::getStatus, queryDTO.getStatus());
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipPlan::getCreateTime);
        
        Page<InternshipPlan> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (InternshipPlan plan : result.getRecords()) {
                fillPlanRelatedFields(plan);
            }
        }
        
        return result;
    }
    
    /**
     * 填充计划关联字段
     */
    private void fillPlanRelatedFields(InternshipPlan plan) {
        // 填充学期信息
        if (plan.getSemesterId() != null) {
            Semester semester = semesterMapper.selectById(plan.getSemesterId());
            if (semester != null) {
                plan.setSemesterName(semester.getSemesterName());
            }
        }
        
        // 填充学校信息
        if (plan.getSchoolId() != null) {
            School school = schoolMapper.selectById(plan.getSchoolId());
            if (school != null) {
                plan.setSchoolName(school.getSchoolName());
            }
        }
        
        // 填充学院信息
        if (plan.getCollegeId() != null) {
            College college = collegeMapper.selectById(plan.getCollegeId());
            if (college != null) {
                plan.setCollegeName(college.getCollegeName());
            }
        }
        
        // 填充专业信息
        if (plan.getMajorId() != null) {
            Major major = majorMapper.selectById(plan.getMajorId());
            if (major != null) {
                plan.setMajorName(major.getMajorName());
            }
        }
        
        // 设置计划类型标识（用于前端显示）
        if (plan.getMajorId() != null) {
            plan.setPlanScopeType("专业计划");
        } else if (plan.getCollegeId() != null) {
            plan.setPlanScopeType("全院计划");
        } else {
            plan.setPlanScopeType("全校计划");
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitPlan(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        // 只有草稿状态才能提交审核
        EntityValidationUtil.validateStatusEquals(plan, InternshipPlanStatus.DRAFT.getCode(), "实习计划", "只有草稿状态的实习计划才能提交审核");
        
        // 更新状态为待审核
        plan.setStatus(InternshipPlanStatus.PENDING.getCode());
        return this.updateById(plan);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditPlan(Long planId, Integer auditStatus, String auditOpinion) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        if (auditStatus == null || 
                (!auditStatus.equals(AuditStatus.APPROVED.getCode()) && !auditStatus.equals(AuditStatus.REJECTED.getCode()))) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipPlan plan = this.getById(planId);
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        // 只有待审核状态才能审核
        EntityValidationUtil.validateStatusEquals(plan, InternshipPlanStatus.PENDING.getCode(), "实习计划", "只有待审核状态的实习计划才能审核");
        
        // 设置审核信息
        AuditUtil.setAuditInfo(plan, auditStatus, auditOpinion, userMapper);
        
        return this.updateById(plan);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPlan(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        // 只有草稿状态才能发布
        EntityValidationUtil.validateStatusEquals(plan, InternshipPlanStatus.DRAFT.getCode(), "实习计划", "只有草稿状态的实习计划才能发布");
        
        // 更新状态为已发布
        plan.setStatus(InternshipPlanStatus.PUBLISHED.getCode());
        plan.setPublishTime(LocalDateTime.now());
        return this.updateById(plan);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deletePlan(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        // 软删除（允许删除所有状态的计划，包括已发布的）
        plan.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(plan);
    }
    
    @Override
    public List<InternshipPlan> getAvailablePlansForStudent(Long studentId) {
        if (studentId == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        // 获取学生信息
        Student student = studentMapper.selectById(studentId);
        EntityValidationUtil.validateEntityExists(student, "学生信息");
        
        // 查询已发布的、匹配组织架构的、在有效期内的实习计划
        LocalDate currentDate = LocalDate.now();
        return getPublishedPlans(
            student.getSchoolId(), 
            student.getCollegeId(), 
            student.getMajorId(), 
            currentDate
        );
    }
    
    @Override
    public List<InternshipPlan> getPublishedPlans(Long schoolId, Long collegeId, Long majorId, LocalDate currentDate) {
        LambdaQueryWrapper<InternshipPlan> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询已发布的计划
        wrapper.eq(InternshipPlan::getStatus, InternshipPlanStatus.PUBLISHED.getCode())
               .eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 组织架构匹配
        if (schoolId != null) {
            wrapper.eq(InternshipPlan::getSchoolId, schoolId);
        }
        
        // 学院匹配：计划可以是全校的（college_id为NULL）或指定学院的
        if (collegeId != null) {
            wrapper.and(w -> w.isNull(InternshipPlan::getCollegeId)
                            .or()
                            .eq(InternshipPlan::getCollegeId, collegeId));
        }
        
        // 专业匹配：计划可以是全院的（major_id为NULL）或指定专业的
        if (majorId != null) {
            wrapper.and(w -> w.isNull(InternshipPlan::getMajorId)
                            .or()
                            .eq(InternshipPlan::getMajorId, majorId));
        }
        
        // 时间范围：当前日期在计划的有效期内
        if (currentDate != null) {
            wrapper.le(InternshipPlan::getStartDate, currentDate)
                   .ge(InternshipPlan::getEndDate, currentDate);
        }
        
        // 按发布时间倒序
        wrapper.orderByDesc(InternshipPlan::getPublishTime);
        
        List<InternshipPlan> plans = this.list(wrapper);
        
        // 填充关联字段
        for (InternshipPlan plan : plans) {
            fillPlanRelatedFields(plan);
        }
        
        return plans;
    }
}

