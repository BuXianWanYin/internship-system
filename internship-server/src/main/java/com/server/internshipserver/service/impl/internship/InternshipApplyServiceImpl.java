package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.internship.Interview;
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
        if (apply.getEnterpriseId() == null) {
            throw new BusinessException("企业ID不能为空");
        }
        
        // 获取当前登录学生信息
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            throw new BusinessException("未登录");
        }
        
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, user.getUserId())
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        // 验证企业是否与学校有合作关系
        if (student.getSchoolId() != null) {
            boolean hasCooperation = cooperationService.hasCooperation(apply.getEnterpriseId(), student.getSchoolId());
            if (!hasCooperation) {
                throw new BusinessException("该企业未与学校建立合作关系，无法申请");
            }
        }
        
        // 如果指定了岗位，验证岗位是否存在且已发布
        if (apply.getPostId() != null) {
            InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
            if (post == null || post.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                throw new BusinessException("岗位不存在");
            }
            if (post.getStatus() == null || post.getStatus() != 3) {
                throw new BusinessException("岗位未发布，无法申请");
            }
            if (!post.getEnterpriseId().equals(apply.getEnterpriseId())) {
                throw new BusinessException("岗位与企业不匹配");
            }
        }
        
        // 新增：验证实习计划
        if (apply.getPlanId() != null) {
            InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
            if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                throw new BusinessException("实习计划不存在");
            }
            
            // 验证计划状态：必须是已发布
            if (plan.getStatus() == null || plan.getStatus() != 4) {
                throw new BusinessException("只能选择已发布的实习计划");
            }
            
            // 验证组织架构匹配
            if (!plan.getSchoolId().equals(student.getSchoolId())) {
                throw new BusinessException("实习计划与学生的学校不匹配");
            }
            if (plan.getCollegeId() != null && !plan.getCollegeId().equals(student.getCollegeId())) {
                throw new BusinessException("实习计划与学生的学院不匹配");
            }
            if (plan.getMajorId() != null && !plan.getMajorId().equals(student.getMajorId())) {
                throw new BusinessException("实习计划与学生的专业不匹配");
            }
            
            // 验证时间范围：如果申请中指定了实习时间，需要验证是否在计划范围内
            if (apply.getInternshipStartDate() != null) {
                if (apply.getInternshipStartDate().isBefore(plan.getStartDate())) {
                    throw new BusinessException("实习开始日期不能早于计划的开始日期");
                }
            }
            if (apply.getInternshipEndDate() != null) {
                if (apply.getInternshipEndDate().isAfter(plan.getEndDate())) {
                    throw new BusinessException("实习结束日期不能晚于计划的结束日期");
                }
            }
            
            // 如果申请中没有指定实习时间，使用计划的时间范围
            if (apply.getInternshipStartDate() == null) {
                apply.setInternshipStartDate(plan.getStartDate());
            }
            if (apply.getInternshipEndDate() == null) {
                apply.setInternshipEndDate(plan.getEndDate());
            }
        } else {
            // 如果没有选择计划，尝试自动匹配
            List<InternshipPlan> availablePlans = internshipPlanService.getAvailablePlansForStudent(student.getStudentId());
            if (!availablePlans.isEmpty()) {
                // 如果有匹配的计划，要求用户必须选择
                throw new BusinessException("请选择实习计划");
            }
        }
        
        // 检查是否已申请过该企业或岗位
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipApply::getStudentId, student.getStudentId())
               .eq(InternshipApply::getApplyType, 1)
               .eq(InternshipApply::getEnterpriseId, apply.getEnterpriseId())
               .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .in(InternshipApply::getStatus, 0, 1, 3); // 待审核、已通过、已录用
        if (apply.getPostId() != null) {
            wrapper.eq(InternshipApply::getPostId, apply.getPostId());
        }
        InternshipApply existApply = this.getOne(wrapper);
        if (existApply != null) {
            throw new BusinessException("已申请过该企业" + (apply.getPostId() != null ? "岗位" : ""));
        }
        
        // 设置申请信息
        apply.setStudentId(student.getStudentId());
        apply.setUserId(user.getUserId());
        apply.setApplyType(1); // 合作企业
        apply.setStatus(0); // 待审核
        apply.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(apply);
        
        // 保存后，填充计划信息
        if (apply.getPlanId() != null) {
            InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
            if (plan != null) {
                apply.setPlanName(plan.getPlanName());
                apply.setPlanCode(plan.getPlanCode());
            }
        }
        
        // 更新岗位申请人数
        if (apply.getPostId() != null) {
            InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
            if (post != null) {
                post.setAppliedCount((post.getAppliedCount() == null ? 0 : post.getAppliedCount()) + 1);
                internshipPostMapper.updateById(post);
            }
        }
        
        return apply;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply addSelfApply(InternshipApply apply) {
        // 参数校验
        if (!StringUtils.hasText(apply.getSelfEnterpriseName())) {
            throw new BusinessException("企业名称不能为空");
        }
        if (!StringUtils.hasText(apply.getSelfEnterpriseAddress())) {
            throw new BusinessException("企业地址不能为空");
        }
        if (!StringUtils.hasText(apply.getSelfContactPerson())) {
            throw new BusinessException("联系人不能为空");
        }
        if (!StringUtils.hasText(apply.getSelfContactPhone())) {
            throw new BusinessException("联系电话不能为空");
        }
        if (!StringUtils.hasText(apply.getSelfPostName())) {
            throw new BusinessException("岗位名称不能为空");
        }
        if (apply.getSelfStartDate() == null || apply.getSelfEndDate() == null) {
            throw new BusinessException("实习开始日期和结束日期不能为空");
        }
        if (apply.getSelfStartDate().isAfter(apply.getSelfEndDate())) {
            throw new BusinessException("实习开始日期不能晚于结束日期");
        }
        
        // 获取当前登录学生信息
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            throw new BusinessException("未登录");
        }
        
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, user.getUserId())
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        // 新增：验证实习计划
        if (apply.getPlanId() != null) {
            InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
            if (plan == null || plan.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                throw new BusinessException("实习计划不存在");
            }
            
            // 验证计划状态：必须是已发布
            if (plan.getStatus() == null || plan.getStatus() != 4) {
                throw new BusinessException("只能选择已发布的实习计划");
            }
            
            // 验证组织架构匹配
            if (!plan.getSchoolId().equals(student.getSchoolId())) {
                throw new BusinessException("实习计划与学生的学校不匹配");
            }
            if (plan.getCollegeId() != null && !plan.getCollegeId().equals(student.getCollegeId())) {
                throw new BusinessException("实习计划与学生的学院不匹配");
            }
            if (plan.getMajorId() != null && !plan.getMajorId().equals(student.getMajorId())) {
                throw new BusinessException("实习计划与学生的专业不匹配");
            }
            
            // 验证时间范围：自主实习的时间需要验证是否在计划范围内
            if (apply.getSelfStartDate() != null) {
                if (apply.getSelfStartDate().isBefore(plan.getStartDate())) {
                    throw new BusinessException("实习开始日期不能早于计划的开始日期");
                }
            }
            if (apply.getSelfEndDate() != null) {
                if (apply.getSelfEndDate().isAfter(plan.getEndDate())) {
                    throw new BusinessException("实习结束日期不能晚于计划的结束日期");
                }
            }
        } else {
            // 如果没有选择计划，尝试自动匹配
            List<InternshipPlan> availablePlans = internshipPlanService.getAvailablePlansForStudent(student.getStudentId());
            if (!availablePlans.isEmpty()) {
                // 如果有匹配的计划，要求用户必须选择
                throw new BusinessException("请选择实习计划");
            }
        }
        
        // 设置申请信息
        apply.setStudentId(student.getStudentId());
        apply.setUserId(user.getUserId());
        apply.setApplyType(2); // 自主实习
        apply.setStatus(0); // 待审核
        apply.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 自主实习不应该有企业ID和岗位ID，清空这些字段
        apply.setEnterpriseId(null);
        apply.setPostId(null);
        
        // 将自主实习的时间字段映射到实习时间字段
        apply.setInternshipStartDate(apply.getSelfStartDate());
        apply.setInternshipEndDate(apply.getSelfEndDate());
        // 自主实习不应该有企业反馈、面试、录用相关字段，清空这些字段
        apply.setEnterpriseFeedback(null);
        apply.setEnterpriseFeedbackTime(null);
        apply.setInterviewTime(null);
        apply.setInterviewLocation(null);
        apply.setInterviewResult(null);
        apply.setInterviewComment(null);
        apply.setAcceptTime(null);
        
        // 保存
        this.save(apply);
        
        // 保存后，填充计划信息
        if (apply.getPlanId() != null) {
            InternshipPlan plan = internshipPlanService.getById(apply.getPlanId());
            if (plan != null) {
                apply.setPlanName(plan.getPlanName());
                apply.setPlanCode(plan.getPlanCode());
            }
        }
        
        return apply;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipApply updateApply(InternshipApply apply) {
        if (apply.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        // 检查申请是否存在
        InternshipApply existApply = this.getById(apply.getApplyId());
        if (existApply == null || existApply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 只有待审核状态才能修改
        if (existApply.getStatus() == null || existApply.getStatus() != 0) {
            throw new BusinessException("只有待审核状态的申请才能修改");
        }
        
        // 数据权限：学生只能修改自己的申请
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null && !user.getUserId().equals(existApply.getUserId())) {
                throw new BusinessException("无权修改该申请");
            }
        }
        
        // 更新
        this.updateById(apply);
        return this.getById(apply.getApplyId());
    }
    
    @Override
    public InternshipApply getApplyById(Long applyId) {
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 填充关联字段
        fillApplyRelatedFields(apply);
        
        // 数据权限过滤
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
                
                // 系统管理员和学校管理员可以查看所有申请
                if (roleCodes != null && (roleCodes.contains("ROLE_SYSTEM_ADMIN") || roleCodes.contains("ROLE_SCHOOL_ADMIN"))) {
                    // 构建状态流转历史和下一步操作提示
                    buildStatusHistory(apply);
                    buildNextActionTip(apply);
                    return apply;
                }
                
                // 学生只能查看自己的申请
                if (apply.getUserId().equals(user.getUserId())) {
                    // 构建状态流转历史和下一步操作提示
                    buildStatusHistory(apply);
                    buildNextActionTip(apply);
                    return apply;
                }
                
                // 企业管理员只能查看自己企业的申请
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId != null && apply.getEnterpriseId() != null
                        && currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    // 构建状态流转历史和下一步操作提示
                    buildStatusHistory(apply);
                    buildNextActionTip(apply);
                    return apply;
                }
                
                // 班主任和学院负责人可以查看自己管理的学生的申请
                if (roleCodes != null && (roleCodes.contains("ROLE_CLASS_TEACHER") || roleCodes.contains("ROLE_COLLEGE_LEADER"))) {
                    // 检查申请的学生是否属于当前用户管理的班级
                    if (apply.getStudentId() != null) {
                        Student student = studentMapper.selectById(apply.getStudentId());
                        if (student != null && student.getClassId() != null) {
                            List<Long> managedClassIds = dataPermissionUtil.getCurrentUserClassIds();
                            if (managedClassIds != null && !managedClassIds.isEmpty() 
                                    && managedClassIds.contains(student.getClassId())) {
                                // 构建状态流转历史和下一步操作提示
                                buildStatusHistory(apply);
                                buildNextActionTip(apply);
                                return apply;
                            }
                        }
                    }
                }
                
                // 如果没有匹配的权限，抛出异常
                throw new BusinessException("无权查看该申请");
            }
        }
        
        // 构建状态流转历史和下一步操作提示
        buildStatusHistory(apply);
        buildNextActionTip(apply);
        
        return apply;
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
        } else if (apply.getApplyType() != null && apply.getApplyType() == 2) {
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
        } else if (apply.getApplyType() != null && apply.getApplyType() == 2) {
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
        if (apply.getApplyType() != null && apply.getApplyType() == 1) {
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
        if (apply.getApplyType() != null && apply.getApplyType() == 2) {
            switch (apply.getStatus()) {
                case 0:
                    return "等待学校审核";
                case 1:
                    return "学校审核通过";
                case 2:
                    return "学校审核拒绝";
                case 5:
                    return "已取消";
                default:
                    return "未知状态";
            }
        }
        
        // 合作企业申请
        if (apply.getApplyType() != null && apply.getApplyType() == 1) {
            switch (apply.getStatus()) {
                case 0:
                    return "等待学校审核";
                case 1:
                    // 学校审核通过，需要根据面试情况判断
                    if (apply.getHasInterview() != null && apply.getHasInterview() && apply.getLatestInterview() != null) {
                        Interview interview = apply.getLatestInterview();
                        if (interview.getStudentConfirm() == null || interview.getStudentConfirm() == 0) {
                            return "等待学生确认面试";
                        } else if (interview.getStudentConfirm() == 1) {
                            if (interview.getStatus() == null || interview.getStatus() == 1) {
                                return "面试已确认，等待面试";
                            } else if (interview.getStatus() == 2) {
                                // 面试已完成
                                if (interview.getInterviewResult() == null) {
                                    return "面试已完成，等待结果";
                                } else if (interview.getInterviewResult() == 1) {
                                    return "面试通过，等待企业决定";
                                } else if (interview.getInterviewResult() == 2) {
                                    return "面试未通过";
                                } else {
                                    return "面试待定";
                                }
                            } else if (interview.getStatus() == 3) {
                                return "面试已取消";
                            }
                        } else if (interview.getStudentConfirm() == 2) {
                            return "学生已拒绝面试";
                        }
                    }
                    return "等待企业处理";
                case 2:
                    return "学校审核拒绝";
                case 3:
                    return "已录用";
                case 4:
                    return "已拒绝录用";
                case 5:
                    return "已取消";
                default:
                    return "未知状态";
            }
        }
        
        return "未知状态";
    }
    
    /**
     * 构建状态流转历史
     */
    private void buildStatusHistory(InternshipApply apply) {
        List<InternshipApply.StatusHistoryItem> history = new ArrayList<>();
        
        // 自主实习申请：简化流程（只显示：申请提交、学校审核、取消）
        if (apply.getApplyType() != null && apply.getApplyType() == 2) {
            // 1. 申请提交
            if (apply.getCreateTime() != null) {
                InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
                item.setActionName("申请提交");
                item.setActionTime(apply.getCreateTime());
                item.setOperator(apply.getStudentName());
                item.setDescription("学生提交了自主实习申请");
                item.setStatus(0);
                history.add(item);
            }
            
            // 2. 学校审核
            if (apply.getAuditTime() != null) {
                InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
                item.setActionName(apply.getStatus() == 1 ? "学校审核通过" : "学校审核拒绝");
                item.setActionTime(apply.getAuditTime());
                item.setOperator(apply.getAuditorName());
                item.setDescription(apply.getAuditOpinion() != null ? apply.getAuditOpinion() : 
                    (apply.getStatus() == 1 ? "学校审核通过" : "学校审核拒绝"));
                item.setStatus(apply.getStatus());
                history.add(item);
            }
            
            // 3. 取消状态（如果状态是已取消）
            if (apply.getStatus() != null && apply.getStatus() == 5) {
                InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
                item.setActionName("申请已取消");
                item.setActionTime(apply.getUpdateTime() != null ? apply.getUpdateTime() : apply.getCreateTime());
                item.setOperator(apply.getStudentName());
                item.setDescription("学生取消了申请");
                item.setStatus(5);
                history.add(item);
            }
            
            apply.setStatusHistory(history);
            return;
        }
        
        // 合作企业申请：全流程状态
        // 1. 申请提交
        if (apply.getCreateTime() != null) {
            InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
            item.setActionName("申请提交");
            item.setActionTime(apply.getCreateTime());
            item.setOperator(apply.getStudentName());
            item.setDescription("学生提交了实习申请");
            item.setStatus(0);
            history.add(item);
        }
        
        // 2. 学校审核（合作企业申请，学校审核通过后status=1）
        if (apply.getApplyType() != null && apply.getApplyType() == 1 && apply.getAuditTime() != null) {
            InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
            item.setActionName(apply.getStatus() == 1 ? "学校审核通过" : "学校审核拒绝");
            item.setActionTime(apply.getAuditTime());
            item.setOperator(apply.getAuditorName());
            item.setDescription(apply.getAuditOpinion() != null ? apply.getAuditOpinion() : 
                (apply.getStatus() == 1 ? "学校审核通过，等待企业处理" : "学校审核拒绝"));
            item.setStatus(apply.getStatus());
            history.add(item);
        }
        
        // 4. 企业反馈（仅合作企业申请）
        if (apply.getApplyType() != null && apply.getApplyType() == 1 && apply.getEnterpriseFeedbackTime() != null) {
            InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
            item.setActionName("企业反馈");
            item.setActionTime(apply.getEnterpriseFeedbackTime());
            item.setOperator(apply.getEnterpriseName());
            item.setDescription(apply.getEnterpriseFeedback() != null ? apply.getEnterpriseFeedback() : "企业已查看申请");
            item.setStatus(apply.getStatus());
            history.add(item);
        }
        
        // 5. 面试安排（仅合作企业申请）
        if (apply.getApplyType() != null && apply.getApplyType() == 1) {
            List<Interview> interviews = interviewMapper.selectList(
                new LambdaQueryWrapper<Interview>()
                    .eq(Interview::getApplyId, apply.getApplyId())
                    .eq(Interview::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    .orderByAsc(Interview::getCreateTime)
            );
            
            if (interviews != null && !interviews.isEmpty()) {
                for (Interview interview : interviews) {
                    if (interview.getCreateTime() != null) {
                        InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
                        item.setActionName("面试安排");
                        item.setActionTime(interview.getCreateTime());
                        item.setOperator(apply.getEnterpriseName());
                        String interviewTypeText = interview.getInterviewType() == 1 ? "现场面试" : 
                            (interview.getInterviewType() == 2 ? "视频面试" : "电话面试");
                        item.setDescription("企业安排了" + interviewTypeText + "，面试时间：" + 
                            (interview.getInterviewTime() != null ? interview.getInterviewTime().toString() : "待定"));
                        item.setStatus(apply.getStatus());
                        history.add(item);
                    }
                    
                    // 学生确认面试
                    if (interview.getStudentConfirmTime() != null) {
                        InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
                        item.setActionName(interview.getStudentConfirm() == 1 ? "学生确认面试" : "学生拒绝面试");
                        item.setActionTime(interview.getStudentConfirmTime());
                        item.setOperator(apply.getStudentName());
                        item.setDescription(interview.getStudentConfirm() == 1 ? "学生已确认参加面试" : "学生已拒绝面试");
                        item.setStatus(apply.getStatus());
                        history.add(item);
                    }
                    
                    // 面试完成
                    if (interview.getInterviewFeedbackTime() != null) {
                        InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
                        String resultText = interview.getInterviewResult() == 1 ? "通过" : 
                            (interview.getInterviewResult() == 2 ? "不通过" : "待定");
                        item.setActionName("面试完成");
                        item.setActionTime(interview.getInterviewFeedbackTime());
                        item.setOperator(apply.getEnterpriseName());
                        item.setDescription("面试结果：" + resultText + 
                            (interview.getInterviewComment() != null ? "，" + interview.getInterviewComment() : ""));
                        item.setStatus(apply.getStatus());
                        history.add(item);
                    }
                }
            }
        }
        
        // 6. 录用/拒绝
        if (apply.getAcceptTime() != null) {
            InternshipApply.StatusHistoryItem item = new InternshipApply.StatusHistoryItem();
            item.setActionName(apply.getStatus() == 3 ? "企业录用" : "企业拒绝");
            item.setActionTime(apply.getAcceptTime());
            item.setOperator(apply.getEnterpriseName());
            item.setDescription(apply.getStatus() == 3 ? "企业已录用" : "企业已拒绝录用");
            item.setStatus(apply.getStatus());
            history.add(item);
        }
        
        // 按时间排序
        history.sort((a, b) -> {
            if (a.getActionTime() == null && b.getActionTime() == null) return 0;
            if (a.getActionTime() == null) return 1;
            if (b.getActionTime() == null) return -1;
            return a.getActionTime().compareTo(b.getActionTime());
        });
        
        apply.setStatusHistory(history);
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
        if (apply.getApplyType() != null && apply.getApplyType() == 2) {
            if (apply.getStatus() == 0) {
                apply.setNextActionTip("等待学校审核，请耐心等待");
            } else if (apply.getStatus() == 1) {
                apply.setNextActionTip("学校审核已通过，可以开始实习");
            } else if (apply.getStatus() == 2) {
                apply.setNextActionTip("学校审核已拒绝，如有疑问请联系班主任");
            }
            return;
        }
        
        // 合作企业申请
        if (apply.getApplyType() != null && apply.getApplyType() == 1) {
            if (apply.getStatus() == 0) {
                apply.setNextActionTip("等待学校审核，请耐心等待");
            } else if (apply.getStatus() == 1) {
                // 使用已填充的面试信息（避免重复查询）
                if (apply.getHasInterview() != null && apply.getHasInterview() && apply.getLatestInterview() != null) {
                    Interview latestInterview = apply.getLatestInterview();
                    if (latestInterview.getStudentConfirm() == null || latestInterview.getStudentConfirm() == 0) {
                        apply.setNextActionTip("企业已安排面试，请前往\"我的面试\"页面确认是否参加");
                    } else if (latestInterview.getStudentConfirm() == 1 && latestInterview.getStatus() == 1) {
                        apply.setNextActionTip("面试已确认，请按时参加面试");
                    } else if (latestInterview.getStatus() == 2) {
                        if (latestInterview.getInterviewResult() == null) {
                            apply.setNextActionTip("面试已完成，等待企业反馈结果");
                        } else if (latestInterview.getInterviewResult() == 1) {
                            apply.setNextActionTip("面试已通过，等待企业决定是否录用");
                        } else if (latestInterview.getInterviewResult() == 2) {
                            apply.setNextActionTip("面试未通过，如有疑问请联系企业");
                        } else {
                            apply.setNextActionTip("面试待定，等待企业决定");
                        }
                    } else if (latestInterview.getStudentConfirm() == 2) {
                        apply.setNextActionTip("已拒绝面试，申请流程已结束");
                    } else if (latestInterview.getStatus() == 3) {
                        apply.setNextActionTip("面试已取消，等待企业重新安排");
                    }
                } else {
                    apply.setNextActionTip("等待企业处理，企业可以安排面试或直接录用/拒绝");
                }
            } else if (apply.getStatus() == 3) {
                apply.setNextActionTip("已录用，恭喜！请与企业联系确认实习安排");
            } else if (apply.getStatus() == 4) {
                apply.setNextActionTip("已拒绝录用，如有疑问请联系企业");
            } else if (apply.getStatus() == 2) {
                apply.setNextActionTip("学校审核已拒绝，如有疑问请联系班主任");
            }
        }
    }
    
    @Override
    public Page<InternshipApply> getApplyPage(Page<InternshipApply> page, Long studentId, Long enterpriseId,
                                               Long postId, Integer applyType, Integer status) {
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(InternshipApply::getStudentId, studentId);
        }
        if (enterpriseId != null) {
            wrapper.eq(InternshipApply::getEnterpriseId, enterpriseId);
        }
        if (postId != null) {
            wrapper.eq(InternshipApply::getPostId, postId);
        }
        if (applyType != null) {
            wrapper.eq(InternshipApply::getApplyType, applyType);
        }
        if (status != null) {
            wrapper.eq(InternshipApply::getStatus, status);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipApply::getCreateTime);
        
        Page<InternshipApply> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (InternshipApply apply : result.getRecords()) {
                fillApplyRelatedFields(apply);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditApply(Long applyId, Integer auditStatus, String auditOpinion) {
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (auditStatus == null || (auditStatus != 1 && auditStatus != 2)) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 只有待审核状态的申请才能审核（支持合作企业和自主实习）
        if (apply.getStatus() == null || apply.getStatus() != 0) {
            throw new BusinessException("只有待审核状态的申请才能审核");
        }
        
        // 设置审核信息
        apply.setStatus(auditStatus);
        apply.setAuditTime(LocalDateTime.now());
        apply.setAuditOpinion(auditOpinion);
        
        // 设置审核人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                apply.setAuditUserId(user.getUserId());
            }
        }
        
        // 如果是自主实习且审核通过，需要创建企业并绑定
        if (auditStatus == 1 && apply.getApplyType() != null && apply.getApplyType() == 2) {
            // 检查是否已经绑定了企业ID
            if (apply.getEnterpriseId() == null) {
                // 根据企业名称查找是否已存在该企业
                Enterprise existingEnterprise = enterpriseMapper.selectOne(
                        new LambdaQueryWrapper<Enterprise>()
                                .eq(Enterprise::getEnterpriseName, apply.getSelfEnterpriseName())
                                .eq(Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .last("LIMIT 1")
                );
                
                Long enterpriseId;
                if (existingEnterprise != null) {
                    // 使用已存在的企业
                    enterpriseId = existingEnterprise.getEnterpriseId();
                } else {
                    // 创建新的企业记录
                    Enterprise newEnterprise = new Enterprise();
                    newEnterprise.setEnterpriseName(apply.getSelfEnterpriseName());
                    // 设置企业地址
                    if (StringUtils.hasText(apply.getSelfEnterpriseAddress())) {
                        newEnterprise.setAddress(apply.getSelfEnterpriseAddress());
                    }
                    // 设置联系人信息
                    if (StringUtils.hasText(apply.getSelfContactPerson())) {
                        newEnterprise.setContactPerson(apply.getSelfContactPerson());
                    }
                    if (StringUtils.hasText(apply.getSelfContactPhone())) {
                        newEnterprise.setContactPhone(apply.getSelfContactPhone());
                    }
                    // 自主实习企业默认审核通过，状态启用
                    newEnterprise.setAuditStatus(1); // 已审核通过
                    newEnterprise.setStatus(1); // 启用
                    newEnterprise.setDeleteFlag(DeleteFlag.NORMAL.getCode());
                    // 生成企业代码（使用自主实习企业名称的拼音首字母或时间戳）
                    String enterpriseCode = "SELF_" + System.currentTimeMillis();
                    newEnterprise.setEnterpriseCode(enterpriseCode);
                    
                    enterpriseMapper.insert(newEnterprise);
                    enterpriseId = newEnterprise.getEnterpriseId();
                }
                
                // 绑定企业ID到申请记录
                apply.setEnterpriseId(enterpriseId);
                
                // 对于自主实习，审核通过后直接设置为已录用状态（status=3）
                // 因为自主实习不需要企业面试流程
                apply.setStatus(3); // 已录用
                apply.setAcceptTime(LocalDateTime.now());
            }
        }
        
        return this.updateById(apply);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean filterApply(Long applyId, Integer action, String comment) {
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (action == null || (action < 1 || action > 4)) {
            throw new BusinessException("操作类型无效");
        }
        
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 数据权限：企业管理员只能操作自己企业的申请
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权操作该申请");
        }
        
        // 只有合作企业的申请才能进行筛选操作
        if (apply.getApplyType() == null || apply.getApplyType() != 1) {
            throw new BusinessException("只能对合作企业申请进行筛选操作");
        }
        
        // 只有学校审核通过的申请（status=1）才能进行企业筛选操作
        if (apply.getStatus() == null || apply.getStatus() != 1) {
            throw new BusinessException("只能处理学校审核通过的申请");
        }
        
        // 根据操作类型更新状态
        switch (action) {
            case 1: // 标记感兴趣（不改变状态，只记录反馈）
                apply.setEnterpriseFeedback(comment);
                apply.setEnterpriseFeedbackTime(LocalDateTime.now());
                break;
            case 2: // 安排面试（状态不变，面试信息在面试表中记录）
                apply.setEnterpriseFeedback(comment);
                apply.setEnterpriseFeedbackTime(LocalDateTime.now());
                break;
            case 3: // 录用
                apply.setStatus(3); // 已录用
                apply.setAcceptTime(LocalDateTime.now());
                apply.setEnterpriseFeedback(comment);
                apply.setEnterpriseFeedbackTime(LocalDateTime.now());
                // 更新岗位录用人数
                if (apply.getPostId() != null) {
                    InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
                    if (post != null) {
                        post.setAcceptedCount((post.getAcceptedCount() == null ? 0 : post.getAcceptedCount()) + 1);
                        internshipPostMapper.updateById(post);
                    }
                }
                break;
            case 4: // 拒绝
                apply.setStatus(4); // 已拒绝录用
                apply.setEnterpriseFeedback(comment);
                apply.setEnterpriseFeedbackTime(LocalDateTime.now());
                break;
        }
        
        return this.updateById(apply);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelApply(Long applyId) {
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 数据权限：学生只能取消自己的申请
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(apply.getUserId())) {
                throw new BusinessException("无权取消该申请");
            }
        }
        
        // 只有待审核状态才能取消
        if (apply.getStatus() == null || apply.getStatus() != 0) {
            throw new BusinessException("只有待审核状态的申请才能取消");
        }
        
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
                        .in(InternshipApply::getStatus, 0, 1, 3));
                post.setAppliedCount(appliedCount.intValue());
                internshipPostMapper.updateById(post);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteApply(Long applyId) {
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 数据权限：学生只能删除自己的申请
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(apply.getUserId())) {
                throw new BusinessException("无权删除该申请");
            }
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
        
        String username = SecurityUtil.getCurrentUsername();
        if (username == null) {
            return;
        }
        
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, username)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (user == null) {
            return;
        }
        
        // 学生只能查看自己的申请
        if (dataPermissionUtil.hasRole("ROLE_STUDENT")) {
            applyStudentFilter(wrapper, user.getUserId());
            return;
        }
        
        // 企业管理员只能查看自己企业的申请
        if (dataPermissionUtil.hasRole("ROLE_ENTERPRISE_ADMIN")) {
            applyEnterpriseFilter(wrapper);
            return;
        }
        
        // 学校管理员可以查看所有申请（不添加过滤条件）
        if (dataPermissionUtil.hasRole("ROLE_SCHOOL_ADMIN")) {
            return;
        }
        
        // 学院负责人可以查看本学院所有学生的申请
        if (dataPermissionUtil.hasRole("ROLE_COLLEGE_LEADER")) {
            applyCollegeFilter(wrapper);
            return;
        }
        
        // 班主任可以查看自己管理的班级的所有学生的申请
        if (dataPermissionUtil.hasRole("ROLE_CLASS_TEACHER")) {
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
            if (students != null && !students.isEmpty()) {
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
        if (managedClassIds != null && !managedClassIds.isEmpty()) {
            // 查询这些班级下的所有学生ID
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, managedClassIds)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getStudentId)
            );
            if (students != null && !students.isEmpty()) {
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
        
        // 只查询未删除的数据
        wrapper.eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 只查询合作企业申请
        wrapper.eq(InternshipApply::getApplyType, 1);
        
        // 只查询已录用的申请（status = 3）
        wrapper.eq(InternshipApply::getStatus, 3);
        
        // 只查询学生已确认上岗的申请（student_confirm_status = 1）
        wrapper.eq(InternshipApply::getStudentConfirmStatus, 1);
        
        // 数据权限：企业管理员只能查看本企业的学生
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            wrapper.eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId);
        } else {
            // 如果没有企业ID，返回空结果
            wrapper.eq(InternshipApply::getApplyId, -1L);
        }
        
        // 条件查询
        if (postId != null) {
            wrapper.eq(InternshipApply::getPostId, postId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipApply::getCreateTime);
        
        Page<InternshipApply> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (InternshipApply apply : result.getRecords()) {
                fillApplyRelatedFields(apply);
                
                // 如果学生姓名或学号条件存在，进行过滤
                if (StringUtils.hasText(studentName) || StringUtils.hasText(studentNo)) {
                    boolean match = true;
                    if (StringUtils.hasText(studentName) && 
                        (apply.getStudentName() == null || !apply.getStudentName().contains(studentName))) {
                        match = false;
                    }
                    if (StringUtils.hasText(studentNo) && 
                        (apply.getStudentNo() == null || !apply.getStudentNo().contains(studentNo))) {
                        match = false;
                    }
                    if (!match) {
                        result.getRecords().remove(apply);
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignMentor(Long applyId, Long mentorId) {
        // 参数校验
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (mentorId == null) {
            throw new BusinessException("企业导师ID不能为空");
        }
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 数据权限：企业管理员只能分配本企业的导师
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权操作该申请");
        }
        
        // 检查企业导师是否存在且属于该企业
        EnterpriseMentor mentor = enterpriseMentorMapper.selectById(mentorId);
        if (mentor == null || mentor.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("企业导师不存在");
        }
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
        // 参数校验
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法确认上岗");
        }
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException("无权操作该申请");
        }
        
        // 验证申请状态为已录用（status = 3）
        if (apply.getStatus() == null || apply.getStatus() != 3) {
            throw new BusinessException("只有已录用的申请才能确认上岗");
        }
        
        // 验证学生确认状态为未确认（student_confirm_status = 0）
        if (apply.getStudentConfirmStatus() != null && apply.getStudentConfirmStatus() != 0) {
            throw new BusinessException("该申请已经确认过了");
        }
        
        // 获取学生信息
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        // 如果学生已有其他已确认的申请，需要先解绑
        if (student.getCurrentApplyId() != null && !student.getCurrentApplyId().equals(applyId)) {
            InternshipApply oldApply = this.getById(student.getCurrentApplyId());
            if (oldApply != null && oldApply.getStudentConfirmStatus() != null && oldApply.getStudentConfirmStatus() == 1) {
                throw new BusinessException("您已有其他已确认的实习申请，请先解绑后再确认新的申请");
            }
        }
        
        // 更新申请：student_confirm_status = 1, student_confirm_time = now()
        apply.setStudentConfirmStatus(1);
        apply.setStudentConfirmTime(LocalDateTime.now());
        // 设置实习开始日期（如果未设置，使用当前日期）
        if (apply.getInternshipStartDate() == null) {
            apply.setInternshipStartDate(java.time.LocalDate.now());
        }
        this.updateById(apply);
        
        // 更新学生：current_apply_id = applyId, current_enterprise_id = enterpriseId, internship_status = 1
        student.setCurrentApplyId(applyId);
        // 绑定企业ID（自主实习在审核通过时已创建企业并绑定企业ID）
        if (apply.getEnterpriseId() != null) {
            student.setCurrentEnterpriseId(apply.getEnterpriseId());
        } else {
            // 如果企业ID为空，说明审核时没有正确创建企业，抛出异常
            throw new BusinessException("申请的企业信息不完整，无法确认上岗，请联系管理员");
        }
        student.setInternshipStatus(1); // 实习中
        studentMapper.updateById(student);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyUnbind(Long applyId, String reason) {
        // 参数校验
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
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
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 验证申请是当前学生的已确认申请（current_apply_id = applyId）
        Student student = studentMapper.selectById(studentId);
        if (student == null || !applyId.equals(student.getCurrentApplyId())) {
            throw new BusinessException("该申请不是您当前的实习申请，无法申请离职");
        }
        
        // 验证学生确认状态为已确认（student_confirm_status = 1）
        if (apply.getStudentConfirmStatus() == null || apply.getStudentConfirmStatus() != 1) {
            throw new BusinessException("该申请尚未确认上岗，无法申请离职");
        }
        
        // 验证解绑状态为未申请（unbind_status = 0）
        if (apply.getUnbindStatus() != null && apply.getUnbindStatus() != 0) {
            throw new BusinessException("该申请已有解绑申请，请等待审核");
        }
        
        // 更新申请：unbind_status = 1, unbind_reason = reason
        apply.setUnbindStatus(1);
        apply.setUnbindReason(reason);
        this.updateById(apply);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean auditUnbind(Long applyId, Integer auditStatus, String auditOpinion) {
        // 参数校验
        if (applyId == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (auditStatus == null || (auditStatus != 2 && auditStatus != 3)) {
            throw new BusinessException("审核状态无效，必须是2（已解绑）或3（解绑被拒绝）");
        }
        
        // 检查申请是否存在
        InternshipApply apply = this.getById(applyId);
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 验证申请的解绑状态为申请解绑（unbind_status = 1）
        if (apply.getUnbindStatus() == null || apply.getUnbindStatus() != 1) {
            throw new BusinessException("该申请没有待审核的解绑申请");
        }
        
        // 验证申请的学生属于当前用户管理的班级或企业
        // 学校管理员可以审核所有学生的解绑申请
        if (!dataPermissionUtil.hasRole("ROLE_SCHOOL_ADMIN")) {
            // 检查是否是企业管理员或企业导师
            boolean isEnterpriseUser = dataPermissionUtil.hasRole("ROLE_ENTERPRISE_ADMIN") 
                    || dataPermissionUtil.hasRole("ROLE_ENTERPRISE_MENTOR");
            
            if (isEnterpriseUser) {
                // 企业管理员或企业导师只能审核本企业的学生的解绑申请
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null 
                        || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    throw new BusinessException("无权审核该学生的解绑申请");
                }
            } else {
                // 学校端角色（班主任、学院负责人）的权限检查
                if (apply.getStudentId() != null) {
                    Student student = studentMapper.selectById(apply.getStudentId());
                    if (student != null && student.getClassId() != null) {
                        List<Long> managedClassIds = dataPermissionUtil.getCurrentUserClassIds();
                        if (managedClassIds == null || managedClassIds.isEmpty() || !managedClassIds.contains(student.getClassId())) {
                            // 如果不是班主任，检查是否是学院负责人
                            if (!dataPermissionUtil.hasRole("ROLE_COLLEGE_LEADER")) {
                                throw new BusinessException("无权审核该学生的解绑申请");
                            }
                            // 学院负责人需要验证学生是否属于本学院
                            Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
                            if (currentUserCollegeId == null || !currentUserCollegeId.equals(student.getCollegeId())) {
                                throw new BusinessException("无权审核该学生的解绑申请");
                            }
                        }
                    }
                }
            }
        }
        
        // 获取当前用户ID
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        
        // 如果审核通过（auditStatus = 2）
        if (auditStatus == 2) {
            // 更新申请：unbind_status = 2, unbind_audit_user_id = currentUserId, unbind_audit_time = now(), unbind_audit_opinion = auditOpinion
            apply.setUnbindStatus(2);
            apply.setUnbindAuditUserId(currentUserId);
            apply.setUnbindAuditTime(LocalDateTime.now());
            apply.setUnbindAuditOpinion(auditOpinion);
            // 重置确认状态
            apply.setStudentConfirmStatus(0);
            // 设置实习结束日期
            apply.setInternshipEndDate(java.time.LocalDate.now());
            this.updateById(apply);
            
            // 更新学生：current_apply_id = null, current_enterprise_id = null, internship_status = 2
            Student student = studentMapper.selectById(apply.getStudentId());
            if (student != null) {
                student.setCurrentApplyId(null);
                student.setCurrentEnterpriseId(null);
                student.setInternshipStatus(2); // 已离职
                studentMapper.updateById(student);
            }
        } else {
            // 如果审核拒绝（auditStatus = 3）
            apply.setUnbindStatus(3);
            apply.setUnbindAuditUserId(currentUserId);
            apply.setUnbindAuditTime(LocalDateTime.now());
            apply.setUnbindAuditOpinion(auditOpinion);
            this.updateById(apply);
        }
        
        return true;
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

