package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.ReviewStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.service.internship.InternshipLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
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
        // 标题据日志日期自动生成
        if (!StringUtils.hasText(log.getLogTitle())) {
            log.setLogTitle(log.getLogDate() + " 实习日志");
        }

        // 验证日志内容不能为空
        if (!StringUtils.hasText(log.getLogContent())) {
            throw new BusinessException("日志内容不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(log.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 获取当前登录学生信息
        UserInfo user = UserUtil.getCurrentUser(userMapper);
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
        EntityValidationUtil.validateEntityExists(existLog, "日志");
        
        // 数据权限：学生只能修改自己的日志
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null) {
            if (user == null || !user.getUserId().equals(existLog.getUserId())) {
                throw new BusinessException("无权修改该日志");
            }
        }
        
        // 只有未批阅的日志才能修改
        if (existLog.getReviewStatus() != null && existLog.getReviewStatus().equals(ReviewStatus.APPROVED.getCode())) {
            throw new BusinessException("已批阅的日志不允许修改");
        }
        
        // 如果没有提供标题，根据日志日期自动生成
        if (!StringUtils.hasText(log.getLogTitle()) && log.getLogDate() != null) {
            log.setLogTitle(log.getLogDate() + " 实习日志");
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
        EntityValidationUtil.validateEntityExists(log, "日志");
        
        // 填充关联字段
        fillLogRelatedFields(log);
        
        return log;
    }
    
    @Override
    public Page<InternshipLog> getLogPage(Page<InternshipLog> page, Long studentId, Long applyId, 
                                          LocalDate logDate, Integer reviewStatus) {
        LambdaQueryWrapper<InternshipLog> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(InternshipLog::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
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
        
        Page<InternshipLog> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (InternshipLog log : result.getRecords()) {
                fillLogRelatedFields(log);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean reviewLog(Long logId, String reviewComment, BigDecimal reviewScore) {
        if (logId == null) {
            throw new BusinessException("日志ID不能为空");
        }
        
        InternshipLog log = this.getById(logId);
        EntityValidationUtil.validateEntityExists(log, "日志");
        
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
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null) {
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
        EntityValidationUtil.validateEntityExists(log, "日志");
        
        // 数据权限：学生只能删除自己的日志
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null) {
            if (user == null || !user.getUserId().equals(log.getUserId())) {
                throw new BusinessException("无权删除该日志");
            }
        }
        
        // 软删除
        log.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(log);
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<InternshipLog> wrapper) {
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        // 学生只能查看自己的日志
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        if (currentUserId != null && dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, currentUserId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                wrapper.eq(InternshipLog::getStudentId, student.getStudentId());
            }
            return;
        }
        
        // 学校管理员：只能查看本学校学生的日志
        if (dataPermissionUtil.hasRole(Constants.ROLE_SCHOOL_ADMIN)) {
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
                    wrapper.in(InternshipLog::getStudentId, studentIds);
                } else {
                    // 如果没有学生，返回空结果
                    wrapper.eq(InternshipLog::getLogId, -1L);
                }
            }
            return;
        }
        
        // 学院负责人：只能查看本学院学生的日志
        if (dataPermissionUtil.hasRole(Constants.ROLE_COLLEGE_LEADER)) {
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
                    wrapper.in(InternshipLog::getStudentId, studentIds);
                } else {
                    // 如果没有学生，返回空结果
                    wrapper.eq(InternshipLog::getLogId, -1L);
                }
            }
            return;
        }
        
        // 班主任：只能查看管理的班级的学生的日志
        if (dataPermissionUtil.hasRole(Constants.ROLE_CLASS_TEACHER)) {
            List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
            if (classIds != null && !classIds.isEmpty()) {
                // 查询管理的班级的所有学生
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                                .in(Student::getClassId, classIds)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getStudentId)
                );
                if (students != null && !students.isEmpty()) {
                    List<Long> studentIds = students.stream()
                            .map(Student::getStudentId)
                            .collect(Collectors.toList());
                    wrapper.in(InternshipLog::getStudentId, studentIds);
                } else {
                    // 如果没有学生，返回空结果
                    wrapper.eq(InternshipLog::getLogId, -1L);
                }
            } else {
                // 如果没有管理的班级，返回空结果
                wrapper.eq(InternshipLog::getLogId, -1L);
            }
            return;
        }
    }
    
    /**
     * 填充日志关联字段
     */
    private void fillLogRelatedFields(InternshipLog log) {
        // 填充学生信息
        if (log.getStudentId() != null) {
            Student student = studentMapper.selectById(log.getStudentId());
            if (student != null) {
                log.setStudentNo(student.getStudentNo());
                // 通过userId获取学生姓名
                if (student.getUserId() != null) {
                    UserInfo user = userMapper.selectById(student.getUserId());
                    if (user != null) {
                        log.setStudentName(user.getRealName());
                    }
                }
            }
        }
        
        // 填充企业信息
        if (log.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(log.getApplyId());
            if (apply != null) {
                if (apply.getEnterpriseId() != null) {
                    // 合作企业申请，从企业表获取企业信息
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        log.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                    // 自主实习，使用自主实习企业名称
                    log.setEnterpriseName(apply.getSelfEnterpriseName());
                }
            }
        }
    }
}

