package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.internship.InternshipApply;
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
import java.time.LocalDateTime;

/**
 * 周报管理Service实现类
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
        
        // 设置周报信息
        report.setStudentId(student.getStudentId());
        report.setUserId(user.getUserId());
        report.setReviewStatus(0); // 未批阅
        report.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(report);
        return report;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipWeeklyReport updateReport(InternshipWeeklyReport report) {
        if (report.getReportId() == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        // 检查周报是否存在
        InternshipWeeklyReport existReport = this.getById(report.getReportId());
        if (existReport == null || existReport.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("周报不存在");
        }
        
        // 数据权限：学生只能修改自己的周报
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(existReport.getUserId())) {
                throw new BusinessException("无权修改该周报");
            }
        }
        
        // 只有未批阅的周报才能修改
        if (existReport.getReviewStatus() != null && existReport.getReviewStatus() == 1) {
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
        return this.getById(report.getReportId());
    }
    
    @Override
    public InternshipWeeklyReport getReportById(Long reportId) {
        if (reportId == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        InternshipWeeklyReport report = this.getById(reportId);
        if (report == null || report.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("周报不存在");
        }
        
        // 填充关联字段
        fillReportRelatedFields(report);
        
        return report;
    }
    
    @Override
    public Page<InternshipWeeklyReport> getReportPage(Page<InternshipWeeklyReport> page, Long studentId, Long applyId,
                                                       Integer weekNumber, Integer reviewStatus) {
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(InternshipWeeklyReport::getStudentId, studentId);
        }
        if (applyId != null) {
            wrapper.eq(InternshipWeeklyReport::getApplyId, applyId);
        }
        if (weekNumber != null) {
            wrapper.eq(InternshipWeeklyReport::getWeekNumber, weekNumber);
        }
        if (reviewStatus != null) {
            wrapper.eq(InternshipWeeklyReport::getReviewStatus, reviewStatus);
        }
        
        // 按周次倒序
        wrapper.orderByDesc(InternshipWeeklyReport::getWeekNumber);
        
        Page<InternshipWeeklyReport> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
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
        
        InternshipWeeklyReport report = this.getById(reportId);
        if (report == null || report.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("周报不存在");
        }
        
        // 验证评分范围
        if (reviewScore != null && (reviewScore.compareTo(BigDecimal.ZERO) < 0 || reviewScore.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("评分必须在0-100之间");
        }
        
        // 设置批阅信息
        report.setReviewStatus(1); // 已批阅
        report.setReviewTime(LocalDateTime.now());
        report.setReviewComment(reviewComment);
        report.setReviewScore(reviewScore);
        
        // 设置批阅人ID（指导教师）
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                report.setInstructorId(user.getUserId());
            }
        }
        
        return this.updateById(report);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteReport(Long reportId) {
        if (reportId == null) {
            throw new BusinessException("周报ID不能为空");
        }
        
        InternshipWeeklyReport report = this.getById(reportId);
        if (report == null || report.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("周报不存在");
        }
        
        // 数据权限：学生只能删除自己的周报
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(report.getUserId())) {
                throw new BusinessException("无权删除该周报");
            }
        }
        
        // 软删除
        report.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(report);
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
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId != null && dataPermissionUtil.hasRole("ROLE_STUDENT")) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, currentUserId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                wrapper.eq(InternshipWeeklyReport::getStudentId, student.getStudentId());
            }
            return;
        }
        
        // 学校管理员：只能查看本学校学生的周报
        if (dataPermissionUtil.hasRole("ROLE_SCHOOL_ADMIN")) {
            Long schoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (schoolId != null) {
                // 查询本学校的所有学生
                java.util.List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getSchoolId, schoolId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getStudentId)
                );
                if (students != null && !students.isEmpty()) {
                    java.util.List<Long> studentIds = students.stream()
                            .map(Student::getStudentId)
                            .collect(java.util.stream.Collectors.toList());
                    wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                }
            }
            return;
        }
        
        // 学院负责人：只能查看本学院学生的周报
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
                    wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
                }
            }
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的周报
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
                wrapper.in(InternshipWeeklyReport::getStudentId, studentIds);
            } else {
                wrapper.eq(InternshipWeeklyReport::getReportId, -1L);
            }
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
        
        // 填充企业信息和日期信息
        if (report.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(report.getApplyId());
            if (apply != null) {
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
                } else if (apply.getApplyType() != null && apply.getApplyType() == 2) {
                    // 自主实习，使用自主实习企业名称和日期
                    report.setEnterpriseName(apply.getSelfEnterpriseName());
                    if (apply.getSelfStartDate() != null) {
                        report.setStartDate(apply.getSelfStartDate());
                    }
                    if (apply.getSelfEndDate() != null) {
                        report.setEndDate(apply.getSelfEndDate());
                    }
                }
            }
        }
        
        // 设置周开始和结束日期（用于前端显示）
        report.setWeekStartDate(report.getStartDate());
        report.setWeekEndDate(report.getEndDate());
    }
}

