package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
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

/**
 * 问题反馈管理Service实现类
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
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
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
        
        // 验证申请是否属于当前学生
        if (!apply.getStudentId().equals(student.getStudentId())) {
            throw new BusinessException("无权为该申请提交反馈");
        }
        
        // 设置反馈信息
        feedback.setStudentId(student.getStudentId());
        feedback.setUserId(user.getUserId());
        feedback.setFeedbackStatus(0); // 待处理
        feedback.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
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
        
        // 检查反馈是否存在
        InternshipFeedback existFeedback = this.getById(feedback.getFeedbackId());
        if (existFeedback == null || existFeedback.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("反馈不存在");
        }
        
        // 数据权限：学生只能修改自己的反馈
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(existFeedback.getUserId())) {
                throw new BusinessException("无权修改该反馈");
            }
        }
        
        // 只有待处理状态的反馈才能修改
        if (existFeedback.getFeedbackStatus() == null || existFeedback.getFeedbackStatus() != 0) {
            throw new BusinessException("只有待处理状态的反馈才能修改");
        }
        
        // 更新
        this.updateById(feedback);
        return this.getById(feedback.getFeedbackId());
    }
    
    @Override
    public InternshipFeedback getFeedbackById(Long feedbackId) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        InternshipFeedback feedback = this.getById(feedbackId);
        if (feedback == null || feedback.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("反馈不存在");
        }
        
        // 填充关联字段
        fillFeedbackRelatedFields(feedback);
        
        return feedback;
    }
    
    @Override
    public Page<InternshipFeedback> getFeedbackPage(Page<InternshipFeedback> page, Long studentId, Long applyId,
                                                    Integer feedbackType, Integer feedbackStatus) {
        LambdaQueryWrapper<InternshipFeedback> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipFeedback::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
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
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
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
        if (replyUserType == null || (replyUserType != 1 && replyUserType != 2)) {
            throw new BusinessException("回复人类型无效");
        }
        
        InternshipFeedback feedback = this.getById(feedbackId);
        if (feedback == null || feedback.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("反馈不存在");
        }
        
        // 设置回复信息
        feedback.setReplyContent(replyContent);
        feedback.setReplyTime(LocalDateTime.now());
        feedback.setReplyUserType(replyUserType);
        feedback.setFeedbackStatus(1); // 处理中
        
        // 设置回复人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                feedback.setReplyUserId(user.getUserId());
            }
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
        if (feedback == null || feedback.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("反馈不存在");
        }
        
        // 只有处理中状态的反馈才能标记为已解决
        if (feedback.getFeedbackStatus() == null || feedback.getFeedbackStatus() != 1) {
            throw new BusinessException("只有处理中状态的反馈才能标记为已解决");
        }
        
        // 更新状态为已解决
        feedback.setFeedbackStatus(2);
        feedback.setSolveTime(LocalDateTime.now());
        
        return this.updateById(feedback);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean closeFeedback(Long feedbackId) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        InternshipFeedback feedback = this.getById(feedbackId);
        if (feedback == null || feedback.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("反馈不存在");
        }
        
        // 更新状态为已关闭
        feedback.setFeedbackStatus(3);
        
        return this.updateById(feedback);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteFeedback(Long feedbackId) {
        if (feedbackId == null) {
            throw new BusinessException("反馈ID不能为空");
        }
        
        InternshipFeedback feedback = this.getById(feedbackId);
        if (feedback == null || feedback.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("反馈不存在");
        }
        
        // 数据权限：学生只能删除自己的反馈
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(feedback.getUserId())) {
                throw new BusinessException("无权删除该反馈");
            }
        }
        
        // 软删除
        feedback.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(feedback);
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
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId != null && dataPermissionUtil.hasRole("ROLE_STUDENT")) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, currentUserId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                wrapper.eq(InternshipFeedback::getStudentId, student.getStudentId());
            }
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的反馈
        java.util.List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            java.util.List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, currentUserClassIds)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getStudentId)
            );
            if (students != null && !students.isEmpty()) {
                java.util.List<Long> studentIds = students.stream()
                        .map(Student::getStudentId)
                        .collect(java.util.stream.Collectors.toList());
                wrapper.in(InternshipFeedback::getStudentId, studentIds);
            } else {
                wrapper.eq(InternshipFeedback::getFeedbackId, -1L);
            }
        }
        // 班主任：可以查看分配的学生的反馈（通过日志/周报/成果中的instructor_id关联，已合并到 ROLE_CLASS_TEACHER）
        if (dataPermissionUtil.hasRole("ROLE_CLASS_TEACHER")) {
            Long currentUserTeacherId = dataPermissionUtil.getCurrentUserTeacherId();
            if (currentUserTeacherId != null) {
                // 通过实习申请关联，查找该教师指导的学生的反馈
                // 先查找该教师指导的学生的申请（通过日志/周报/成果中的instructor_id反推）
                // 由于反馈表没有instructor_id，需要通过apply_id关联到学生，再通过学生的日志/周报/成果中的instructor_id判断
                // 简化方案：指导教师可以查看同学院学生的反馈
                Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
                if (currentUserCollegeId != null) {
                    // 查找该学院的所有学生
                    java.util.List<Student> students = studentMapper.selectList(
                            new LambdaQueryWrapper<Student>()
                                    .eq(Student::getCollegeId, currentUserCollegeId)
                                    .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                    .select(Student::getStudentId)
                    );
                    if (students != null && !students.isEmpty()) {
                        java.util.List<Long> studentIds = students.stream()
                                .map(Student::getStudentId)
                                .collect(java.util.stream.Collectors.toList());
                        wrapper.in(InternshipFeedback::getStudentId, studentIds);
                    } else {
                        wrapper.eq(InternshipFeedback::getFeedbackId, -1L);
                    }
                }
            }
            return;
        }
        
        // 企业导师：可以查看本企业实习学生的反馈
        if (dataPermissionUtil.hasRole("ROLE_ENTERPRISE_MENTOR")) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId != null) {
                // 查找该企业的所有实习申请
                java.util.List<InternshipApply> applies = internshipApplyMapper.selectList(
                        new LambdaQueryWrapper<InternshipApply>()
                                .eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId)
                                .eq(InternshipApply::getStatus, 1) // 已通过的申请
                                .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(InternshipApply::getStudentId)
                );
                if (applies != null && !applies.isEmpty()) {
                    java.util.List<Long> studentIds = applies.stream()
                            .map(InternshipApply::getStudentId)
                            .distinct()
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in(InternshipFeedback::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipFeedback::getFeedbackId, -1L);
                }
            }
            return;
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
        
        // 填充企业信息
        if (feedback.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(feedback.getApplyId());
            if (apply != null) {
                if (apply.getEnterpriseId() != null) {
                    // 合作企业申请，从企业表获取企业信息
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        feedback.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                } else if (apply.getApplyType() != null && apply.getApplyType() == 2) {
                    // 自主实习，使用自主实习企业名称
                    feedback.setEnterpriseName(apply.getSelfEnterpriseName());
                }
            }
        }
    }
}

