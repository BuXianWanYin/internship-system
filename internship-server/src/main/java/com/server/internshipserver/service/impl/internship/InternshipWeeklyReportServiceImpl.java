package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.ReviewStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.InternshipWeeklyReportQueryDTO;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.service.internship.InternshipWeeklyReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * 周报管理Service实现类
 * 实现实习周报的提交、批阅、查询等业务功能
 */
@Service
public class InternshipWeeklyReportServiceImpl extends ServiceImpl<InternshipWeeklyReportMapper, InternshipWeeklyReport> implements InternshipWeeklyReportService {
    
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
    
    @Autowired
    private InternshipPostMapper internshipPostMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipWeeklyReport addReport(InternshipWeeklyReport report) {
        // 参数校验
        if (report.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (report.getWeekNumber() == null) {
            throw new BusinessException("周次不能为空");
        }
        if (report.getReportDate() == null) {
            throw new BusinessException("周报日期不能为空");
        }
        // 如果没有提供标题，根据周次和日期自动生成
        if (!StringUtils.hasText(report.getReportTitle())) {
            if (report.getWeekNumber() != null) {
                report.setReportTitle("第" + report.getWeekNumber() + "周实习周报");
            } else if (report.getReportDate() != null) {
                report.setReportTitle(report.getReportDate() + " 实习周报");
            } else {
                report.setReportTitle("实习周报");
            }
        }
        // 验证周报内容不能为空
        if (!StringUtils.hasText(report.getWorkContent())) {
            throw new BusinessException("周报内容不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(report.getApplyId());
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
            throw new BusinessException("无权为该申请提交周报");
        }
        
        // 检查该周次是否已提交
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipWeeklyReport::getApplyId, report.getApplyId())
               .eq(InternshipWeeklyReport::getWeekNumber, report.getWeekNumber())
               .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipWeeklyReport existReport = this.getOne(wrapper);
        if (existReport != null) {
            throw new BusinessException("该周次周报已提交");
        }
        
        // 如果前端传了 weekStartDate，根据它和 weekNumber 反推出实习开始日期，并更新申请表的日期字段
        if (report.getWeekStartDate() != null && report.getWeekNumber() != null && report.getWeekNumber() > 0) {
            updateApplyDatesFromWeekStartDate(apply, report.getWeekStartDate(), report.getWeekNumber());
            // 重新查询申请数据，获取更新后的日期
            apply = internshipApplyMapper.selectById(report.getApplyId());
        }
        
        // 设置周报信息
        report.setStudentId(student.getStudentId());
        report.setUserId(user.getUserId());
        report.setReviewStatus(ReviewStatus.PENDING.getCode()); // 未批阅
        EntityDefaultValueUtil.setDefaultValues(report);
        
        // 保存（weekStartDate 和 weekEndDate 现在是数据库字段，会自动保存）
        this.save(report);
        
        // 填充关联字段（包括日期信息）
        fillReportRelatedFields(report);
        
        return report;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipWeeklyReport updateReport(InternshipWeeklyReport report) {
        if (report.getReportId() == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        // 检查周报是否存在（只查询未删除的记录）
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipWeeklyReport::getReportId, report.getReportId())
               .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipWeeklyReport existReport = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(existReport, "周报");
        
        // 数据权限：学生只能修改自己的周报
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null && !user.getUserId().equals(existReport.getUserId())) {
            throw new BusinessException("无权修改该周报");
        }
        
        // 只有未批阅的周报才能修改
        if (existReport.getReviewStatus() != null && existReport.getReviewStatus().equals(ReviewStatus.APPROVED.getCode())) {
            throw new BusinessException("已批阅的周报不允许修改");
        }
        
        // 如果没有提供标题，根据周次和日期自动生成
        if (!StringUtils.hasText(report.getReportTitle())) {
            if (report.getWeekNumber() != null) {
                report.setReportTitle("第" + report.getWeekNumber() + "周实习周报");
            } else if (report.getReportDate() != null) {
                report.setReportTitle(report.getReportDate() + " 实习周报");
            } else {
                // 如果都没有，使用原有标题
                report.setReportTitle(existReport.getReportTitle());
            }
        }
        
        // 更新
        this.updateById(report);
        
        // 重新查询更新后的记录（只查询未删除的记录）
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipWeeklyReport::getReportId, report.getReportId())
               .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipWeeklyReport updatedReport = this.getOne(wrapper);
        fillReportRelatedFields(updatedReport);
        return updatedReport;
    }
    
    @Override
    public InternshipWeeklyReport getReportById(Long reportId) {
        if (reportId == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        // 使用查询条件确保只查询未删除的记录
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipWeeklyReport::getReportId, reportId)
               .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipWeeklyReport report = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(report, "周报");
        
        // 填充关联字段
        fillReportRelatedFields(report);
        
        return report;
    }
    
    @Override
    public Page<InternshipWeeklyReport> getReportPage(Page<InternshipWeeklyReport> page, InternshipWeeklyReportQueryDTO queryDTO) {
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipWeeklyReport::getDeleteFlag);
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (queryDTO != null) {
            if (queryDTO.getStudentId() != null) {
                wrapper.eq(InternshipWeeklyReport::getStudentId, queryDTO.getStudentId());
            }
            if (queryDTO.getApplyId() != null) {
                wrapper.eq(InternshipWeeklyReport::getApplyId, queryDTO.getApplyId());
            }
            if (queryDTO.getWeekNumber() != null) {
                wrapper.eq(InternshipWeeklyReport::getWeekNumber, queryDTO.getWeekNumber());
            }
            if (queryDTO.getReviewStatus() != null) {
                wrapper.eq(InternshipWeeklyReport::getReviewStatus, queryDTO.getReviewStatus());
            }
        }
        
        // 按周次倒序
        wrapper.orderByDesc(InternshipWeeklyReport::getWeekNumber);
        
        Page<InternshipWeeklyReport> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (InternshipWeeklyReport report : result.getRecords()) {
                fillReportRelatedFields(report);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewReport(Long reportId, String reviewComment, BigDecimal reviewScore) {
        if (reportId == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        // 使用查询条件确保只查询未删除的记录
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipWeeklyReport::getReportId, reportId)
               .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipWeeklyReport report = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(report, "周报");
        
        // 获取申请信息，判断申请类型
        InternshipApply apply = null;
        if (report.getApplyId() != null) {
            apply = internshipApplyMapper.selectById(report.getApplyId());
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
                    throw new BusinessException("无权审批该周报");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                
                // 企业管理员：可以审批本企业的所有学生周报
                if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
                    Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                    if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                            || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                        throw new BusinessException("无权审批该周报");
                    }
                }
                // 企业导师：只能审批分配给自己的学生周报
                else if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
                    Long currentUserMentorId = dataPermissionUtil.getCurrentUserMentorId();
                    if (currentUserMentorId == null || apply.getMentorId() == null
                            || !currentUserMentorId.equals(apply.getMentorId())) {
                        throw new BusinessException("无权审批该周报，只能审批分配给自己的学生周报");
                    }
                } else {
                    throw new BusinessException("无权审批该周报");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以审批
                if (currentUser == null) {
                    throw new BusinessException("无权审批该周报");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权审批该周报");
                }
                // 验证学生是否属于班主任管理的班级
                Student student = studentMapper.selectById(report.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权审批该周报");
                }
            }
        }
        
        // 验证评分范围
        if (reviewScore != null && (reviewScore.compareTo(new BigDecimal(Constants.SCORE_MIN)) < 0 || reviewScore.compareTo(new BigDecimal(Constants.SCORE_MAX)) > 0)) {
            throw new BusinessException("评分必须在" + Constants.SCORE_MIN + "-" + Constants.SCORE_MAX + "之间");
        }
        
        // 设置批阅信息
        report.setReviewStatus(ReviewStatus.APPROVED.getCode());
        report.setReviewTime(LocalDateTime.now());
        report.setReviewComment(reviewComment);
        report.setReviewScore(reviewScore);
        
        // 设置批阅人ID（指导教师）
        if (currentUser != null) {
            report.setInstructorId(currentUser.getUserId());
        }
        
        return this.updateById(report);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReport(Long reportId) {
        if (reportId == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        // 使用查询条件确保只查询未删除的记录
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(InternshipWeeklyReport::getReportId, reportId)
               .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        InternshipWeeklyReport report = this.getOne(wrapper);
        EntityValidationUtil.validateEntityExists(report, "周报");
        
        // 数据权限：学生只能删除自己的周报
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null && !user.getUserId().equals(report.getUserId())) {
            throw new BusinessException("无权删除该周报");
        }
        
        // 软删除：使用 LambdaUpdateWrapper 确保 delete_flag 字段被正确更新
        LambdaUpdateWrapper<InternshipWeeklyReport> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(InternshipWeeklyReport::getReportId, reportId)
                     .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                     .set(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.DELETED.getCode());
        boolean result = this.update(updateWrapper);
        if (!result) {
            throw new BusinessException("删除周报失败");
        }
        return result;
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<InternshipWeeklyReport> wrapper) {
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        // 学生只能查看自己的周报
        if (applyStudentFilter(wrapper)) {
            return;
        }
        
        // 学校管理员：只能查看本学校学生的周报
        if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN)) {
            applySchoolAdminFilter(wrapper);
            return;
        }
        
        // 学院负责人：只能查看本学院学生的周报
        if (dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
            applyCollegeLeaderFilter(wrapper);
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的周报
        if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
            applyClassTeacherFilter(wrapper);
            return;
        }
        
        // 企业管理员和企业导师：只能查看本企业实习学生的周报
        if (dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_ADMIN) || dataPermissionUtil.hasRole(Constants.ROLE_ENTERPRISE_MENTOR)) {
            applyEnterpriseFilter(wrapper);
            return;
        }
    }
    
    /**
     * 应用学生过滤：学生只能查看自己的周报
     */
    private boolean applyStudentFilter(LambdaQueryWrapper<InternshipWeeklyReport> wrapper) {
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
                wrapper.eq(InternshipWeeklyReport::getStudentId, student.getStudentId());
            }
            return true;
        }
        return false;
    }
    
    /**
     * 应用学校管理员过滤：只能查看本学校学生的周报
     */
    private void applySchoolAdminFilter(LambdaQueryWrapper<InternshipWeeklyReport> wrapper) {
        Long schoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (schoolId != null) {
            // 查询本学校的所有学生
             
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getSchoolId, schoolId)
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
                        List<Long> studentIds = students.stream()
                                .filter(s -> s.getUserId() != null && validUserIds.contains(s.getUserId()))
                                .map(Student::getStudentId)
                                .collect(Collectors.toList());
                        if (!studentIds.isEmpty()) {
                            wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
                        } else {
                            wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                        }
                    } else {
                        wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                    }
                } else {
                    wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                }
            } else {
                wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
            }
        }
    }
    
    /**
     * 应用学院负责人过滤：只能查看本学院学生的周报
     */
    private void applyCollegeLeaderFilter(LambdaQueryWrapper<InternshipWeeklyReport> wrapper) {
        Long collegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (collegeId != null) {
            // 查询本学院的所有学生
             
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getCollegeId, collegeId)
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
                        List<Long> studentIds = students.stream()
                                .filter(s -> s.getUserId() != null && validUserIds.contains(s.getUserId()))
                                .map(Student::getStudentId)
                                .collect(Collectors.toList());
                        if (!studentIds.isEmpty()) {
                            wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
                        } else {
                            wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                        }
                    } else {
                        wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                    }
                } else {
                    wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                }
            } else {
                wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
            }
        }
    }
    
    /**
     * 应用班主任过滤：只能查看管理的班级的学生的周报
     */
    private void applyClassTeacherFilter(LambdaQueryWrapper<InternshipWeeklyReport> wrapper) {
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
                wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
            } else {
                wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
            }
        } else {
            wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
        }
    }
    
    /**
     * 应用企业过滤：企业管理员和企业导师只能查看本企业实习学生的周报
     */
    private void applyEnterpriseFilter(LambdaQueryWrapper<InternshipWeeklyReport> wrapper) {
        List<Long> studentIds = dataPermissionUtil.getEnterpriseStudentIds();
        if (studentIds != null && !studentIds.isEmpty()) {
            wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
        } else {
            wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
        }
    }
    
    /**
     * 填充周报关联字段
     */
    private void fillReportRelatedFields(InternshipWeeklyReport report) {
        // 填充学生信息
        if (report.getStudentId() != null) {
            Student student = studentMapper.selectById(report.getStudentId());
            if (student != null) {
                report.setStudentNo(student.getStudentNo());
                if (student.getUserId() != null) {
                    UserInfo user = userMapper.selectById(student.getUserId());
                    if (user != null) {
                        report.setStudentName(user.getRealName());
                    }
                }
            }
        }
        
        // 填充企业信息、日期信息和申请类型
        if (report.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(report.getApplyId());
            if (apply != null) {
                report.setApplyType(apply.getApplyType());
                if (apply.getEnterpriseId() != null) {
                    // 合作企业申请，从企业表获取企业信息
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        report.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                    // 从岗位表获取开始和结束日期
                    if (apply.getPostId() != null) {
                        InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
                        if (post != null) {
                            if (post.getInternshipStartDate() != null) {
                                report.setStartDate(post.getInternshipStartDate());
                            }
                            if (post.getInternshipEndDate() != null) {
                                report.setEndDate(post.getInternshipEndDate());
                            }
                        }
                    }
                } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                    // 自主实习，使用自主实习企业名称和日期
                    report.setEnterpriseName(apply.getSelfEnterpriseName());
                    // 优先使用 selfStartDate 和 selfEndDate，如果为 null 则使用 internshipStartDate 和 internshipEndDate
                    if (apply.getSelfStartDate() != null) {
                        report.setStartDate(apply.getSelfStartDate());
                    } else if (apply.getInternshipStartDate() != null) {
                        report.setStartDate(apply.getInternshipStartDate());
                    }
                    if (apply.getSelfEndDate() != null) {
                        report.setEndDate(apply.getSelfEndDate());
                    } else if (apply.getInternshipEndDate() != null) {
                        report.setEndDate(apply.getInternshipEndDate());
                    }
                }
            }
        }
        
        // 根据周次和实习开始日期计算该周的开始和结束日期
        calculateWeekDates(report);
    }
    
    /**
     * 根据前端传入的 weekStartDate 和 weekNumber 反推出实习开始日期，并更新申请表的日期字段
     * @param apply 申请对象
     * @param weekStartDate 周开始日期
     * @param weekNumber 周次
     */
    private void updateApplyDatesFromWeekStartDate(InternshipApply apply, LocalDate weekStartDate, Integer weekNumber) {
        if (apply == null || weekStartDate == null || weekNumber == null || weekNumber <= 0) {
            return;
        }
        
        // 计算实习开始日期：weekStartDate 是第 weekNumber 周的周一
        // 实习开始日期应该是第1周的周一，所以需要往前推 (weekNumber - 1) 周
        LocalDate calculatedStartDate = weekStartDate.minusWeeks(weekNumber - 1);
        
        // 如果是自主实习，且 selfStartDate 或 internshipStartDate 为 null，则更新它们
        if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
            boolean needUpdate = false;
            
            if (apply.getSelfStartDate() == null) {
                apply.setSelfStartDate(calculatedStartDate);
                needUpdate = true;
            }
            
            if (apply.getInternshipStartDate() == null) {
                apply.setInternshipStartDate(calculatedStartDate);
                needUpdate = true;
            }
            
            // 如果前端还传了 weekEndDate，可以计算实习结束日期
            // 但这里我们只更新开始日期，结束日期由用户后续填写或系统自动计算
            
            if (needUpdate) {
                internshipApplyMapper.updateById(apply);
            }
        }
    }
    
    /**
     * 根据周次和实习开始日期计算该周的开始和结束日期
     * 注意：如果数据库中已经有 weekStartDate 和 weekEndDate，则不会重新计算
     * @param report 周报对象
     */
    private void calculateWeekDates(InternshipWeeklyReport report) {
        // 如果数据库中已经有 weekStartDate 和 weekEndDate，直接使用它们，不重新计算
        if (report.getWeekStartDate() != null && report.getWeekEndDate() != null) {
            return;
        }
        
        if (report.getStartDate() == null || report.getWeekNumber() == null || report.getWeekNumber() <= 0) {
            // 如果开始日期或周次为空，则无法计算
            return;
        }
        
        LocalDate internshipStartDate = report.getStartDate();
        
        // 计算第 weekNumber 周的开始日期
        // 第1周从实习开始日期所在周的周一开始
        // 找到实习开始日期所在周的周一
        DayOfWeek startDayOfWeek = internshipStartDate.getDayOfWeek();
        int daysToMonday = startDayOfWeek.getValue() - 1; // 0=周一, 6=周日
        LocalDate firstWeekMonday = internshipStartDate.minusDays(daysToMonday);
        
        // 计算第 weekNumber 周的开始日期（周一）
        LocalDate weekStartDate = firstWeekMonday.plusWeeks(report.getWeekNumber() - 1);
        
        // 计算该周的结束日期（周日）
        LocalDate weekEndDate = weekStartDate.plusDays(6);
        
        // 如果实习结束日期不为空，且计算出的周结束日期超过了实习结束日期，则使用实习结束日期
        if (report.getEndDate() != null && weekEndDate.isAfter(report.getEndDate())) {
            weekEndDate = report.getEndDate();
        }
        
        // 只有在没有设置的情况下才设置（用于查询时填充）
        if (report.getWeekStartDate() == null) {
            report.setWeekStartDate(weekStartDate);
        }
        if (report.getWeekEndDate() == null) {
            report.setWeekEndDate(weekEndDate);
        }
    }
}

