package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
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
        if (!StringUtils.hasText(report.getReportTitle())) {
            throw new BusinessException("周报标题不能为空");
        }
        if (!StringUtils.hasText(report.getWorkSummary())) {
            throw new BusinessException("本周工作总结不能为空");
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
        
        return report;
    }
    
    @Override
    public Page<InternshipWeeklyReport> getReportPage(Page<InternshipWeeklyReport> page, Long studentId, Long applyId,
                                                       Integer weekNumber, Integer reviewStatus) {
        LambdaQueryWrapper<InternshipWeeklyReport> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                // 学生只能查看自己的周报
                Student student = studentMapper.selectOne(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getUserId, user.getUserId())
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (student != null) {
                    wrapper.eq(InternshipWeeklyReport::getStudentId, student.getStudentId());
                }
                // 指导教师可以查看分配的学生的周报
                // TODO: 实现指导教师数据权限过滤
            }
        }
        
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
        
        return this.page(page, wrapper);
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
}

