package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.Interview;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.mapper.internship.InterviewMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.service.internship.InterviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

/**
 * 面试管理Service实现类
 */
@Service
public class InterviewServiceImpl extends ServiceImpl<InterviewMapper, Interview> implements InterviewService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private InternshipPostMapper internshipPostMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Interview addInterview(Interview interview) {
        // 参数校验
        if (interview.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (interview.getInterviewTime() == null) {
            throw new BusinessException("面试时间不能为空");
        }
        if (interview.getInterviewType() == null) {
            throw new BusinessException("面试类型不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(interview.getApplyId());
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 如果enterpriseId或studentId为空，从申请中自动获取
        if (interview.getEnterpriseId() == null) {
            interview.setEnterpriseId(apply.getEnterpriseId());
        }
        if (interview.getStudentId() == null) {
            interview.setStudentId(apply.getStudentId());
        }
        
        // 再次校验
        if (interview.getEnterpriseId() == null) {
            throw new BusinessException("企业ID不能为空");
        }
        if (interview.getStudentId() == null) {
            throw new BusinessException("学生ID不能为空");
        }
        
        // 数据权限：企业管理员只能为自己企业的申请安排面试
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(interview.getEnterpriseId())) {
            throw new BusinessException("无权为该企业安排面试");
        }
        if (!apply.getEnterpriseId().equals(interview.getEnterpriseId())) {
            throw new BusinessException("申请与企业不匹配");
        }
        
        // 处理前端字段映射
        // videoLink -> interviewLink
        if (StringUtils.hasText(interview.getVideoLink()) && !StringUtils.hasText(interview.getInterviewLink())) {
            interview.setInterviewLink(interview.getVideoLink());
        }
        // interviewDescription -> interviewContent
        if (StringUtils.hasText(interview.getInterviewDescription()) && !StringUtils.hasText(interview.getInterviewContent())) {
            interview.setInterviewContent(interview.getInterviewDescription());
        }
        // contactPerson -> interviewer
        if (StringUtils.hasText(interview.getContactPerson()) && !StringUtils.hasText(interview.getInterviewer())) {
            interview.setInterviewer(interview.getContactPerson());
        }
        // contactPhone -> interviewPhone（仅当面试类型为电话面试时）
        if (interview.getInterviewType() == 3 && StringUtils.hasText(interview.getContactPhone()) && !StringUtils.hasText(interview.getInterviewPhone())) {
            interview.setInterviewPhone(interview.getContactPhone());
        }
        
        // 根据面试类型验证必填字段
        if (interview.getInterviewType() == 1) { // 现场面试
            if (!StringUtils.hasText(interview.getInterviewLocation())) {
                throw new BusinessException("现场面试必须填写面试地点");
            }
        } else if (interview.getInterviewType() == 2) { // 视频面试
            if (!StringUtils.hasText(interview.getInterviewLink())) {
                throw new BusinessException("视频面试必须填写面试链接");
            }
        } else if (interview.getInterviewType() == 3) { // 电话面试
            if (!StringUtils.hasText(interview.getInterviewPhone())) {
                throw new BusinessException("电话面试必须填写面试电话");
            }
        }
        
        // 设置默认值
        interview.setStatus(0); // 待确认
        interview.setStudentConfirm(0); // 未确认
        interview.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(interview);
        return interview;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Interview updateInterview(Interview interview) {
        if (interview.getInterviewId() == null) {
            throw new BusinessException("面试ID不能为空");
        }
        
        // 检查面试是否存在
        Interview existInterview = this.getById(interview.getInterviewId());
        if (existInterview == null || existInterview.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("面试不存在");
        }
        
        // 数据权限：企业管理员只能修改自己企业的面试
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(existInterview.getEnterpriseId())) {
            throw new BusinessException("无权修改该面试");
        }
        
        // 已确认或已完成的面试不允许修改
        if (existInterview.getStatus() != null && (existInterview.getStatus() == 1 || existInterview.getStatus() == 2)) {
            throw new BusinessException("已确认或已完成的面试不允许修改");
        }
        
        // 处理前端字段映射
        // videoLink -> interviewLink
        if (StringUtils.hasText(interview.getVideoLink())) {
            interview.setInterviewLink(interview.getVideoLink());
        }
        // interviewDescription -> interviewContent
        if (StringUtils.hasText(interview.getInterviewDescription())) {
            interview.setInterviewContent(interview.getInterviewDescription());
        }
        // contactPerson -> interviewer
        if (StringUtils.hasText(interview.getContactPerson())) {
            interview.setInterviewer(interview.getContactPerson());
        }
        // contactPhone -> interviewPhone（仅当面试类型为电话面试时）
        if (interview.getInterviewType() != null && interview.getInterviewType() == 3 && StringUtils.hasText(interview.getContactPhone())) {
            interview.setInterviewPhone(interview.getContactPhone());
        }
        
        // 更新
        this.updateById(interview);
        Interview updated = this.getById(interview.getInterviewId());
        fillInterviewRelatedFields(updated);
        return updated;
    }
    
    @Override
    public Interview getInterviewById(Long interviewId) {
        if (interviewId == null) {
            throw new BusinessException("面试ID不能为空");
        }
        
        Interview interview = this.getById(interviewId);
        if (interview == null || interview.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("面试不存在");
        }
        
        // 填充关联字段
        fillInterviewRelatedFields(interview);
        
        return interview;
    }
    
    @Override
    public Page<Interview> getInterviewPage(Page<Interview> page, Long applyId, Long enterpriseId,
                                            Long studentId, Integer status) {
        LambdaQueryWrapper<Interview> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Interview::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (applyId != null) {
            wrapper.eq(Interview::getApplyId, applyId);
        }
        if (enterpriseId != null) {
            wrapper.eq(Interview::getEnterpriseId, enterpriseId);
        }
        if (studentId != null) {
            wrapper.eq(Interview::getStudentId, studentId);
        }
        if (status != null) {
            wrapper.eq(Interview::getStatus, status);
        }
        
        // 按面试时间倒序
        wrapper.orderByDesc(Interview::getInterviewTime);
        
        Page<Interview> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Interview interview : result.getRecords()) {
                fillInterviewRelatedFields(interview);
            }
        }
        
        return result;
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<Interview> wrapper) {
        // 系统管理员可以查看所有面试，不添加过滤条件
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
        
        // 学生只能查看自己的面试
        if (dataPermissionUtil.hasRole("ROLE_STUDENT")) {
            applyStudentFilter(wrapper, user.getUserId());
        } else {
            // 企业管理员只能查看自己企业的面试
            applyEnterpriseFilter(wrapper);
        }
    }
    
    /**
     * 应用学生权限过滤
     */
    private void applyStudentFilter(LambdaQueryWrapper<Interview> wrapper, Long userId) {
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, userId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (student != null) {
            wrapper.eq(Interview::getStudentId, student.getStudentId());
        } else {
            // 如果没有学生信息，返回空结果
            wrapper.eq(Interview::getInterviewId, -1L);
        }
    }
    
    /**
     * 应用企业权限过滤
     */
    private void applyEnterpriseFilter(LambdaQueryWrapper<Interview> wrapper) {
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            wrapper.eq(Interview::getEnterpriseId, currentUserEnterpriseId);
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmInterview(Long interviewId, Integer confirm) {
        if (interviewId == null) {
            throw new BusinessException("面试ID不能为空");
        }
        if (confirm == null || (confirm != 1 && confirm != 2)) {
            throw new BusinessException("确认状态无效");
        }
        
        Interview interview = this.getById(interviewId);
        if (interview == null || interview.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("面试不存在");
        }
        
        // 数据权限：学生只能确认自己的面试
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            // 验证学生身份和面试的学生ID匹配
            if (dataPermissionUtil.hasRole("ROLE_STUDENT")) {
                Student student = studentMapper.selectOne(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getUserId, user.getUserId())
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (student == null) {
                    throw new BusinessException("学生信息不存在");
                }
                if (!student.getStudentId().equals(interview.getStudentId())) {
                    throw new BusinessException("无权确认该面试，只能确认自己的面试");
                }
            } else {
                throw new BusinessException("只有学生可以确认面试");
            }
        }
        
        // 只有待确认状态才能确认
        if (interview.getStatus() == null || interview.getStatus() != 0) {
            throw new BusinessException("只有待确认状态的面试才能确认");
        }
        
        // 更新确认信息
        interview.setStudentConfirm(confirm);
        interview.setStudentConfirmTime(LocalDateTime.now());
        if (confirm == 1) {
            interview.setStatus(1); // 已确认
        } else {
            interview.setStatus(3); // 已取消
        }
        
        return this.updateById(interview);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitInterviewResult(Long interviewId, Integer interviewResult, String interviewComment) {
        if (interviewId == null) {
            throw new BusinessException("面试ID不能为空");
        }
        if (interviewResult == null || (interviewResult < 1 || interviewResult > 3)) {
            throw new BusinessException("面试结果无效");
        }
        
        Interview interview = this.getById(interviewId);
        if (interview == null || interview.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("面试不存在");
        }
        
        // 数据权限：企业管理员只能提交自己企业的面试结果
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(interview.getEnterpriseId())) {
            throw new BusinessException("无权提交该面试结果");
        }
        
        // 只有已确认或已完成的面试才能提交结果
        if (interview.getStatus() == null || (interview.getStatus() != 1 && interview.getStatus() != 2)) {
            throw new BusinessException("只有已确认或已完成的面试才能提交结果");
        }
        
        // 更新面试结果
        interview.setInterviewResult(interviewResult);
        interview.setInterviewComment(interviewComment);
        interview.setInterviewFeedbackTime(LocalDateTime.now());
        if (interview.getStatus() == 1) {
            interview.setStatus(2); // 已完成
        }
        
        return this.updateById(interview);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelInterview(Long interviewId) {
        if (interviewId == null) {
            throw new BusinessException("面试ID不能为空");
        }
        
        Interview interview = this.getById(interviewId);
        if (interview == null || interview.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("面试不存在");
        }
        
        // 数据权限：企业管理员只能取消自己企业的面试
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(interview.getEnterpriseId())) {
            throw new BusinessException("无权取消该面试");
        }
        
        // 只有待确认或已确认的面试才能取消
        if (interview.getStatus() == null || (interview.getStatus() != 0 && interview.getStatus() != 1)) {
            throw new BusinessException("只有待确认或已确认的面试才能取消");
        }
        
        // 更新状态为已取消
        interview.setStatus(3);
        return this.updateById(interview);
    }
    
    /**
     * 填充面试关联字段
     */
    private void fillInterviewRelatedFields(Interview interview) {
        if (interview == null) {
            return;
        }
        
        // 填充企业名称
        if (interview.getEnterpriseId() != null) {
            Enterprise enterprise = enterpriseMapper.selectById(interview.getEnterpriseId());
            if (enterprise != null) {
                interview.setEnterpriseName(enterprise.getEnterpriseName());
            }
        }
        
        // 填充岗位名称和学生信息（从申请中获取）
        if (interview.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(interview.getApplyId());
            if (apply != null) {
                if (apply.getPostId() != null) {
                    InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
                    if (post != null) {
                        interview.setPostName(post.getPostName());
                    }
                }
                // 填充学生信息
                if (apply.getStudentId() != null) {
                    Student student = studentMapper.selectById(apply.getStudentId());
                    if (student != null) {
                        interview.setStudentNo(student.getStudentNo());
                        // 通过userId获取学生姓名
                        if (student.getUserId() != null) {
                            UserInfo user = userMapper.selectById(student.getUserId());
                            if (user != null) {
                                interview.setStudentName(user.getRealName());
                            }
                        }
                    }
                }
            }
        }
        
        // 将数据库字段映射到前端字段（用于前端显示和编辑）
        // interviewLink -> videoLink
        if (StringUtils.hasText(interview.getInterviewLink()) && !StringUtils.hasText(interview.getVideoLink())) {
            interview.setVideoLink(interview.getInterviewLink());
        }
        // interviewContent -> interviewDescription
        if (StringUtils.hasText(interview.getInterviewContent()) && !StringUtils.hasText(interview.getInterviewDescription())) {
            interview.setInterviewDescription(interview.getInterviewContent());
        }
        // interviewer -> contactPerson
        if (StringUtils.hasText(interview.getInterviewer()) && !StringUtils.hasText(interview.getContactPerson())) {
            interview.setContactPerson(interview.getInterviewer());
        }
        // interviewPhone -> contactPhone（仅当面试类型为电话面试时）
        if (interview.getInterviewType() != null && interview.getInterviewType() == 3 
                && StringUtils.hasText(interview.getInterviewPhone()) && !StringUtils.hasText(interview.getContactPhone())) {
            interview.setContactPhone(interview.getInterviewPhone());
        }
    }
}

