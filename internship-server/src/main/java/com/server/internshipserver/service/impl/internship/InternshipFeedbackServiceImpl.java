package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.FeedbackStatus;
import com.server.internshipserver.common.enums.InternshipApplyStatus;
import com.server.internshipserver.common.enums.ReplyUserType;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.InternshipFeedback;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.internship.InternshipFeedbackMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.service.internship.InternshipFeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 问题反馈管理Service实现类
 * 实现实习问题反馈的提交、处理、查询等业务功能
 */
@Service
public class InternshipFeedbackServiceImpl extends ServiceImpl<InternshipFeedbackMapper, InternshipFeedback> implements InternshipFeedbackService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipFeedback addFeedback(InternshipFeedback feedback) {
        // 参数校验
        if (feedback.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (feedback.getFeedbackType() == null) {
            throw new BusinessException("反馈类型不能为空");
        }
        if (!StringUtils.hasText(feedback.getFeedbackTitle())) {
            throw new BusinessException("反馈标题不能为空");
        }
        if (!StringUtils.hasText(feedback.getFeedbackContent())) {
            throw new BusinessException("反馈内容不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(feedback.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 获取当前登录学生信息
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, user.getUserId())
        );
        // 检查关联的user_info是否已删除
        if (student != null && student.getUserId() != null) {
            UserInfo studentUser = userMapper.selectById(student.getUserId());
            if (studentUser == null || studentUser.getDeleteFlag() == null || studentUser.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                student = null;
            }
        }
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        // 验证申请是否属于当前学生
        if (!apply.getStudentId().equals(student.getStudentId())) {
            throw new BusinessException("无权为该申请提交反馈");
        }
        
        // 设置反馈信息
        feedback.setStudentId(student.getStudentId());
        feedback.setUserId(user.getUserId());
        feedback.setFeedbackStatus(FeedbackStatus.PENDING.getCode()); // 待处理
        EntityDefaultValueUtil.setDefaultValues(feedback);
        
        // 保存
        this.save(feedback);
        return feedback;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipFeedback updateFeedback(InternshipFeedback feedback) {
        if (feedback.getFeedbackId() == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        // 检查反馈是否存在（只查询未删除的记录）
        LambdaQueryWrapper<InternshipFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipFeedback::getFeedbackId, feedback.getFeedbackId())
               .eq(InternshipFeedback::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipFeedback existFeedback = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(existFeedback, "反馈");
        
        // 数据权限：学生只能修改自己的反馈
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null && !user.getUserId().equals(existFeedback.getUserId())) {
            throw new BusinessException("无权修改该反馈");
        }
        
        // 只有待处理状态的反馈才能修改
        if (existFeedback.getFeedbackStatus() == null || !existFeedback.getFeedbackStatus().equals(FeedbackStatus.PENDING.getCode())) {
            throw new BusinessException("只有待处理状态的反馈才能修改");
        }
        
        // 更新
        this.updateById(feedback);
        
        // 重新查询更新后的记录（只查询未删除的记录）
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipFeedback::getFeedbackId, feedback.getFeedbackId())
               .eq(InternshipFeedback::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipFeedback updatedFeedback = this.getOne(wrapper);
        fillFeedbackRelatedFields(updatedFeedback);
        return updatedFeedback;
    }
    
    @Override
    public InternshipFeedback getFeedbackById(Long feedbackId) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        // 使用查询条件确保只查询未删除的记录
        LambdaQueryWrapper<InternshipFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipFeedback::getFeedbackId, feedbackId)
               .eq(InternshipFeedback::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipFeedback feedback = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(feedback, "反馈");
        
        // 填充关联字段
        fillFeedbackRelatedFields(feedback);
        
        return feedback;
    }
    
    @Override
    public Page<InternshipFeedback> getFeedbackPage(Page<InternshipFeedback> page, Long studentId, Long applyId,
                                                    Integer feedbackType, Integer feedbackStatus) {
        LambdaQueryWrapper<InternshipFeedback> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipFeedback::getDeleteFlag);
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(InternshipFeedback::getStudentId, studentId);
        }
        if (applyId != null) {
            wrapper.eq(InternshipFeedback::getApplyId, applyId);
        }
        if (feedbackType != null) {
            wrapper.eq(InternshipFeedback::getFeedbackType, feedbackType);
        }
        if (feedbackStatus != null) {
            wrapper.eq(InternshipFeedback::getFeedbackStatus, feedbackStatus);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(InternshipFeedback::getCreateTime);
        
        Page<InternshipFeedback> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (InternshipFeedback feedback : result.getRecords()) {
                fillFeedbackRelatedFields(feedback);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyFeedback(Long feedbackId, String replyContent, Integer replyUserType) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        if (!StringUtils.hasText(replyContent)) {
            throw new BusinessException("回复内容不能为空");
        }
        
        InternshipFeedback feedback = this.getById(feedbackId);
        EntityValidationUtil.validateEntityExists(feedback, "反馈");
        
        // 获取申请信息，判断申请类型
        InternshipApply apply = null;
        if (feedback.getApplyId() != null) {
            apply = internshipApplyMapper.selectById(feedback.getApplyId());
        }
        
        // 数据权限：根据申请类型判断权限
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isAdmin = false;
        if (currentUser != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            isAdmin = dataPermissionUtil.isSystemAdmin() || 
                     DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN);
        }
        
        // 根据申请类型自动设置回复人类型，并验证权限
        if (!isAdmin && apply != null) {
            if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
                // 合作企业实习：企业导师可以回复
                if (replyUserType == null || !replyUserType.equals(ReplyUserType.ENTERPRISE_MENTOR.getCode())) {
                    replyUserType = ReplyUserType.ENTERPRISE_MENTOR.getCode();
                }
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                        || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    throw new BusinessException("无权回复该反馈");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以回复
                if (replyUserType == null || !replyUserType.equals(ReplyUserType.CLASS_TEACHER.getCode())) {
                    replyUserType = ReplyUserType.CLASS_TEACHER.getCode();
                }
                if (currentUser == null) {
                    throw new BusinessException("无权回复该反馈");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权回复该反馈");
                }
                // 验证学生是否属于班主任管理的班级
                Student student = studentMapper.selectById(feedback.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权回复该反馈");
                }
            }
        } else if (replyUserType == null) {
            // 系统管理员或学校管理员需要指定回复人类型
            throw new BusinessException("回复人类型不能为空");
        }
        
        // 设置回复信息
        feedback.setReplyContent(replyContent);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setReplyUserType(replyUserType);
        feedback.setFeedbackStatus(FeedbackStatus.REPLIED.getCode());
        
        // 设置回复人ID
        if (currentUser != null) {
            feedback.setReplyUserId(currentUser.getUserId());
        }
        
        return this.updateById(feedback);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean solveFeedback(Long feedbackId) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        InternshipFeedback feedback = this.getById(feedbackId);
        EntityValidationUtil.validateEntityExists(feedback, "反馈");
        
        // 只有处理中状态的反馈才能标记为已解决
        if (feedback.getFeedbackStatus() == null || !feedback.getFeedbackStatus().equals(FeedbackStatus.REPLIED.getCode())) {
            throw new BusinessException("只有处理中状态的反馈才能标记为已解决");
        }
        
        // 更新状态为已解决
        feedback.setFeedbackStatus(FeedbackStatus.SOLVED.getCode());
        feedback.setSolveTime(LocalDateTime.now());
        
        return this.updateById(feedback);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeFeedback(Long feedbackId) {
        EntityValidationUtil.validateIdNotNull(feedbackId, "反馈ID");
        
        InternshipFeedback feedback = this.getById(feedbackId);
        EntityValidationUtil.validateEntityExists(feedback, "反馈");
        
        // 更新状态为已关闭
        feedback.setFeedbackStatus(FeedbackStatus.CLOSED.getCode());
        
        return this.updateById(feedback);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFeedback(Long feedbackId) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        // 使用查询条件确保只查询未删除的记录
        LambdaQueryWrapper<InternshipFeedback> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipFeedback::getFeedbackId, feedbackId)
               .eq(InternshipFeedback::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipFeedback feedback = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(feedback, "反馈");
        
        // 数据权限：学生只能删除自己的反馈
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null && !user.getUserId().equals(feedback.getUserId())) {
            throw new BusinessException("无权删除该反馈");
        }
        
        // 软删除：使用 LambdaUpdateWrapper 确保 delete_flag 字段被正确更新
        LambdaUpdateWrapper<InternshipFeedback> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(InternshipFeedback::getFeedbackId, feedbackId)
                     .eq(InternshipFeedback::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                     .set(InternshipFeedback::getDeleteFlag, DeleteFlag.DELETED.getCode());
        boolean result = this.update(updateWrapper);
        if (!result) {
            throw new BusinessException("删除反馈失败");
        }
        return result;
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<InternshipFeedback> wrapper) {
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        // 学生只能查看自己的反馈
        if (applyStudentFilter(wrapper)) {
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的反馈
        if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
            applyClassTeacherFilter(wrapper);
            return;
        }
        
        // 企业管理员和企业导师：可以查看本企业实习学生的反馈
        if (dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_ADMIN) || dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_MENTOR)) {
            applyEnterpriseFilter(wrapper);
            return;
        }
    }
    
    /**
     * 应用学生过滤：学生只能查看自己的反馈
     */
    private boolean applyStudentFilter(LambdaQueryWrapper<InternshipFeedback> wrapper) {
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId != null && dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, currentUserId)
            );
            // 检查关联的user_info是否已删除
            if (student != null && student.getUserId() != null) {
                UserInfo studentUser = userMapper.selectById(student.getUserId());
                if (studentUser == null || studentUser.getDeleteFlag() == null || studentUser.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                    student = null;
                }
            }
            if (student != null) {
                wrapper.eq(InternshipFeedback::getStudentId, student.getStudentId());
            }
            return true;
        }
        return false;
    }
    
    /**
     * 应用班主任过滤：只能查看管理的班级的学生的反馈
     */
    private void applyClassTeacherFilter(LambdaQueryWrapper<InternshipFeedback> wrapper) {
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
             
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, currentUserClassIds)
                            .select(Student::getStudentId, Student::getUserId)
            );
            // 通过关联user_info表过滤已删除的学生
            if (students != null && !students.isEmpty()) {
                List<Long> userIds = students.stream()
                        .map(Student::getUserId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                if (!userIds.isEmpty()) {
                    List<UserInfo> validUsers = userMapper.selectList(
                            new LambdaQueryWrapper<UserInfo>()
                                    .in(UserInfo::getUserId, userIds)
                                    .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                    .select(UserInfo::getUserId)
                    );
                    if (validUsers != null && !validUsers.isEmpty()) {
                        List<Long> validUserIds = validUsers.stream()
                                .map(UserInfo::getUserId)
                                .collect(Collectors.toList());
                        students = students.stream()
                                .filter(s -> s.getUserId() != null && validUserIds.contains(s.getUserId()))
                                .collect(Collectors.toList());
                    } else {
                        students = Collections.emptyList();
                    }
                } else {
                    students = Collections.emptyList();
                }
            }
            if (students != null && !students.isEmpty()) {
                List<Long> studentIds = students.stream()
                        .map(Student::getStudentId)
                        .collect(Collectors.toList());
                wrapper.in(InternshipFeedback::getStudentId, studentIds);
            } else {
                wrapper.eq(InternshipFeedback::getFeedbackId, -1L);
            }
        } else {
            wrapper.eq(InternshipFeedback::getFeedbackId, -1L);
        }
    }
    
    /**
     * 应用企业过滤：企业管理员和企业导师只能查看本企业实习学生的反馈
     */
    private void applyEnterpriseFilter(LambdaQueryWrapper<InternshipFeedback> wrapper) {
        List<Long> studentIds = dataPermissionUtil.getEnterpriseStudentIds();
        if (studentIds != null && !studentIds.isEmpty()) {
            wrapper.in(InternshipFeedback::getStudentId, studentIds);
        } else {
            wrapper.eq(InternshipFeedback::getFeedbackId, -1L);
        }
    }
    
    /**
     * 填充反馈关联字段
     */
    private void fillFeedbackRelatedFields(InternshipFeedback feedback) {
        // 填充学生信息
        if (feedback.getStudentId() != null) {
            Student student = studentMapper.selectById(feedback.getStudentId());
            if (student != null) {
                feedback.setStudentNo(student.getStudentNo());
                if (student.getUserId() != null) {
                    UserInfo user = userMapper.selectById(student.getUserId());
                    if (user != null) {
                        feedback.setStudentName(user.getRealName());
                    }
                }
            }
        }
        
        // 填充企业信息和申请类型
        if (feedback.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(feedback.getApplyId());
            if (apply != null) {
                feedback.setApplyType(apply.getApplyType());
                if (apply.getEnterpriseId() != null) {
                    // 合作企业申请，从企业表获取企业信息
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        feedback.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                    // 自主实习，使用自主实习企业名称
                    feedback.setEnterpriseName(apply.getSelfEnterpriseName());
                }
            }
        }
    }
}

