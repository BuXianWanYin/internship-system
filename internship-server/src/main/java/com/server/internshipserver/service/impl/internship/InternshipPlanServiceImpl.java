package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.system.Semester;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.mapper.internship.InternshipPlanMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.system.SemesterMapper;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.service.internship.InternshipPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipPlan addPlan(InternshipPlan plan) {
        // 参数校验
        if (!StringUtils.hasText(plan.getPlanName())) {
            throw new BusinessException("实习计划名称不能为空");
        }
        if (!StringUtils.hasText(plan.getPlanCode())) {
            throw new BusinessException("实习计划编号不能为空");
        }
        if (plan.getSchoolId() == null) {
            throw new BusinessException("所属学校ID不能为空");
        }
        if (plan.getStartDate() == null || plan.getEndDate() == null) {
            throw new BusinessException("实习开始日期和结束日期不能为空");
        }
        if (plan.getStartDate().isAfter(plan.getEndDate())) {
            throw new BusinessException("实习开始日期不能晚于结束日期");
        }
        
        // 检查计划编号是否已存在
        LambdaQueryWrapper<InternshipPlan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipPlan::getPlanCode, plan.getPlanCode())
               .eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipPlan existPlan = this.getOne(wrapper);
        if (existPlan != null) {
            throw new BusinessException("实习计划编号已存在");
        }
        
        // 设置默认值
        if (plan.getStatus() == null) {
            plan.setStatus(0); // 默认草稿
        }
        plan.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 设置创建人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                plan.setCreateUserId(user.getUserId());
            }
        }
        
        // 保存
        this.save(plan);
        return plan;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipPlan updatePlan(InternshipPlan plan) {
        if (plan.getPlanId() == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        // 检查计划是否存在
        InternshipPlan existPlan = this.getById(plan.getPlanId());
        if (existPlan == null || existPlan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("实习计划不存在");
        }
        
        // 如果状态为已发布，不允许修改
        if (existPlan.getStatus() != null && existPlan.getStatus() == 4) {
            throw new BusinessException("已发布的实习计划不允许修改");
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
        
        // 更新
        this.updateById(plan);
        return this.getById(plan.getPlanId());
    }
    
    @Override
    public InternshipPlan getPlanById(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("实习计划不存在");
        }
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserInfoSchoolId();
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
    public Page<InternshipPlan> getPlanPage(Page<InternshipPlan> page, String planName, Long semesterId,
                                            Long schoolId, Long collegeId, Long majorId, Integer status) {
        LambdaQueryWrapper<InternshipPlan> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserInfoSchoolId();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        
        if (currentUserSchoolId != null) {
            wrapper.eq(InternshipPlan::getSchoolId, currentUserSchoolId);
        }
        if (currentUserCollegeId != null) {
            wrapper.eq(InternshipPlan::getCollegeId, currentUserCollegeId);
        }
        
        // 条件查询
        if (StringUtils.hasText(planName)) {
            wrapper.like(InternshipPlan::getPlanName, planName);
        }
        if (semesterId != null) {
            wrapper.eq(InternshipPlan::getSemesterId, semesterId);
        }
        if (schoolId != null) {
            wrapper.eq(InternshipPlan::getSchoolId, schoolId);
        }
        if (collegeId != null) {
            wrapper.eq(InternshipPlan::getCollegeId, collegeId);
        }
        if (majorId != null) {
            wrapper.eq(InternshipPlan::getMajorId, majorId);
        }
        if (status != null) {
            wrapper.eq(InternshipPlan::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipPlan::getCreateTime);
        
        Page<InternshipPlan> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
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
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitPlan(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("实习计划不存在");
        }
        
        // 只有草稿状态才能提交审核
        if (plan.getStatus() == null || plan.getStatus() != 0) {
            throw new BusinessException("只有草稿状态的实习计划才能提交审核");
        }
        
        // 更新状态为待审核
        plan.setStatus(1);
        return this.updateById(plan);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditPlan(Long planId, Integer auditStatus, String auditOpinion) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        if (auditStatus == null || (auditStatus != 2 && auditStatus != 3)) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipPlan plan = this.getById(planId);
        if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("实习计划不存在");
        }
        
        // 只有待审核状态才能审核
        if (plan.getStatus() == null || plan.getStatus() != 1) {
            throw new BusinessException("只有待审核状态的实习计划才能审核");
        }
        
        // 设置审核信息
        plan.setStatus(auditStatus);
        plan.setAuditTime(LocalDateTime.now());
        plan.setAuditOpinion(auditOpinion);
        
        // 设置审核人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                plan.setAuditUserId(user.getUserId());
            }
        }
        
        return this.updateById(plan);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean publishPlan(Long planId) {
        if (planId == null) {
            throw new BusinessException("实习计划ID不能为空");
        }
        
        InternshipPlan plan = this.getById(planId);
        if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("实习计划不存在");
        }
        
        // 只有已通过审核的才能发布
        if (plan.getStatus() == null || plan.getStatus() != 2) {
            throw new BusinessException("只有已通过审核的实习计划才能发布");
        }
        
        // 更新状态为已发布
        plan.setStatus(4);
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
        if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("实习计划不存在");
        }
        
        // 已发布的计划不允许删除
        if (plan.getStatus() != null && plan.getStatus() == 4) {
            throw new BusinessException("已发布的实习计划不允许删除");
        }
        
        // 软删除
        plan.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(plan);
    }
}

