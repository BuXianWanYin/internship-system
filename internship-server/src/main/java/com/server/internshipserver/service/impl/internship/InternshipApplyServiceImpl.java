package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.cooperation.EnterpriseSchoolCooperationService;
import com.server.internshipserver.service.internship.InternshipApplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;

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
        
        // 设置申请信息
        apply.setStudentId(student.getStudentId());
        apply.setUserId(user.getUserId());
        apply.setApplyType(2); // 自主实习
        apply.setStatus(0); // 待审核
        apply.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(apply);
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
        
        // 数据权限过滤
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                // 学生只能查看自己的申请
                if (apply.getUserId().equals(user.getUserId())) {
                    return apply;
                }
                // 企业管理员只能查看自己企业的申请
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId != null && apply.getEnterpriseId() != null
                        && currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    return apply;
                }
                // 其他角色通过数据权限过滤
                // TODO: 实现更细粒度的权限控制
            }
        }
        
        return apply;
    }
    
    @Override
    public Page<InternshipApply> getApplyPage(Page<InternshipApply> page, Long studentId, Long enterpriseId,
                                               Long postId, Integer applyType, Integer status) {
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                // 学生只能查看自己的申请
                Student student = studentMapper.selectOne(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getUserId, user.getUserId())
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (student != null) {
                    wrapper.eq(InternshipApply::getStudentId, student.getStudentId());
                }
                // 企业管理员只能查看自己企业的申请
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId != null) {
                    wrapper.eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId);
                }
            }
        }
        
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
        
        return this.page(page, wrapper);
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
        
        // 只有自主实习的待审核状态才能审核
        if (apply.getApplyType() == null || apply.getApplyType() != 2) {
            throw new BusinessException("只能审核自主实习申请");
        }
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
        
        // 软删除
        apply.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(apply);
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
}

