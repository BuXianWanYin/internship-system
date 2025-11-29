package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.ReviewStatus;
import com.server.internshipserver.common.enums.InternshipApplyStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
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
        // 文件URL改为非必填，允许不上传附件
        // if (!StringUtils.hasText(achievement.getFileUrls())) {
        //     throw new BusinessException("文件URL不能为空");
        // }
        if (achievement.getSubmitDate() == null) {
            throw new BusinessException("提交日期不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(achievement.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 获取当前登录学生信息
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        
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
        achievement.setReviewStatus(ReviewStatus.PENDING.getCode()); // 待审核
        EntityDefaultValueUtil.setDefaultValues(achievement);
        
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
        EntityValidationUtil.validateEntityExists(existAchievement, "成果");
        
        // 数据权限：学生只能修改自己的成果
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null || !user.getUserId().equals(existAchievement.getUserId())) {
            throw new BusinessException("无权修改该成果");
        }
        
        // 只有待审核状态的成果才能修改
        if (existAchievement.getReviewStatus() != null && !existAchievement.getReviewStatus().equals(ReviewStatus.PENDING.getCode())) {
            throw new BusinessException("只有待审核状态的成果才能修改");
        }
        
        // 使用LambdaUpdateWrapper确保null值也能正确更新
        com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<InternshipAchievement> updateWrapper = 
            new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();
        updateWrapper.eq(InternshipAchievement::getAchievementId, achievement.getAchievementId())
                    .set(InternshipAchievement::getAchievementName, achievement.getAchievementName())
                    .set(InternshipAchievement::getAchievementType, achievement.getAchievementType())
                    .set(InternshipAchievement::getAchievementDescription, achievement.getAchievementDescription())
                    .set(InternshipAchievement::getFileUrls, achievement.getFileUrls()) // 允许设置为null
                    .set(InternshipAchievement::getSubmitDate, achievement.getSubmitDate());
        
        this.update(updateWrapper);
        return this.getById(achievement.getAchievementId());
    }
    
    @Override
    public InternshipAchievement getAchievementById(Long achievementId) {
        if (achievementId == null) {
            throw new BusinessException("成果ID不能为空");
        }
        
        InternshipAchievement achievement = this.getById(achievementId);
        EntityValidationUtil.validateEntityExists(achievement, "成果");
        
        // 填充关联字段
        fillAchievementRelatedFields(achievement);
        
        return achievement;
    }
    
    @Override
    public Page<InternshipAchievement> getAchievementPage(Page<InternshipAchievement> page, Long studentId, Long applyId,
                                                          String achievementType, Integer reviewStatus) {
        LambdaQueryWrapper<InternshipAchievement> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(InternshipAchievement::getDeleteFlag);
        
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
        if (EntityValidationUtil.hasRecords(result)) {
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
        if (reviewStatus == null || (!reviewStatus.equals(ReviewStatus.APPROVED.getCode()) && !reviewStatus.equals(ReviewStatus.REJECTED.getCode()))) {
            throw new BusinessException("审核状态无效");
        }
        
        InternshipAchievement achievement = this.getById(achievementId);
        EntityValidationUtil.validateEntityExists(achievement, "成果");
        
        // 只有待审核状态的成果才能审核
        if (achievement.getReviewStatus() == null || !achievement.getReviewStatus().equals(ReviewStatus.PENDING.getCode())) {
            throw new BusinessException("只有待审核状态的成果才能审核");
        }
        
        // 获取申请信息，判断申请类型
        InternshipApply apply = null;
        if (achievement.getApplyId() != null) {
            apply = internshipApplyMapper.selectById(achievement.getApplyId());
        }
        
        // 数据权限：根据申请类型判断权限
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isAdmin = false;
        if (currentUser != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            isAdmin = dataPermissionUtil.isSystemAdmin() || 
                     DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN);
        }
        
        if (!isAdmin && apply != null) {
            if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
                // 合作企业实习：企业导师可以审批（需要是该学生的导师）
                if (currentUser == null) {
                    throw new BusinessException("无权审批该成果");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                
                // 企业管理员：可以审批本企业的所有学生成果
                if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
                    Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                    if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                            || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                        throw new BusinessException("无权审批该成果");
                    }
                }
                // 企业导师：只能审批分配给自己的学生成果
                else if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
                    Long currentUserMentorId = dataPermissionUtil.getCurrentUserMentorId();
                    if (currentUserMentorId == null || apply.getMentorId() == null
                            || !currentUserMentorId.equals(apply.getMentorId())) {
                        throw new BusinessException("无权审批该成果，只能审批分配给自己的学生成果");
                    }
                } else {
                    throw new BusinessException("无权审批该成果");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以审批
                if (currentUser == null) {
                    throw new BusinessException("无权审批该成果");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权审批该成果");
                }
                // 验证学生是否属于班主任管理的班级
                Student student = studentMapper.selectById(achievement.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权审批该成果");
                }
            }
        }
        
        // 设置审核信息
        achievement.setReviewStatus(reviewStatus);
        achievement.setReviewTime(LocalDateTime.now());
        achievement.setReviewComment(reviewComment);
        
        // 设置审核人ID（指导教师）
        if (currentUser != null) {
            achievement.setInstructorId(currentUser.getUserId());
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
        EntityValidationUtil.validateEntityExists(achievement, "成果");
        
        // 数据权限：学生只能删除自己的成果
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null || !user.getUserId().equals(achievement.getUserId())) {
            throw new BusinessException("无权删除该成果");
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
        if (applyStudentFilter(wrapper)) {
            return;
        }
        
        // 学校管理员：只能查看本学校学生的成果
        if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN)) {
            applySchoolAdminFilter(wrapper);
            return;
        }
        
        // 学院负责人：只能查看本学院学生的成果
        if (dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
            applyCollegeLeaderFilter(wrapper);
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的成果
        if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
            applyClassTeacherFilter(wrapper);
            return;
        }
        
        // 企业管理员和企业导师：只能查看本企业实习学生的成果
        if (dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_ADMIN) || dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_MENTOR)) {
            applyEnterpriseFilter(wrapper);
            return;
        }
    }
    
    /**
     * 应用学生过滤：学生只能查看自己的成果
     */
    private boolean applyStudentFilter(LambdaQueryWrapper<InternshipAchievement> wrapper) {
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId != null && dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, currentUserId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                wrapper.eq(InternshipAchievement::getStudentId, student.getStudentId());
            }
            return true;
        }
        return false;
    }
    
    /**
     * 应用学校管理员过滤：只能查看本学校学生的成果
     */
    private void applySchoolAdminFilter(LambdaQueryWrapper<InternshipAchievement> wrapper) {
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
    }
    
    /**
     * 应用学院负责人过滤：只能查看本学院学生的成果
     */
    private void applyCollegeLeaderFilter(LambdaQueryWrapper<InternshipAchievement> wrapper) {
        Long collegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (collegeId != null) {
            // 查询本学院的所有学生
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
                wrapper.in(InternshipAchievement::getStudentId, studentIds);
            } else {
                wrapper.eq(InternshipAchievement::getAchievementId, -1L);
            }
        }
    }
    
    /**
     * 应用班主任过滤：只能查看管理的班级的学生的成果
     */
    private void applyClassTeacherFilter(LambdaQueryWrapper<InternshipAchievement> wrapper) {
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
        } else {
            wrapper.eq(InternshipAchievement::getAchievementId, -1L);
        }
    }
    
    /**
     * 应用企业过滤：企业管理员和企业导师只能查看本企业实习学生的成果
     */
    private void applyEnterpriseFilter(LambdaQueryWrapper<InternshipAchievement> wrapper) {
        List<Long> studentIds = dataPermissionUtil.getEnterpriseStudentIds();
        if (studentIds != null && !studentIds.isEmpty()) {
            wrapper.in(InternshipAchievement::getStudentId, studentIds);
        } else {
            wrapper.eq(InternshipAchievement::getAchievementId, -1L);
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
        
        // 填充企业信息和申请类型
        if (achievement.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(achievement.getApplyId());
            if (apply != null) {
                achievement.setApplyType(apply.getApplyType());
                if (apply.getEnterpriseId() != null) {
                    // 合作企业申请，从企业表获取企业信息
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        achievement.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                    // 自主实习，使用自主实习企业名称
                    achievement.setEnterpriseName(apply.getSelfEnterpriseName());
                }
            }
        }
        
        // 设置成果标题（用于显示）
        achievement.setAchievementTitle(achievement.getAchievementName());
    }
}

