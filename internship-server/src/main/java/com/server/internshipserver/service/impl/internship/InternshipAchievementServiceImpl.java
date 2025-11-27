package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipAchievement;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.internship.InternshipAchievementMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.service.internship.InternshipAchievementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 阶段性成果管理Service实现类
 */
@Service
public class InternshipAchievementServiceImpl extends ServiceImpl<InternshipAchievementMapper, InternshipAchievement> implements InternshipAchievementService {
    
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
    public InternshipAchievement addAchievement(InternshipAchievement achievement) {
        // 参数校验
        if (achievement.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (!StringUtils.hasText(achievement.getAchievementName())) {
            throw new BusinessException("成果名称不能为空");
        }
        if (!StringUtils.hasText(achievement.getAchievementType())) {
            throw new BusinessException("成果类型不能为空");
        }
        if (!StringUtils.hasText(achievement.getFileUrls())) {
            throw new BusinessException("文件URL不能为空");
        }
        if (achievement.getSubmitDate() == null) {
            throw new BusinessException("提交日期不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(achievement.getApplyId());
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
            throw new BusinessException("无权为该申请提交成果");
        }
        
        // 设置成果信息
        achievement.setStudentId(student.getStudentId());
        achievement.setUserId(user.getUserId());
        achievement.setReviewStatus(0); // 待审核
        achievement.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(achievement);
        return achievement;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipAchievement updateAchievement(InternshipAchievement achievement) {
        if (achievement.getAchievementId() == null) {
            throw new BusinessException("成果ID不能为空");
        }
        
        // 检查成果是否存在
        InternshipAchievement existAchievement = this.getById(achievement.getAchievementId());
        if (existAchievement == null || existAchievement.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("成果不存在");
        }
        
        // 数据权限：学生只能修改自己的成果
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(existAchievement.getUserId())) {
                throw new BusinessException("无权修改该成果");
            }
        }
        
        // 只有待审核状态的成果才能修改
        if (existAchievement.getReviewStatus() != null && existAchievement.getReviewStatus() != 0) {
            throw new BusinessException("只有待审核状态的成果才能修改");
        }
        
        // 更新
        this.updateById(achievement);
        return this.getById(achievement.getAchievementId());
    }
    
    @Override
    public InternshipAchievement getAchievementById(Long achievementId) {
        if (achievementId == null) {
            throw new BusinessException("成果ID不能为空");
        }
        
        InternshipAchievement achievement = this.getById(achievementId);
        if (achievement == null || achievement.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("成果不存在");
        }
        
        // 填充关联字段
        fillAchievementRelatedFields(achievement);
        
        return achievement;
    }
    
    @Override
    public Page<InternshipAchievement> getAchievementPage(Page<InternshipAchievement> page, Long studentId, Long applyId,
                                                          String achievementType, Integer reviewStatus) {
        LambdaQueryWrapper<InternshipAchievement> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipAchievement::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(InternshipAchievement::getStudentId, studentId);
        }
        if (applyId != null) {
            wrapper.eq(InternshipAchievement::getApplyId, applyId);
        }
        if (StringUtils.hasText(achievementType)) {
            wrapper.eq(InternshipAchievement::getAchievementType, achievementType);
        }
        if (reviewStatus != null) {
            wrapper.eq(InternshipAchievement::getReviewStatus, reviewStatus);
        }
        
        // 按提交日期倒序
        wrapper.orderByDesc(InternshipAchievement::getSubmitDate);
        
        Page<InternshipAchievement> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (InternshipAchievement achievement : result.getRecords()) {
                fillAchievementRelatedFields(achievement);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewAchievement(Long achievementId, Integer reviewStatus, String reviewComment) {
        if (achievementId == null) {
            throw new BusinessException("成果ID不能为空");
        }
        if (reviewStatus == null || (reviewStatus != 1 && reviewStatus != 2)) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipAchievement achievement = this.getById(achievementId);
        if (achievement == null || achievement.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("成果不存在");
        }
        
        // 只有待审核状态的成果才能审核
        if (achievement.getReviewStatus() == null || achievement.getReviewStatus() != 0) {
            throw new BusinessException("只有待审核状态的成果才能审核");
        }
        
        // 设置审核信息
        achievement.setReviewStatus(reviewStatus);
        achievement.setReviewTime(LocalDateTime.now());
        achievement.setReviewComment(reviewComment);
        
        // 设置审核人ID（指导教师）
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                achievement.setInstructorId(user.getUserId());
            }
        }
        
        return this.updateById(achievement);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAchievement(Long achievementId) {
        if (achievementId == null) {
            throw new BusinessException("成果ID不能为空");
        }
        
        InternshipAchievement achievement = this.getById(achievementId);
        if (achievement == null || achievement.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("成果不存在");
        }
        
        // 数据权限：学生只能删除自己的成果
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(achievement.getUserId())) {
                throw new BusinessException("无权删除该成果");
            }
        }
        
        // 软删除
        achievement.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(achievement);
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<InternshipAchievement> wrapper) {
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        // 学生只能查看自己的成果
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId != null && dataPermissionUtil.hasRole("ROLE_STUDENT")) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, currentUserId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                wrapper.eq(InternshipAchievement::getStudentId, student.getStudentId());
            }
            return;
        }
        
        // 学校管理员：只能查看本学校学生的成果
        if (dataPermissionUtil.hasRole("ROLE_SCHOOL_ADMIN")) {
            Long schoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (schoolId != null) {
                // 查询本学校的所有学生
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getSchoolId, schoolId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getStudentId)
                );
                if (students != null && !students.isEmpty()) {
                    List<Long> studentIds = students.stream()
                            .map(Student::getStudentId)
                            .collect(Collectors.toList());
                    wrapper.in(InternshipAchievement::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipAchievement::getAchievementId, -1L);
                }
            }
            return;
        }
        
        // 学院负责人：只能查看本学院学生的成果
        if (dataPermissionUtil.hasRole("ROLE_COLLEGE_LEADER")) {
            Long collegeId = dataPermissionUtil.getCurrentUserCollegeId();
            if (collegeId != null) {
                // 查询本学院的所有学生
                java.util.List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getCollegeId, collegeId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getStudentId)
                );
                if (students != null && !students.isEmpty()) {
                    java.util.List<Long> studentIds = students.stream()
                            .map(Student::getStudentId)
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in(InternshipAchievement::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipAchievement::getAchievementId, -1L);
                }
            }
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的成果
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, currentUserClassIds)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getStudentId)
            );
            if (students != null && !students.isEmpty()) {
                List<Long> studentIds = students.stream()
                        .map(Student::getStudentId)
                        .collect(Collectors.toList());
                wrapper.in(InternshipAchievement::getStudentId, studentIds);
            } else {
                wrapper.eq(InternshipAchievement::getAchievementId, -1L);
            }
        }
    }
    
    /**
     * 填充成果关联字段
     */
    private void fillAchievementRelatedFields(InternshipAchievement achievement) {
        // 填充学生信息
        if (achievement.getStudentId() != null) {
            Student student = studentMapper.selectById(achievement.getStudentId());
            if (student != null) {
                achievement.setStudentNo(student.getStudentNo());
                if (student.getUserId() != null) {
                    UserInfo user = userMapper.selectById(student.getUserId());
                    if (user != null) {
                        achievement.setStudentName(user.getRealName());
                    }
                }
            }
        }
        
        // 填充企业信息
        if (achievement.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(achievement.getApplyId());
            if (apply != null) {
                if (apply.getEnterpriseId() != null) {
                    // 合作企业申请，从企业表获取企业信息
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        achievement.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                } else if (apply.getApplyType() != null && apply.getApplyType() == 2) {
                    // 自主实习，使用自主实习企业名称
                    achievement.setEnterpriseName(apply.getSelfEnterpriseName());
                }
            }
        }
        
        // 设置成果标题（用于显示）
        achievement.setAchievementTitle(achievement.getAchievementName());
    }
}

