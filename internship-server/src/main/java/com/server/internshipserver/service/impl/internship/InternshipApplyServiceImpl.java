package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.InternshipApplyStatus;
import com.server.internshipserver.common.enums.InternshipPlanStatus;
import com.server.internshipserver.common.enums.InternshipPostStatus;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.utils.AuditUtil;
import com.server.internshipserver.common.utils.DateValidationUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.enums.FilterAction;
import com.server.internshipserver.common.enums.InterviewResult;
import com.server.internshipserver.common.enums.InterviewStatus;
import com.server.internshipserver.common.enums.InterviewType;
import com.server.internshipserver.common.enums.StudentConfirmStatus;
import com.server.internshipserver.common.enums.StudentInternshipStatus;
import com.server.internshipserver.common.enums.UnbindStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.Interview;
import com.server.internshipserver.domain.internship.dto.AuditApplyDTO;
import com.server.internshipserver.domain.internship.dto.AuditUnbindDTO;
import com.server.internshipserver.domain.internship.dto.FilterApplyDTO;
import com.server.internshipserver.domain.internship.dto.InternshipApplyQueryDTO;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.internship.InterviewMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.EnterpriseMentorMapper;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import com.server.internshipserver.service.internship.InternshipApplyService;
import com.server.internshipserver.service.internship.InternshipPlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

/**
 * 实习申请管理Service实现类
 */
@Service
public class InternshipApplyServiceImpl extends ServiceImpl<InternshipApplyMapper, InternshipApply> implements InternshipApplyService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private InternshipPostMapper internshipPostMapper;
    
    @Autowired
    private EnterpriseSchoolCooperationService cooperationService;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private InterviewMapper interviewMapper;
    
    @Autowired
    private EnterpriseMentorMapper enterpriseMentorMapper;
    
    @Autowired
    private InternshipPlanService internshipPlanService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply addCooperationApply(InternshipApply apply) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(apply.getEnterpriseId(), "企业ID");
        
        // 获取当前登录学生信息
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        Student student = getCurrentStudent(user);
        
        // 验证企业合作关系
        validateEnterpriseCooperation(apply.getEnterpriseId(), student);
        
        // 验证岗位
        validatePost(apply);
        
        // 验证实习计划
        validateAndSetPlan(apply, student);
        
        // 检查是否已申请
        checkDuplicateApply(apply, student);
        
        // 设置申请信息并保存
        setApplyInfoAndSave(apply, student, user, ApplyType.COOPERATION.getCode());
        
        // 填充计划信息
        fillPlanInfo(apply);
        
        // 更新岗位申请人数
        updatePostAppliedCount(apply);
        
        return apply;
    }
    
    /**
     * 获取当前学生信息
     */
    private Student getCurrentStudent(UserInfo user) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, user.getUserId())
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        return student;
    }
    
    /**
     * 验证企业合作关系
     */
    private void validateEnterpriseCooperation(Long enterpriseId, Student student) {
        if (student.getSchoolId() == null) {
            return;
        }
        
        boolean hasCooperation = cooperationService.hasCooperation(enterpriseId, student.getSchoolId());
        if (!hasCooperation) {
            throw new BusinessException("该企业未与学校建立合作关系，无法申请");
        }
    }
    
    /**
     * 验证岗位
     */
    private void validatePost(InternshipApply apply) {
        if (apply.getPostId() == null) {
            return;
        }
        
        InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
        EntityValidationUtil.validateEntityExists(post, "岗位");
        
        EntityValidationUtil.validateStatusEquals(post, InternshipPostStatus.PUBLISHED.getCode(), 
                "岗位", "岗位未发布，无法申请");
        
        if (!post.getEnterpriseId().equals(apply.getEnterpriseId())) {
            throw new BusinessException("岗位与企业不匹配");
        }
    }
    
    /**
     * 验证并设置实习计划
     */
    private void validateAndSetPlan(InternshipApply apply, Student student) {
        if (apply.getPlanId() != null) {
            validatePlan(apply, student);
            setPlanDates(apply);
        } else {
            checkAvailablePlans(apply, student);
        }
    }
    
    /**
     * 验证实习计划
     */
    private void validatePlan(InternshipApply apply, Student student) {
        InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        EntityValidationUtil.validateStatusEquals(plan, InternshipPlanStatus.PUBLISHED.getCode(), 
                "实习计划", "只能选择已发布的实习计划");
        
        validatePlanOrgMatch(plan, student);
        validatePlanDateRange(apply, plan);
    }
    
    /**
     * 验证计划组织架构匹配
     */
    private void validatePlanOrgMatch(InternshipPlan plan, Student student) {
        if (!plan.getSchoolId().equals(student.getSchoolId())) {
            throw new BusinessException("实习计划与学生的学校不匹配");
        }
        if (plan.getCollegeId() != null && !plan.getCollegeId().equals(student.getCollegeId())) {
            throw new BusinessException("实习计划与学生的学院不匹配");
        }
        if (plan.getMajorId() != null && !plan.getMajorId().equals(student.getMajorId())) {
            throw new BusinessException("实习计划与学生的专业不匹配");
        }
    }
    
    /**
     * 验证计划时间范围
     */
    private void validatePlanDateRange(InternshipApply apply, InternshipPlan plan) {
        if (apply.getInternshipStartDate() != null 
                && apply.getInternshipStartDate().isBefore(plan.getStartDate())) {
            throw new BusinessException("实习开始日期不能早于计划的开始日期");
        }
        if (apply.getInternshipEndDate() != null 
                && apply.getInternshipEndDate().isAfter(plan.getEndDate())) {
            throw new BusinessException("实习结束日期不能晚于计划的结束日期");
        }
    }
    
    /**
     * 设置计划日期
     */
    private void setPlanDates(InternshipApply apply) {
        InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
        if (apply.getInternshipStartDate() == null) {
            apply.setInternshipStartDate(plan.getStartDate());
        }
        if (apply.getInternshipEndDate() == null) {
            apply.setInternshipEndDate(plan.getEndDate());
        }
    }
    
    /**
     * 检查可用计划
     */
    private void checkAvailablePlans(InternshipApply apply, Student student) {
        List<InternshipPlan> availablePlans = internshipPlanService.getAvailablePlansForStudent(student.getStudentId());
        if (!availablePlans.isEmpty()) {
            throw new BusinessException("请选择实习计划");
        }
    }
    
    /**
     * 检查重复申请
     */
    private void checkDuplicateApply(InternshipApply apply, Student student) {
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipApply::getStudentId, student.getStudentId())
               .eq(InternshipApply::getApplyType, ApplyType.COOPERATION.getCode())
               .eq(InternshipApply::getEnterpriseId, apply.getEnterpriseId())
               .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .in(InternshipApply::getStatus, 
                       InternshipApplyStatus.PENDING.getCode(), 
                       InternshipApplyStatus.APPROVED.getCode(), 
                       InternshipApplyStatus.ACCEPTED.getCode());
        if (apply.getPostId() != null) {
            wrapper.eq(InternshipApply::getPostId, apply.getPostId());
        }
        InternshipApply existApply = this.getOne(wrapper);
        if (existApply != null) {
            throw new BusinessException("已申请过该企业" + (apply.getPostId() != null ? "岗位" : ""));
        }
    }
    
    /**
     * 设置申请信息并保存
     */
    private void setApplyInfoAndSave(InternshipApply apply, Student student, UserInfo user, Integer applyType) {
        apply.setStudentId(student.getStudentId());
        apply.setUserId(user.getUserId());
        apply.setApplyType(applyType);
        apply.setStatus(InternshipApplyStatus.PENDING.getCode());
        EntityDefaultValueUtil.setDefaultValues(apply);
        this.save(apply);
    }
    
    /**
     * 填充计划信息
     */
    private void fillPlanInfo(InternshipApply apply) {
        if (apply.getPlanId() == null) {
            return;
        }
        
        InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
        if (plan != null) {
            apply.setPlanName(plan.getPlanName());
            apply.setPlanCode(plan.getPlanCode());
        }
    }
    
    /**
     * 更新岗位申请人数
     */
    private void updatePostAppliedCount(InternshipApply apply) {
        if (apply.getPostId() == null) {
            return;
        }
        
        InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
        if (post != null) {
            post.setAppliedCount((post.getAppliedCount() == null ? 0 : post.getAppliedCount()) + 1);
            internshipPostMapper.updateById(post);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply addSelfApply(InternshipApply apply) {
        // 参数校验
        validateSelfApplyParams(apply);
        
        // 获取当前登录学生信息
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        Student student = getCurrentStudent(user);
        
        // 验证实习计划
        validateAndSetSelfPlan(apply, student);
        
        // 设置申请信息并保存
        setSelfApplyInfoAndSave(apply, student, user);
        
        // 填充计划信息
        fillPlanInfo(apply);
        
        return apply;
    }
    
    /**
     * 验证自主实习申请参数
     */
    private void validateSelfApplyParams(InternshipApply apply) {
        EntityValidationUtil.validateStringNotBlank(apply.getSelfEnterpriseName(), "企业名称");
        EntityValidationUtil.validateStringNotBlank(apply.getSelfEnterpriseAddress(), "企业地址");
        EntityValidationUtil.validateStringNotBlank(apply.getSelfContactPerson(), "联系人");
        EntityValidationUtil.validateStringNotBlank(apply.getSelfContactPhone(), "联系电话");
        EntityValidationUtil.validateStringNotBlank(apply.getSelfPostName(), "岗位名称");
        
        if (apply.getSelfStartDate() == null || apply.getSelfEndDate() == null) {
            throw new BusinessException("实习开始日期和结束日期不能为空");
        }
        DateValidationUtil.validateDateRange(apply.getSelfStartDate(), apply.getSelfEndDate(), "实习申请");
    }
    
    /**
     * 验证并设置自主实习计划
     */
    private void validateAndSetSelfPlan(InternshipApply apply, Student student) {
        if (apply.getPlanId() != null) {
            validateSelfPlan(apply, student);
        } else {
            checkAvailablePlans(apply, student);
        }
    }
    
    /**
     * 验证自主实习计划
     */
    private void validateSelfPlan(InternshipApply apply, Student student) {
        InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
        EntityValidationUtil.validateEntityExists(plan, "实习计划");
        
        EntityValidationUtil.validateStatusEquals(plan, InternshipPlanStatus.PUBLISHED.getCode(), 
                "实习计划", "只能选择已发布的实习计划");
        
        validatePlanOrgMatch(plan, student);
        validateSelfPlanDateRange(apply, plan);
    }
    
    /**
     * 验证自主实习计划时间范围
     */
    private void validateSelfPlanDateRange(InternshipApply apply, InternshipPlan plan) {
        if (apply.getSelfStartDate() != null 
                && apply.getSelfStartDate().isBefore(plan.getStartDate())) {
            throw new BusinessException("实习开始日期不能早于计划的开始日期");
        }
        if (apply.getSelfEndDate() != null 
                && apply.getSelfEndDate().isAfter(plan.getEndDate())) {
            throw new BusinessException("实习结束日期不能晚于计划的结束日期");
        }
    }
    
    /**
     * 设置自主实习申请信息并保存
     */
    private void setSelfApplyInfoAndSave(InternshipApply apply, Student student, UserInfo user) {
        setApplyInfoAndSave(apply, student, user, ApplyType.SELF.getCode());
        
        // 清空合作企业相关字段
        clearCooperationFields(apply);
        
        // 映射自主实习时间字段
        apply.setInternshipStartDate(apply.getSelfStartDate());
        apply.setInternshipEndDate(apply.getSelfEndDate());
    }
    
    /**
     * 清空合作企业相关字段
     */
    private void clearCooperationFields(InternshipApply apply) {
        apply.setEnterpriseId(null);
        apply.setPostId(null);
        apply.setEnterpriseFeedback(null);
        apply.setEnterpriseFeedbackTime(null);
        apply.setInterviewTime(null);
        apply.setInterviewLocation(null);
        apply.setInterviewResult(null);
        apply.setInterviewComment(null);
        apply.setAcceptTime(null);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply updateApply(InternshipApply apply) {
        if (apply.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        // 检查申请是否存在
        InternshipApply existApply = this.getById(apply.getApplyId());
        EntityValidationUtil.validateEntityExists(existApply, "申请");
        
        // 只有待审核状态才能修改
        EntityValidationUtil.validateStatusEquals(existApply, InternshipApplyStatus.PENDING.getCode(), "申请", "只有待审核状态的申请才能修改");
        
        // 数据权限：学生只能修改自己的申请
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null && !user.getUserId().equals(existApply.getUserId())) {
            throw new BusinessException("无权修改该申请");
        }
        
        // 更新
        this.updateById(apply);
        return this.getById(apply.getApplyId());
    }
    
    @Override
    public InternshipApply getApplyById(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 填充关联字段
        fillApplyRelatedFields(apply);
        
        // 数据权限检查
        checkApplyPermission(apply);
        
        // 构建状态流转历史和下一步操作提示
        buildStatusHistory(apply);
        buildNextActionTip(apply);
        
        return apply;
    }
    
    /**
     * 检查申请查看权限
     */
    private void checkApplyPermission(InternshipApply apply) {
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return;
        }
        
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 系统管理员和学校管理员可以查看所有申请
        if (hasAdminRole(roleCodes)) {
            return;
        }
        
        // 学生只能查看自己的申请
        if (isStudentOwnApply(apply, user)) {
            return;
        }
        
        // 企业管理员只能查看自己企业的申请
        if (isEnterpriseAdminOwnApply(apply)) {
            return;
        }
        
        // 班主任和学院负责人可以查看自己管理的学生的申请
        if (isTeacherOwnStudentApply(apply, roleCodes)) {
            return;
        }
        
        // 如果没有匹配的权限，抛出异常
        throw new BusinessException("无权查看该申请");
    }
    
    /**
     * 检查是否为管理员角色
     */
    private boolean hasAdminRole(List<String> roleCodes) {
        return roleCodes != null && (roleCodes.contains(Constants.ROLE_SYSTEM_ADMIN) || roleCodes.contains(Constants.ROLE_SCHOOL_ADMIN));
    }
    
    /**
     * 检查是否为学生自己的申请
     */
    private boolean isStudentOwnApply(InternshipApply apply, UserInfo user) {
        return apply.getUserId().equals(user.getUserId());
    }
    
    /**
     * 检查是否为企业管理员自己的企业申请
     */
    private boolean isEnterpriseAdminOwnApply(InternshipApply apply) {
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        return currentUserEnterpriseId != null && apply.getEnterpriseId() != null
                && currentUserEnterpriseId.equals(apply.getEnterpriseId());
    }
    
    /**
     * 检查是否为班主任/学院负责人管理的学生的申请
     */
    private boolean isTeacherOwnStudentApply(InternshipApply apply, List<String> roleCodes) {
        if (roleCodes == null || (!roleCodes.contains(Constants.ROLE_CLASS_TEACHER) && !roleCodes.contains(Constants.ROLE_COLLEGE_LEADER))) {
            return false;
        }
        
        // 检查申请的学生是否属于当前用户管理的班级
        if (apply.getStudentId() == null) {
            return false;
        }
        
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student == null || student.getClassId() == null) {
            return false;
        }
        
        List<Long> managedClassIds = dataPermissionUtil.getCurrentUserClassIds();
        return managedClassIds != null && !managedClassIds.isEmpty() 
                && managedClassIds.contains(student.getClassId());
    }
    
    /**
     * 填充申请关联字段
     */
    private void fillApplyRelatedFields(InternshipApply apply) {
        // 填充学生信息
        if (apply.getStudentId() != null) {
            Student student = studentMapper.selectById(apply.getStudentId());
            if (student != null) {
                apply.setStudentNo(student.getStudentNo());
                // 设置学生实习状态
                apply.setStudentInternshipStatus(student.getInternshipStatus());
                // 获取用户信息
                if (student.getUserId() != null) {
                    UserInfo user = userMapper.selectById(student.getUserId());
                    if (user != null) {
                        apply.setStudentName(user.getRealName());
                    }
                }
            }
        }
        
        // 填充企业信息
        if (apply.getEnterpriseId() != null) {
            // 合作企业申请，从企业表获取企业信息
            Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
            if (enterprise != null) {
                apply.setEnterpriseName(enterprise.getEnterpriseName());
                apply.setEnterpriseAddress(enterprise.getAddress());
                apply.setContactPerson(enterprise.getContactPerson());
                apply.setContactPhone(enterprise.getContactPhone());
            }
        } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
            // 自主实习，使用自主实习企业信息（学生申请时填写）
            apply.setEnterpriseName(apply.getSelfEnterpriseName());
            apply.setEnterpriseAddress(apply.getSelfEnterpriseAddress());
            apply.setContactPerson(apply.getSelfContactPerson());
            apply.setContactPhone(apply.getSelfContactPhone());
        }
        
        // 填充岗位信息
        if (apply.getPostId() != null) {
            // 合作企业申请，从岗位表获取岗位信息
            InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
            if (post != null) {
                apply.setPostName(post.getPostName());
            }
        } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
            // 自主实习，使用自主实习岗位名称（学生申请时填写）
            apply.setPostName(apply.getSelfPostName());
        }
        
        // 填充审核人信息
        if (apply.getAuditUserId() != null) {
            UserInfo auditor = userMapper.selectById(apply.getAuditUserId());
            if (auditor != null) {
                apply.setAuditorName(auditor.getRealName());
            }
        }
        
        // 填充企业导师信息
        if (apply.getMentorId() != null) {
            EnterpriseMentor mentor = enterpriseMentorMapper.selectById(apply.getMentorId());
            if (mentor != null) {
                apply.setMentorName(mentor.getMentorName());
            }
        }
        
        // 新增：填充实习计划信息
        if (apply.getPlanId() != null) {
            InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
            if (plan != null) {
                apply.setPlanName(plan.getPlanName());
                apply.setPlanCode(plan.getPlanCode());
            }
        }
        
        // 填充面试信息和状态文本
        fillInterviewInfoAndStatusText(apply);
    }
    
    /**
     * 填充面试信息和生成状态文本
     */
    private void fillInterviewInfoAndStatusText(InternshipApply apply) {
        // 查询面试记录（仅合作企业申请）
        if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
            List<Interview> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                    .eq(Interview::getApplyId, apply.getApplyId())
                    .eq(Interview::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    .orderByDesc(Interview::getCreateTime)
            );
            
            if (interviews != null && !interviews.isEmpty()) {
                apply.setHasInterview(true);
                apply.setLatestInterview(interviews.get(0));
            } else {
                apply.setHasInterview(false);
                apply.setLatestInterview(null);
            }
        } else {
            apply.setHasInterview(false);
            apply.setLatestInterview(null);
        }
        
        // 生成状态文本
        apply.setStatusText(buildStatusText(apply));
    }
    
    /**
     * 构建状态文本（根据申请状态和面试状态动态生成）
     */
    private String buildStatusText(InternshipApply apply) {
        if (apply.getStatus() == null) {
            return "状态异常";
        }
        
        // 自主实习申请
        if (isSelfApply(apply)) {
            return buildSelfApplyStatusText(apply);
        }
        
        // 合作企业申请
        if (isCooperationApply(apply)) {
            return buildCooperationApplyStatusText(apply);
        }
        
        return "未知状态";
    }
    
    /**
     * 判断是否为自主实习申请
     */
    private boolean isSelfApply(InternshipApply apply) {
        return apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode());
    }
    
    /**
     * 判断是否为合作企业申请
     */
    private boolean isCooperationApply(InternshipApply apply) {
        return apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode());
    }
    
    /**
     * 构建自主实习申请状态文本
     */
    private String buildSelfApplyStatusText(InternshipApply apply) {
        Integer status = apply.getStatus();
        if (status == null) {
            return "未知状态";
        }
        
        if (status.equals(InternshipApplyStatus.PENDING.getCode())) {
            return "等待学校审核";
        } else if (status.equals(InternshipApplyStatus.APPROVED.getCode())) {
            return "学校审核通过";
        } else if (status.equals(InternshipApplyStatus.REJECTED.getCode())) {
            return "学校审核拒绝";
        } else if (status.equals(InternshipApplyStatus.RESIGNED.getCode())) {
            return "已离职";
        }
        
        return "未知状态";
    }
    
    /**
     * 构建合作企业申请状态文本
     */
    private String buildCooperationApplyStatusText(InternshipApply apply) {
        Integer status = apply.getStatus();
        if (status == null) {
            return "未知状态";
        }
        
        if (status.equals(InternshipApplyStatus.PENDING.getCode())) {
            return "等待学校审核";
        } else if (status.equals(InternshipApplyStatus.APPROVED.getCode())) {
            return buildApprovedStatusText(apply);
        } else if (status.equals(InternshipApplyStatus.REJECTED.getCode())) {
            return "学校审核拒绝";
        } else if (status.equals(InternshipApplyStatus.ACCEPTED.getCode())) {
            return "已录用";
        } else if (status.equals(InternshipApplyStatus.REJECTED_ACCEPTANCE.getCode())) {
            return "已拒绝录用";
        } else if (status.equals(InternshipApplyStatus.RESIGNED.getCode())) {
            return "已离职";
        }
        
        return "未知状态";
    }
    
    /**
     * 构建已审核通过状态文本（根据面试情况）
     */
    private String buildApprovedStatusText(InternshipApply apply) {
        if (!hasValidInterview(apply)) {
            return "等待企业处理";
        }
        
        Interview interview = apply.getLatestInterview();
        return buildInterviewStatusText(interview);
    }
    
    /**
     * 判断是否有有效的面试信息
     */
    private boolean hasValidInterview(InternshipApply apply) {
        return apply.getHasInterview() != null 
                && apply.getHasInterview() 
                && apply.getLatestInterview() != null;
    }
    
    /**
     * 构建面试状态文本
     */
    private String buildInterviewStatusText(Interview interview) {
        // 学生未确认面试
        if (interview.getStudentConfirm() == null 
                || interview.getStudentConfirm().equals(StudentConfirmStatus.NOT_CONFIRMED.getCode())) {
            return "等待学生确认面试";
        }
        
        // 学生已拒绝面试
        if (interview.getStudentConfirm().equals(StudentConfirmStatus.REJECTED.getCode())) {
            return "学生已拒绝面试";
        }
        
        // 学生已确认面试
        if (interview.getStudentConfirm().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            return buildConfirmedInterviewStatusText(interview);
        }
        
        return "等待企业处理";
    }
    
    /**
     * 构建已确认面试的状态文本
     */
    private String buildConfirmedInterviewStatusText(Interview interview) {
        if (interview.getStatus() == null || interview.getStatus().equals(InterviewStatus.CONFIRMED.getCode())) {
            return "面试已确认，等待面试";
        }
        
        if (interview.getStatus().equals(InterviewStatus.CANCELLED.getCode())) {
            return "面试已取消";
        }
        
        if (interview.getStatus().equals(InterviewStatus.COMPLETED.getCode())) {
            return buildCompletedInterviewStatusText(interview);
        }
        
        return "等待企业处理";
    }
    
    /**
     * 构建已完成面试的状态文本
     */
    private String buildCompletedInterviewStatusText(Interview interview) {
        if (interview.getInterviewResult() == null) {
            return "面试已完成，等待结果";
        }
        
        if (interview.getInterviewResult().equals(InterviewResult.PASSED.getCode())) {
            return "面试通过，等待企业决定";
        } else if (interview.getInterviewResult().equals(InterviewResult.FAILED.getCode())) {
            return "面试未通过";
        }
        
        return "面试待定";
    }
    
    /**
     * 构建状态流转历史
     */
    private void buildStatusHistory(InternshipApply apply) {
        List<InternshipApply.StatusHistoryItem> history = new ArrayList<>();
        
        // 自主实习申请：简化流程
        if (isSelfApply(apply)) {
            buildSelfApplyStatusHistory(apply, history);
            apply.setStatusHistory(history);
            return;
        }
        
        // 合作企业申请：全流程状态
        if (isCooperationApply(apply)) {
            buildCooperationApplyStatusHistory(apply, history);
        }
        
        // 按时间排序
        sortHistoryByTime(history);
        apply.setStatusHistory(history);
    }
    
    /**
     * 构建自主实习申请状态流转历史
     */
    private void buildSelfApplyStatusHistory(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        addSubmitHistoryItem(apply, history, "学生提交了自主实习申请");
        addAuditHistoryItem(apply, history, "学校审核通过", "学校审核拒绝");
        addCancelledHistoryItem(apply, history);
    }
    
    /**
     * 构建合作企业申请状态流转历史
     */
    private void buildCooperationApplyStatusHistory(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        addSubmitHistoryItem(apply, history, "学生提交了实习申请");
        addCooperationAuditHistoryItem(apply, history);
        addEnterpriseFeedbackHistoryItem(apply, history);
        addInterviewHistoryItems(apply, history);
        addAcceptHistoryItem(apply, history);
    }
    
    /**
     * 添加申请提交历史项
     */
    private void addSubmitHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history, String description) {
        if (apply.getCreateTime() == null) {
            return;
        }
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                "申请提交",
                apply.getCreateTime(),
                apply.getStudentName(),
                description,
                InternshipApplyStatus.PENDING.getCode()
        );
        history.add(item);
    }
    
    /**
     * 添加审核历史项（自主实习）
     */
    private void addAuditHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history, 
                                     String approvedDesc, String rejectedDesc) {
        if (apply.getAuditTime() == null) {
            return;
        }
        
        // 判断是否审核通过：APPROVED（已通过）、ACCEPTED（已录用）、REJECTED_ACCEPTANCE（已拒绝录用）都表示审核通过
        // 只有REJECTED（已拒绝）表示审核被拒绝
        Integer status = apply.getStatus();
        boolean isApproved = status != null && (
                status.equals(InternshipApplyStatus.APPROVED.getCode()) ||
                status.equals(InternshipApplyStatus.ACCEPTED.getCode()) ||
                status.equals(InternshipApplyStatus.REJECTED_ACCEPTANCE.getCode())
        );
        String actionName = isApproved ? "学校审核通过" : "学校审核拒绝";
        String description = apply.getAuditOpinion() != null 
                ? apply.getAuditOpinion() 
                : (isApproved ? approvedDesc : rejectedDesc);
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                actionName,
                apply.getAuditTime(),
                apply.getAuditorName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 添加审核历史项（合作企业）
     */
    private void addCooperationAuditHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        if (apply.getAuditTime() == null) {
            return;
        }
        
        // 判断是否审核通过：APPROVED（已通过）、ACCEPTED（已录用）、REJECTED_ACCEPTANCE（已拒绝录用）都表示审核通过
        // 只有REJECTED（已拒绝）表示审核被拒绝
        Integer status = apply.getStatus();
        boolean isApproved = status != null && (
                status.equals(InternshipApplyStatus.APPROVED.getCode()) ||
                status.equals(InternshipApplyStatus.ACCEPTED.getCode()) ||
                status.equals(InternshipApplyStatus.REJECTED_ACCEPTANCE.getCode())
        );
        String actionName = isApproved ? "学校审核通过" : "学校审核拒绝";
        String description = apply.getAuditOpinion() != null 
                ? apply.getAuditOpinion() 
                : (isApproved ? "学校审核通过，等待企业处理" : "学校审核拒绝");
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                actionName,
                apply.getAuditTime(),
                apply.getAuditorName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 添加取消历史项
     */
    private void addCancelledHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        if (apply.getStatus() == null || !apply.getStatus().equals(InternshipApplyStatus.CANCELLED.getCode())) {
            return;
        }
        
        LocalDateTime actionTime = apply.getUpdateTime() != null ? apply.getUpdateTime() : apply.getCreateTime();
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                "申请已取消",
                actionTime,
                apply.getStudentName(),
                "学生取消了申请",
                InternshipApplyStatus.CANCELLED.getCode()
        );
        history.add(item);
    }
    
    /**
     * 添加企业反馈历史项
     */
    private void addEnterpriseFeedbackHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        if (apply.getEnterpriseFeedbackTime() == null) {
            return;
        }
        
        String description = apply.getEnterpriseFeedback() != null 
                ? apply.getEnterpriseFeedback() 
                : "企业已查看申请";
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                "企业反馈",
                apply.getEnterpriseFeedbackTime(),
                apply.getEnterpriseName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 添加面试相关历史项
     */
    private void addInterviewHistoryItems(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        List<Interview> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                        .eq(Interview::getApplyId, apply.getApplyId())
                        .eq(Interview::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .orderByAsc(Interview::getCreateTime)
        );
        
        if (EntityValidationUtil.isEmpty(interviews)) {
            return;
        }
        
        for (Interview interview : interviews) {
            addInterviewArrangementHistoryItem(apply, history, interview);
            addStudentConfirmHistoryItem(apply, history, interview);
            addInterviewCompletedHistoryItem(apply, history, interview);
        }
    }
    
    /**
     * 添加面试安排历史项
     */
    private void addInterviewArrangementHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history, Interview interview) {
        if (interview.getCreateTime() == null) {
            return;
        }
        
        String interviewTypeText = getInterviewTypeText(interview.getInterviewType());
        String interviewTime = interview.getInterviewTime() != null ? interview.getInterviewTime().toString() : "待定";
        String description = "企业安排了" + interviewTypeText + "，面试时间：" + interviewTime;
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                "面试安排",
                interview.getCreateTime(),
                apply.getEnterpriseName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 添加学生确认面试历史项
     */
    private void addStudentConfirmHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history, Interview interview) {
        if (interview.getStudentConfirmTime() == null) {
            return;
        }
        
        boolean isConfirmed = interview.getStudentConfirm().equals(StudentConfirmStatus.CONFIRMED.getCode());
        String actionName = isConfirmed ? "学生确认面试" : "学生拒绝面试";
        String description = isConfirmed ? "学生已确认参加面试" : "学生已拒绝面试";
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                actionName,
                interview.getStudentConfirmTime(),
                apply.getStudentName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 添加面试完成历史项
     */
    private void addInterviewCompletedHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history, Interview interview) {
        if (interview.getInterviewFeedbackTime() == null) {
            return;
        }
        
        String resultText = getInterviewResultText(interview.getInterviewResult());
        String description = "面试结果：" + resultText;
        if (interview.getInterviewComment() != null) {
            description += "，" + interview.getInterviewComment();
        }
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                "面试完成",
                interview.getInterviewFeedbackTime(),
                apply.getEnterpriseName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 添加录用/拒绝历史项
     */
    private void addAcceptHistoryItem(InternshipApply apply, List<InternshipApply.StatusHistoryItem> history) {
        if (apply.getAcceptTime() == null) {
            return;
        }
        
        boolean isAccepted = apply.getStatus().equals(InternshipApplyStatus.ACCEPTED.getCode());
        String actionName = isAccepted ? "企业录用" : "企业拒绝";
        String description = isAccepted ? "企业已录用" : "企业已拒绝录用";
        
        InternshipApply.StatusHistoryItem item = createHistoryItem(
                actionName,
                apply.getAcceptTime(),
                apply.getEnterpriseName(),
                description,
                apply.getStatus()
        );
        history.add(item);
    }
    
    /**
     * 创建历史项
     */
    private InternshipApply.StatusHistoryItem createHistoryItem(String actionName, LocalDateTime actionTime, 
                                                                 String operator, String description, Integer status) {
        InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
        item.setActionName(actionName);
        item.setActionTime(actionTime);
        item.setOperator(operator);
        item.setDescription(description);
        item.setStatus(status);
        return item;
    }
    
    /**
     * 获取面试类型文本
     */
    private String getInterviewTypeText(Integer interviewType) {
        if (interviewType == null) {
            return "面试";
        }
        if (interviewType.equals(InterviewType.ON_SITE.getCode())) {
            return "现场面试";
        } else if (interviewType.equals(InterviewType.VIDEO.getCode())) {
            return "视频面试";
        } else {
            return "电话面试";
        }
    }
    
    /**
     * 获取面试结果文本
     */
    private String getInterviewResultText(Integer result) {
        if (result == null) {
            return "待定";
        }
        if (result.equals(InterviewResult.PASSED.getCode())) {
            return "通过";
        } else if (result.equals(InterviewResult.FAILED.getCode())) {
            return "不通过";
        }
        return "待定";
    }
    
    /**
     * 按时间排序历史项
     */
    private void sortHistoryByTime(List<InternshipApply.StatusHistoryItem> history) {
        history.sort((a, b) -> {
            if (a.getActionTime() == null && b.getActionTime() == null) {
                return 0;
            }
            if (a.getActionTime() == null) {
                return 1;
            }
            if (b.getActionTime() == null) {
                return -1;
            }
            return a.getActionTime().compareTo(b.getActionTime());
        });
    }
    
    /**
     * 构建下一步操作提示
     */
    private void buildNextActionTip(InternshipApply apply) {
        if (apply.getStatus() == null) {
            apply.setNextActionTip("状态异常，请联系管理员");
            return;
        }
        
        // 自主实习申请
        if (isSelfApply(apply)) {
            buildSelfApplyNextActionTip(apply);
            return;
        }
        
        // 合作企业申请
        if (isCooperationApply(apply)) {
            buildCooperationApplyNextActionTip(apply);
        }
    }
    
    /**
     * 构建自主实习申请下一步操作提示
     */
    private void buildSelfApplyNextActionTip(InternshipApply apply) {
        Integer status = apply.getStatus();
        if (status.equals(InternshipApplyStatus.PENDING.getCode())) {
            apply.setNextActionTip("等待学校审核，请耐心等待");
        } else if (status.equals(InternshipApplyStatus.APPROVED.getCode())) {
            apply.setNextActionTip("学校审核已通过，可以开始实习");
        } else if (status.equals(InternshipApplyStatus.REJECTED.getCode())) {
            apply.setNextActionTip("学校审核已拒绝，如有疑问请联系班主任");
        }
    }
    
    /**
     * 构建合作企业申请下一步操作提示
     */
    private void buildCooperationApplyNextActionTip(InternshipApply apply) {
        Integer status = apply.getStatus();
        if (status.equals(InternshipApplyStatus.PENDING.getCode())) {
            apply.setNextActionTip("等待学校审核，请耐心等待");
        } else if (status.equals(InternshipApplyStatus.APPROVED.getCode())) {
            buildApprovedNextActionTip(apply);
        } else if (status.equals(InternshipApplyStatus.ACCEPTED.getCode())) {
            apply.setNextActionTip("已录用，恭喜！请与企业联系确认实习安排");
        } else if (status.equals(InternshipApplyStatus.REJECTED_ACCEPTANCE.getCode())) {
            apply.setNextActionTip("已拒绝录用，如有疑问请联系企业");
        } else if (status.equals(InternshipApplyStatus.REJECTED.getCode())) {
            apply.setNextActionTip("学校审核已拒绝，如有疑问请联系班主任");
        }
    }
    
    /**
     * 构建已审核通过状态的下一步操作提示
     */
    private void buildApprovedNextActionTip(InternshipApply apply) {
        if (!hasValidInterview(apply)) {
            apply.setNextActionTip("等待企业处理，企业可以安排面试或直接录用/拒绝");
            return;
        }
        
        Interview latestInterview = apply.getLatestInterview();
        buildInterviewNextActionTip(apply, latestInterview);
    }
    
    /**
     * 构建面试相关的下一步操作提示
     */
    private void buildInterviewNextActionTip(InternshipApply apply, Interview interview) {
        // 学生未确认面试
        if (interview.getStudentConfirm() == null 
                || interview.getStudentConfirm().equals(StudentConfirmStatus.NOT_CONFIRMED.getCode())) {
            apply.setNextActionTip("企业已安排面试，请前往\"我的面试\"页面确认是否参加");
            return;
        }
        
        // 学生已拒绝面试
        if (interview.getStudentConfirm().equals(StudentConfirmStatus.REJECTED.getCode())) {
            apply.setNextActionTip("已拒绝面试，申请流程已结束");
            return;
        }
        
        // 学生已确认面试
        if (interview.getStudentConfirm().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            buildConfirmedInterviewNextActionTip(apply, interview);
        }
    }
    
    /**
     * 构建已确认面试的下一步操作提示
     */
    private void buildConfirmedInterviewNextActionTip(InternshipApply apply, Interview interview) {
        if (interview.getStatus().equals(InterviewStatus.CONFIRMED.getCode())) {
            apply.setNextActionTip("面试已确认，请按时参加面试");
            return;
        }
        
        if (interview.getStatus().equals(InterviewStatus.CANCELLED.getCode())) {
            apply.setNextActionTip("面试已取消，等待企业重新安排");
            return;
        }
        
        if (interview.getStatus().equals(InterviewStatus.COMPLETED.getCode())) {
            buildCompletedInterviewNextActionTip(apply, interview);
        }
    }
    
    /**
     * 构建已完成面试的下一步操作提示
     */
    private void buildCompletedInterviewNextActionTip(InternshipApply apply, Interview interview) {
        if (interview.getInterviewResult() == null) {
            apply.setNextActionTip("面试已完成，等待企业反馈结果");
        } else if (interview.getInterviewResult().equals(InterviewResult.PASSED.getCode())) {
            apply.setNextActionTip("面试已通过，等待企业决定是否录用");
        } else if (interview.getInterviewResult().equals(InterviewResult.FAILED.getCode())) {
            apply.setNextActionTip("面试未通过，如有疑问请联系企业");
        } else {
            apply.setNextActionTip("面试待定，等待企业决定");
        }
    }
    
    @Override
    public Page<InternshipApply> getApplyPage(Page<InternshipApply> page, InternshipApplyQueryDTO queryDTO) {
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipApply::getDeleteFlag);
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (queryDTO != null) {
            if (queryDTO.getStudentId() != null) {
                wrapper.eq(InternshipApply::getStudentId, queryDTO.getStudentId());
            }
            if (queryDTO.getEnterpriseId() != null) {
                wrapper.eq(InternshipApply::getEnterpriseId, queryDTO.getEnterpriseId());
            }
            if (queryDTO.getPostId() != null) {
                wrapper.eq(InternshipApply::getPostId, queryDTO.getPostId());
            }
            if (queryDTO.getApplyType() != null) {
                wrapper.eq(InternshipApply::getApplyType, queryDTO.getApplyType());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(InternshipApply::getStatus, queryDTO.getStatus());
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipApply::getCreateTime);
        
        Page<InternshipApply> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (InternshipApply apply : result.getRecords()) {
                fillApplyRelatedFields(apply);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditApply(Long applyId, AuditApplyDTO auditDTO) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        if (auditDTO == null || auditDTO.getAuditStatus() == null) {
            throw new BusinessException("审核信息不能为空");
        }
        
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        EntityValidationUtil.validateStatusEquals(apply, InternshipApplyStatus.PENDING.getCode(), 
                "申请", "只有待审核状态的申请才能审核");
        
        // 设置审核信息
        AuditUtil.setAuditInfo(apply, auditDTO.getAuditStatus().getCode(), auditDTO.getAuditOpinion(), userMapper);
        
        // 如果是自主实习且审核通过，需要创建企业并绑定
        if (isSelfApplyApproved(apply, auditDTO.getAuditStatus())) {
            processSelfApplyApproved(apply);
        }
        
        return this.updateById(apply);
    }
    
    /**
     * 判断是否为自主实习且审核通过
     */
    private boolean isSelfApplyApproved(InternshipApply apply, AuditStatus auditStatus) {
        return apply.getApplyType() != null 
                && apply.getApplyType().equals(ApplyType.SELF.getCode())
                && auditStatus == AuditStatus.APPROVED;
    }
    
    
    /**
     * 处理自主实习审核通过
     */
    private void processSelfApplyApproved(InternshipApply apply) {
        Long enterpriseId = findOrCreateEnterprise(apply);
        apply.setEnterpriseId(enterpriseId);
        apply.setStatus(InternshipApplyStatus.ACCEPTED.getCode());
        apply.setAcceptTime(LocalDateTime.now());
    }
    
    /**
     * 查找或创建企业
     */
    private Long findOrCreateEnterprise(InternshipApply apply) {
        Enterprise existingEnterprise = findExistingEnterprise(apply.getSelfEnterpriseName());
        if (existingEnterprise != null) {
            return existingEnterprise.getEnterpriseId();
        }
        return createSelfEnterprise(apply);
    }
    
    /**
     * 查找已存在的企业
     */
    private Enterprise findExistingEnterprise(String enterpriseName) {
        return enterpriseMapper.selectOne(
                new LambdaQueryWrapper<Enterprise>()
                        .eq(Enterprise::getEnterpriseName, enterpriseName)
                        .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .last("LIMIT 1")
        );
    }
    
    /**
     * 创建自主实习企业
     */
    private Long createSelfEnterprise(InternshipApply apply) {
        Enterprise newEnterprise = new Enterprise();
        newEnterprise.setEnterpriseName(apply.getSelfEnterpriseName());
        
        if (StringUtils.hasText(apply.getSelfEnterpriseAddress())) {
            newEnterprise.setAddress(apply.getSelfEnterpriseAddress());
        }
        if (StringUtils.hasText(apply.getSelfContactPerson())) {
            newEnterprise.setContactPerson(apply.getSelfContactPerson());
        }
        if (StringUtils.hasText(apply.getSelfContactPhone())) {
            newEnterprise.setContactPhone(apply.getSelfContactPhone());
        }
        
        newEnterprise.setAuditStatus(AuditStatus.APPROVED.getCode());
        newEnterprise.setStatus(UserStatus.ENABLED.getCode());
        newEnterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        newEnterprise.setEnterpriseCode("SELF_" + System.currentTimeMillis());
        
        enterpriseMapper.insert(newEnterprise);
        return newEnterprise.getEnterpriseId();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean filterApply(Long applyId, FilterApplyDTO filterDTO) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        if (filterDTO == null || filterDTO.getAction() == null) {
            throw new BusinessException("筛选信息不能为空");
        }
        
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 验证权限和状态
        validateFilterApplyPermission(apply);
        validateFilterApplyStatus(apply);
        
        // 根据操作类型更新状态
        processFilterAction(apply, filterDTO.getAction(), filterDTO.getComment());
        
        return this.updateById(apply);
    }
    
    /**
     * 验证筛选操作权限
     */
    private void validateFilterApplyPermission(InternshipApply apply) {
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权操作该申请");
        }
    }
    
    /**
     * 验证筛选操作状态
     */
    private void validateFilterApplyStatus(InternshipApply apply) {
        if (!isCooperationApply(apply)) {
            throw new BusinessException("只能对合作企业申请进行筛选操作");
        }
        
        EntityValidationUtil.validateStatusEquals(apply, InternshipApplyStatus.APPROVED.getCode(), 
                "申请", "只能处理学校审核通过的申请");
    }
    
    /**
     * 处理筛选操作
     */
    private void processFilterAction(InternshipApply apply, FilterAction action, String comment) {
        switch (action) {
            case INTERESTED: // 标记感兴趣
            case ARRANGE_INTERVIEW: // 安排面试
                setEnterpriseFeedback(apply, comment);
                break;
            case ACCEPT: // 录用
                processAcceptAction(apply, comment);
                break;
            case REJECT: // 拒绝
                processRejectAction(apply, comment);
                break;
        }
    }
    
    /**
     * 设置企业反馈
     */
    private void setEnterpriseFeedback(InternshipApply apply, String comment) {
        apply.setEnterpriseFeedback(comment);
        apply.setEnterpriseFeedbackTime(LocalDateTime.now());
    }
    
    /**
     * 处理录用操作
     */
    private void processAcceptAction(InternshipApply apply, String comment) {
        apply.setStatus(InternshipApplyStatus.ACCEPTED.getCode());
        apply.setAcceptTime(LocalDateTime.now());
        setEnterpriseFeedback(apply, comment);
        updatePostAcceptedCount(apply);
    }
    
    /**
     * 处理拒绝操作
     */
    private void processRejectAction(InternshipApply apply, String comment) {
        apply.setStatus(InternshipApplyStatus.REJECTED_ACCEPTANCE.getCode());
        setEnterpriseFeedback(apply, comment);
    }
    
    /**
     * 更新岗位录用人数
     */
    private void updatePostAcceptedCount(InternshipApply apply) {
        if (apply.getPostId() == null) {
            return;
        }
        
        InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
        if (post != null) {
            post.setAcceptedCount((post.getAcceptedCount() == null ? 0 : post.getAcceptedCount()) + 1);
            internshipPostMapper.updateById(post);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelApply(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：学生只能取消自己的申请
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null || !user.getUserId().equals(apply.getUserId())) {
            throw new BusinessException("无权取消该申请");
        }
        
        // 只有待审核状态才能取消
        EntityValidationUtil.validateStatusEquals(apply, InternshipApplyStatus.PENDING.getCode(), "申请", "只有待审核状态的申请才能取消");
        
        // 软删除 - 使用 LambdaUpdateWrapper 确保字段被更新
        boolean result = this.update(new LambdaUpdateWrapper<InternshipApply>()
                .eq(InternshipApply::getApplyId, applyId)
                .set(InternshipApply::getDeleteFlag, DeleteFlag.DELETED.getCode()));
        if (!result) {
            throw new BusinessException("取消申请失败，请重试");
        }
        
        // 更新岗位的已申请人数
        if (apply.getPostId() != null) {
            InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
            if (post != null) {
                // 重新计算已申请人数（只统计未删除且状态为待审核、已通过、已录用的申请）
                Long appliedCount = this.count(new LambdaQueryWrapper<InternshipApply>()
                        .eq(InternshipApply::getPostId, apply.getPostId())
                        .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .in(InternshipApply::getStatus, 
                                InternshipApplyStatus.PENDING.getCode(), 
                                InternshipApplyStatus.APPROVED.getCode(), 
                                InternshipApplyStatus.ACCEPTED.getCode()));
                post.setAppliedCount(appliedCount.intValue());
                internshipPostMapper.updateById(post);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApply(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：学生只能删除自己的申请
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null || !user.getUserId().equals(apply.getUserId())) {
            throw new BusinessException("无权删除该申请");
        }
        
        // 软删除
        apply.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(apply);
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<InternshipApply> wrapper) {
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            return;
        }
        
        // 学生只能查看自己的申请
        if (dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
            applyStudentFilter(wrapper, user.getUserId());
            return;
        }
        
        // 企业管理员只能查看自己企业的申请
        if (dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_ADMIN)) {
            applyEnterpriseFilter(wrapper);
            return;
        }
        
        // 学校管理员可以查看所有申请（不添加过滤条件）
        if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN)) {
            return;
        }
        
        // 学院负责人可以查看本学院所有学生的申请
        if (dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
            applyCollegeFilter(wrapper);
            return;
        }
        
        // 班主任可以查看自己管理的班级的所有学生的申请
        if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
            applyClassTeacherFilter(wrapper);
            return;
        }
    }
    
    /**
     * 应用学生过滤条件
     */
    private void applyStudentFilter(LambdaQueryWrapper<InternshipApply> wrapper, Long userId) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (student != null) {
            wrapper.eq(InternshipApply::getStudentId, student.getStudentId());
        }
    }
    
    /**
     * 应用企业过滤条件
     */
    private void applyEnterpriseFilter(LambdaQueryWrapper<InternshipApply> wrapper) {
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            wrapper.eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId);
        }
    }
    
    /**
     * 应用学院过滤条件（学院负责人可以查看本学院所有学生的申请）
     */
    private void applyCollegeFilter(LambdaQueryWrapper<InternshipApply> wrapper) {
        Long collegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (collegeId != null) {
            // 查询该学院下的所有学生ID
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getCollegeId, collegeId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getStudentId)
            );
            if (EntityValidationUtil.isNotEmpty(students)) {
                List<Long> studentIds = students.stream()
                        .map(Student::getStudentId)
                        .collect(Collectors.toList());
                wrapper.in(InternshipApply::getStudentId, studentIds);
            } else {
                // 如果没有学生，返回空结果
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
        }
    }
    
    /**
     * 应用班主任过滤条件（班主任可以查看自己管理的班级的所有学生的申请）
     */
    private void applyClassTeacherFilter(LambdaQueryWrapper<InternshipApply> wrapper) {
        List<Long> managedClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (EntityValidationUtil.isNotEmpty(managedClassIds)) {
            // 查询这些班级下的所有学生ID
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, managedClassIds)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getStudentId)
            );
            if (EntityValidationUtil.isNotEmpty(students)) {
                List<Long> studentIds = students.stream()
                        .map(Student::getStudentId)
                        .collect(Collectors.toList());
                wrapper.in(InternshipApply::getStudentId, studentIds);
            } else {
                // 如果没有学生，返回空结果
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
        } else {
            // 如果没有管理的班级，返回空结果
            wrapper.eq(InternshipApply::getApplyId, -1L);
        }
    }
    
    @Override
    public Page<InternshipApply> getEnterpriseStudents(Page<InternshipApply> page, String studentName, String studentNo, Long postId) {
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        
        QueryWrapperUtil.notDeleted(wrapper, InternshipApply::getDeleteFlag);
        wrapper.eq(InternshipApply::getApplyType, ApplyType.COOPERATION.getCode());
        wrapper.eq(InternshipApply::getStatus, InternshipApplyStatus.ACCEPTED.getCode());
        wrapper.eq(InternshipApply::getStudentConfirmStatus, StudentConfirmStatus.CONFIRMED.getCode());
        
        // 数据权限过滤
        applyEnterpriseFilterForStudents(wrapper);
        
        // 条件查询
        if (postId != null) {
            wrapper.eq(InternshipApply::getPostId, postId);
        }
        
        wrapper.orderByDesc(InternshipApply::getCreateTime);
        
        Page<InternshipApply> result = this.page(page, wrapper);
        
        // 填充关联字段并过滤
        if (EntityValidationUtil.hasRecords(result)) {
            fillAndFilterEnterpriseStudents(result, studentName, studentNo);
        }
        
        return result;
    }
    
    /**
     * 应用企业过滤条件（企业学生查询）
     */
    private void applyEnterpriseFilterForStudents(LambdaQueryWrapper<InternshipApply> wrapper) {
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            wrapper.eq(InternshipApply::getApplyId, -1L);
            return;
        }
        
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        
        // 企业导师：只能查看分配给自己的学生
        if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
            Long mentorId = dataPermissionUtil.getCurrentUserMentorId();
            if (mentorId != null) {
                wrapper.eq(InternshipApply::getMentorId, mentorId);
            } else {
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
            return;
        }
        
        // 企业管理员：查看本企业的所有学生
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            wrapper.eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId);
        } else {
            wrapper.eq(InternshipApply::getApplyId, -1L);
        }
    }
    
    /**
     * 填充关联字段并过滤企业学生
     */
    private void fillAndFilterEnterpriseStudents(Page<InternshipApply> result, String studentName, String studentNo) {
        List<InternshipApply> records = result.getRecords();
        List<InternshipApply> filteredRecords = new ArrayList<>();
        
        for (InternshipApply apply : records) {
            fillApplyRelatedFields(apply);
            
            if (matchesStudentFilter(apply, studentName, studentNo)) {
                filteredRecords.add(apply);
            }
        }
        
        result.setRecords(filteredRecords);
        result.setTotal(filteredRecords.size());
    }
    
    /**
     * 判断申请是否匹配学生过滤条件
     */
    private boolean matchesStudentFilter(InternshipApply apply, String studentName, String studentNo) {
        if (!StringUtils.hasText(studentName) && !StringUtils.hasText(studentNo)) {
            return true;
        }
        
        if (StringUtils.hasText(studentName) 
                && (apply.getStudentName() == null || !apply.getStudentName().contains(studentName))) {
            return false;
        }
        
        if (StringUtils.hasText(studentNo) 
                && (apply.getStudentNo() == null || !apply.getStudentNo().contains(studentNo))) {
            return false;
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMentor(Long applyId, Long mentorId) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        if (mentorId == null) {
            throw new BusinessException("企业导师ID不能为空");
        }
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：企业管理员只能分配本企业的导师
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权操作该申请");
        }
        
        // 检查企业导师是否存在且属于该企业
        EnterpriseMentor mentor = enterpriseMentorMapper.selectById(mentorId);
        EntityValidationUtil.validateEntityExists(mentor, "企业导师");
        if (!mentor.getEnterpriseId().equals(apply.getEnterpriseId())) {
            throw new BusinessException("企业导师不属于该企业");
        }
        
        // 更新申请的企业导师ID
        apply.setMentorId(mentorId);
        this.updateById(apply);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmOnboard(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        // 获取当前学生ID
        Long studentId = getCurrentStudentId();
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 验证权限和状态
        validateConfirmOnboardPermission(apply, studentId);
        validateConfirmOnboardStatus(apply);
        
        // 获取学生信息
        Student student = studentMapper.selectById(studentId);
        EntityValidationUtil.validateEntityExists(student, "学生信息");
        
        // 检查是否有其他已确认的申请
        checkOtherConfirmedApply(student, applyId);
        
        // 更新申请和学生信息
        updateApplyForConfirmOnboard(apply);
        updateStudentForConfirmOnboard(student, apply, applyId);
        
        return true;
    }
    
    /**
     * 获取当前学生ID
     */
    private Long getCurrentStudentId() {
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法确认上岗");
        }
        return studentId;
    }
    
    /**
     * 验证确认上岗权限
     */
    private void validateConfirmOnboardPermission(InternshipApply apply, Long studentId) {
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作该申请");
        }
    }
    
    /**
     * 验证确认上岗状态
     */
    private void validateConfirmOnboardStatus(InternshipApply apply) {
        EntityValidationUtil.validateStatusEquals(apply, InternshipApplyStatus.ACCEPTED.getCode(), 
                "申请", "只有已录用的申请才能确认上岗");
        
        // 检查是否已离职
        if (apply.getUnbindStatus() != null && apply.getUnbindStatus().equals(UnbindStatus.UNBOUND.getCode())) {
            throw new BusinessException("该申请已离职，无法确认上岗");
        }
        
        if (apply.getStudentConfirmStatus() != null 
                && !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.NOT_CONFIRMED.getCode())) {
            throw new BusinessException("该申请已经确认过了");
        }
    }
    
    /**
     * 检查是否有其他已确认的申请
     */
    private void checkOtherConfirmedApply(Student student, Long applyId) {
        if (student.getCurrentApplyId() == null || student.getCurrentApplyId().equals(applyId)) {
            return;
        }
        
        InternshipApply oldApply = this.getById(student.getCurrentApplyId());
        if (oldApply != null 
                && oldApply.getStudentConfirmStatus() != null 
                && oldApply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            throw new BusinessException("您已有其他已确认的实习申请，请先解绑后再确认新的申请");
        }
    }
    
    /**
     * 更新申请信息（确认上岗）
     */
    private void updateApplyForConfirmOnboard(InternshipApply apply) {
        apply.setStudentConfirmStatus(StudentConfirmStatus.CONFIRMED.getCode());
        apply.setStudentConfirmTime(LocalDateTime.now());
        if (apply.getInternshipStartDate() == null) {
            apply.setInternshipStartDate(java.time.LocalDate.now());
        }
        this.updateById(apply);
    }
    
    /**
     * 更新学生信息（确认上岗）
     */
    private void updateStudentForConfirmOnboard(Student student, InternshipApply apply, Long applyId) {
        student.setCurrentApplyId(applyId);
        
        if (apply.getEnterpriseId() == null) {
            throw new BusinessException("申请的企业信息不完整，无法确认上岗，请联系管理员");
        }
        
        student.setCurrentEnterpriseId(apply.getEnterpriseId());
        student.setInternshipStatus(StudentInternshipStatus.IN_PROGRESS.getCode());
        studentMapper.updateById(student);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyUnbind(Long applyId, String reason) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        if (!StringUtils.hasText(reason)) {
            throw new BusinessException("离职原因不能为空");
        }
        
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法申请离职");
        }
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 验证申请是当前学生的已确认申请（current_apply_id = applyId）
        Student student = studentMapper.selectById(studentId);
        if (student == null || !applyId.equals(student.getCurrentApplyId())) {
            throw new BusinessException("该申请不是您当前的实习申请，无法申请离职");
        }
        
        // 验证学生确认状态为已确认
        if (apply.getStudentConfirmStatus() == null || !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            throw new BusinessException("该申请尚未确认上岗，无法申请离职");
        }
        
        // 验证解绑状态为未申请
        if (apply.getUnbindStatus() != null && !apply.getUnbindStatus().equals(UnbindStatus.NOT_APPLIED.getCode())) {
            throw new BusinessException("该申请已有解绑申请，请等待审核");
        }
        
        // 更新申请：unbind_status = APPLIED, unbind_reason = reason
        apply.setUnbindStatus(UnbindStatus.APPLIED.getCode());
        apply.setUnbindReason(reason);
        this.updateById(apply);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditUnbind(Long applyId, AuditUnbindDTO auditDTO) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        if (auditDTO == null || auditDTO.getAuditStatus() == null) {
            throw new BusinessException("审核信息不能为空");
        }
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 验证申请的解绑状态为申请解绑或企业管理员已审批
        if (apply.getUnbindStatus() == null || 
            (!apply.getUnbindStatus().equals(UnbindStatus.APPLIED.getCode()) 
             && !apply.getUnbindStatus().equals(UnbindStatus.ENTERPRISE_APPROVED.getCode()))) {
            throw new BusinessException("该申请没有待审核的解绑申请");
        }
        
        // 验证权限
        checkUnbindAuditPermission(apply);
        
        // 获取当前用户ID
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        
        // 判断当前用户角色，执行相应的审核逻辑
        if (isEnterpriseUser()) {
            // 企业管理员/企业导师审批：只能审批状态为"申请解绑"的申请
            if (!apply.getUnbindStatus().equals(UnbindStatus.APPLIED.getCode())) {
                throw new BusinessException("该申请已由企业管理员审批，请等待学校管理员或班主任审批");
            }
            // 企业管理员审批通过，状态变为"企业管理员已审批"
            if (auditDTO.getAuditStatus().getCode() == AuditStatus.APPROVED.getCode()) {
                processEnterpriseUnbindApproved(apply, currentUserId, auditDTO.getAuditOpinion());
            } else {
                // 企业管理员拒绝，直接变为"解绑被拒绝"
                processUnbindRejected(apply, currentUserId, auditDTO.getAuditOpinion());
            }
        } else {
            // 学校端角色审批：只能审批状态为"企业管理员已审批"的申请
            if (!apply.getUnbindStatus().equals(UnbindStatus.ENTERPRISE_APPROVED.getCode())) {
                throw new BusinessException("该申请需要先由企业管理员审批");
            }
            // 学校端审批
            if (auditDTO.getAuditStatus().getCode() == AuditStatus.APPROVED.getCode()) {
                processUnbindApproved(apply, currentUserId, auditDTO.getAuditOpinion());
            } else {
                processUnbindRejected(apply, currentUserId, auditDTO.getAuditOpinion());
            }
        }
        
        return true;
    }
    
    
    /**
     * 检查解绑审核权限
     */
    private void checkUnbindAuditPermission(InternshipApply apply) {
        // 学校管理员可以审核所有学生的解绑申请
        if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN)) {
            return;
        }
        
        // 企业用户权限检查
        if (isEnterpriseUser()) {
            checkEnterpriseUnbindPermission(apply);
            return;
        }
        
        // 学校端角色权限检查
        checkSchoolUnbindPermission(apply);
    }
    
    /**
     * 判断是否为企业用户
     */
    private boolean isEnterpriseUser() {
        return dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_ADMIN) 
                || dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_MENTOR);
    }
    
    /**
     * 检查企业用户解绑审核权限
     */
    private void checkEnterpriseUnbindPermission(InternshipApply apply) {
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null 
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权审核该学生的解绑申请");
        }
    }
    
    /**
     * 检查学校端角色解绑审核权限
     */
    private void checkSchoolUnbindPermission(InternshipApply apply) {
        if (apply.getStudentId() == null) {
            throw new BusinessException("无权审核该学生的解绑申请");
        }
        
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student == null || student.getClassId() == null) {
            throw new BusinessException("无权审核该学生的解绑申请");
        }
        
        // 检查是否是班主任管理的班级
        if (isStudentInManagedClasses(student)) {
            return;
        }
        
        // 检查是否是学院负责人管理的学院
        if (isStudentInManagedCollege(student)) {
            return;
        }
        
        throw new BusinessException("无权审核该学生的解绑申请");
    }
    
    /**
     * 检查学生是否在管理的班级中
     */
    private boolean isStudentInManagedClasses(Student student) {
        List<Long> managedClassIds = dataPermissionUtil.getCurrentUserClassIds();
        return managedClassIds != null && !managedClassIds.isEmpty() 
                && managedClassIds.contains(student.getClassId());
    }
    
    /**
     * 检查学生是否在管理的学院中
     */
    private boolean isStudentInManagedCollege(Student student) {
        if (!dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
            return false;
        }
        
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        return currentUserCollegeId != null && currentUserCollegeId.equals(student.getCollegeId());
    }
    
    /**
     * 处理企业管理员解绑审核通过
     */
    private void processEnterpriseUnbindApproved(InternshipApply apply, Long currentUserId, String auditOpinion) {
        apply.setUnbindStatus(UnbindStatus.ENTERPRISE_APPROVED.getCode());
        // 保存企业管理员审核信息（可以使用新的字段，或者合并到现有字段）
        apply.setUnbindAuditUserId(currentUserId);
        apply.setUnbindAuditTime(LocalDateTime.now());
        apply.setUnbindAuditOpinion(auditOpinion);
        this.updateById(apply);
    }
    
    /**
     * 处理解绑审核通过（学校端最终审批）
     */
    private void processUnbindApproved(InternshipApply apply, Long currentUserId, String auditOpinion) {
        apply.setUnbindStatus(UnbindStatus.UNBOUND.getCode());
        apply.setUnbindAuditUserId(currentUserId);
        apply.setUnbindAuditTime(LocalDateTime.now());
        // 合并审核意见：企业管理员意见 + 学校端意见
        String existingOpinion = apply.getUnbindAuditOpinion();
        if (StringUtils.hasText(existingOpinion) && StringUtils.hasText(auditOpinion)) {
            apply.setUnbindAuditOpinion("企业管理员：" + existingOpinion + "；学校端：" + auditOpinion);
        } else if (StringUtils.hasText(auditOpinion)) {
            apply.setUnbindAuditOpinion("学校端：" + auditOpinion);
        }
        apply.setStudentConfirmStatus(StudentConfirmStatus.NOT_CONFIRMED.getCode());
        apply.setInternshipEndDate(java.time.LocalDate.now());
        // 更新申请状态为已离职
        apply.setStatus(InternshipApplyStatus.RESIGNED.getCode());
        this.updateById(apply);
        
        // 更新学生信息
        updateStudentAfterUnbind(apply.getStudentId());
    }
    
    /**
     * 处理解绑审核拒绝
     */
    private void processUnbindRejected(InternshipApply apply, Long currentUserId, String auditOpinion) {
        apply.setUnbindStatus(UnbindStatus.REJECTED.getCode());
        apply.setUnbindAuditUserId(currentUserId);
        apply.setUnbindAuditTime(LocalDateTime.now());
        apply.setUnbindAuditOpinion(auditOpinion);
        this.updateById(apply);
    }
    
    /**
     * 更新学生解绑后的信息
     */
    private void updateStudentAfterUnbind(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student != null) {
            student.setCurrentApplyId(null);
            student.setCurrentEnterpriseId(null);
            student.setInternshipStatus(StudentInternshipStatus.RESIGNED.getCode());
            studentMapper.updateById(student);
        }
    }
    
    @Override
    public InternshipApply getCurrentInternship() {
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            return null;
        }
        
        // 获取学生信息
        Student student = studentMapper.selectById(studentId);
        if (student == null || student.getCurrentApplyId() == null) {
            return null;
        }
        
        // 查询 current_apply_id 对应的申请
        InternshipApply apply = this.getById(student.getCurrentApplyId());
        // 允许返回null，不使用验证工具
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            return null;
        }
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            return null;
        }
        
        // 填充关联字段（企业信息、岗位信息等）
        fillApplyRelatedFields(apply);
        
        return apply;
    }
}

