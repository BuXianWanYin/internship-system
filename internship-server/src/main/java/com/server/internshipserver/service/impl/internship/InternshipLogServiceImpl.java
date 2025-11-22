package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.internship.InternshipLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * 实习日志管理Service实现类
 */
@Service
public class InternshipLogServiceImpl extends ServiceImpl<InternshipLogMapper, InternshipLog> implements InternshipLogService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipLog addLog(InternshipLog log) {
        // 参数校验
        if (log.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (log.getLogDate() == null) {
            throw new BusinessException("日志日期不能为空");
        }
        if (!StringUtils.hasText(log.getLogTitle())) {
            throw new BusinessException("日志标题不能为空");
        }
        if (!StringUtils.hasText(log.getLogContent())) {
            throw new BusinessException("日志内容不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(log.getApplyId());
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
            throw new BusinessException("无权为该申请提交日志");
        }
        
        // 设置日志信息
        log.setStudentId(student.getStudentId());
        log.setUserId(user.getUserId());
        log.setReviewStatus(0); // 未批阅
        log.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(log);
        return log;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public InternshipLog updateLog(InternshipLog log) {
        if (log.getLogId() == null) {
            throw new BusinessException("日志ID不能为空");
        }
        
        // 检查日志是否存在
        InternshipLog existLog = this.getById(log.getLogId());
        if (existLog == null || existLog.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("日志不存在");
        }
        
        // 数据权限：学生只能修改自己的日志
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(existLog.getUserId())) {
                throw new BusinessException("无权修改该日志");
            }
        }
        
        // 只有未批阅的日志才能修改
        if (existLog.getReviewStatus() != null && existLog.getReviewStatus() == 1) {
            throw new BusinessException("已批阅的日志不允许修改");
        }
        
        // 更新
        this.updateById(log);
        return this.getById(log.getLogId());
    }
    
    @Override
    public InternshipLog getLogById(Long logId) {
        if (logId == null) {
            throw new BusinessException("日志ID不能为空");
        }
        
        InternshipLog log = this.getById(logId);
        if (log == null || log.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("日志不存在");
        }
        
        return log;
    }
    
    @Override
    public Page<InternshipLog> getLogPage(Page<InternshipLog> page, Long studentId, Long applyId, 
                                          LocalDate logDate, Integer reviewStatus) {
        LambdaQueryWrapper<InternshipLog> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipLog::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                // 学生只能查看自己的日志
                Student student = studentMapper.selectOne(
                        new LambdaQueryWrapper<Student>()
                                .eq(Student::getUserId, user.getUserId())
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (student != null) {
                    wrapper.eq(InternshipLog::getStudentId, student.getStudentId());
                }
                // 指导教师可以查看分配的学生的日志
                // TODO: 实现指导教师数据权限过滤
            }
        }
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(InternshipLog::getStudentId, studentId);
        }
        if (applyId != null) {
            wrapper.eq(InternshipLog::getApplyId, applyId);
        }
        if (logDate != null) {
            wrapper.eq(InternshipLog::getLogDate, logDate);
        }
        if (reviewStatus != null) {
            wrapper.eq(InternshipLog::getReviewStatus, reviewStatus);
        }
        
        // 按日志日期倒序
        wrapper.orderByDesc(InternshipLog::getLogDate);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewLog(Long logId, String reviewComment, BigDecimal reviewScore) {
        if (logId == null) {
            throw new BusinessException("日志ID不能为空");
        }
        
        InternshipLog log = this.getById(logId);
        if (log == null || log.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("日志不存在");
        }
        
        // 验证评分范围
        if (reviewScore != null && (reviewScore.compareTo(BigDecimal.ZERO) < 0 || reviewScore.compareTo(new BigDecimal("100")) > 0)) {
            throw new BusinessException("评分必须在0-100之间");
        }
        
        // 设置批阅信息
        log.setReviewStatus(1); // 已批阅
        log.setReviewTime(LocalDateTime.now());
        log.setReviewComment(reviewComment);
        log.setReviewScore(reviewScore);
        
        // 设置批阅人ID（指导教师）
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                log.setInstructorId(user.getUserId());
            }
        }
        
        return this.updateById(log);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteLog(Long logId) {
        if (logId == null) {
            throw new BusinessException("日志ID不能为空");
        }
        
        InternshipLog log = this.getById(logId);
        if (log == null || log.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("日志不存在");
        }
        
        // 数据权限：学生只能删除自己的日志
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user == null || !user.getUserId().equals(log.getUserId())) {
                throw new BusinessException("无权删除该日志");
            }
        }
        
        // 软删除
        log.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(log);
    }
}

