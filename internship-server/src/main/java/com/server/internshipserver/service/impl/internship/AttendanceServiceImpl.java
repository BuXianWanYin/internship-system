package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.SecurityUtil;
import com.server.internshipserver.domain.internship.Attendance;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.dto.AttendanceStatistics;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.mapper.internship.AttendanceMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.service.internship.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.math.BigDecimal;
import java.time.Duration;

/**
 * 考勤管理Service实现类
 */
@Service
public class AttendanceServiceImpl extends ServiceImpl<AttendanceMapper, Attendance> implements AttendanceService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance addAttendance(Attendance attendance) {
        // 参数校验
        if (attendance.getApplyId() == null) {
            throw new BusinessException("申请ID不能为空");
        }
        if (attendance.getStudentId() == null) {
            throw new BusinessException("学生ID不能为空");
        }
        if (attendance.getAttendanceDate() == null) {
            throw new BusinessException("考勤日期不能为空");
        }
        if (attendance.getAttendanceType() == null) {
            throw new BusinessException("考勤类型不能为空");
        }
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(attendance.getApplyId());
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("申请不存在");
        }
        
        // 数据权限：企业管理员或企业导师只能为本企业的实习生确认考勤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权为该申请确认考勤");
        }
        
        // 验证学生是否属于该申请
        if (!apply.getStudentId().equals(attendance.getStudentId())) {
            throw new BusinessException("学生与申请不匹配");
        }
        
        // 检查该日期是否已确认考勤
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, attendance.getApplyId())
               .eq(Attendance::getAttendanceDate, attendance.getAttendanceDate())
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        if (existAttendance != null) {
            throw new BusinessException("该日期考勤已确认");
        }
        
        // 请假类型需要填写请假原因
        if (attendance.getAttendanceType() == 4 && !StringUtils.hasText(attendance.getLeaveReason())) {
            throw new BusinessException("请假类型必须填写请假原因");
        }
        
        // 设置考勤信息
        attendance.setUserId(apply.getUserId());
        attendance.setConfirmStatus(0); // 待确认
        attendance.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 设置确认人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                attendance.setConfirmUserId(user.getUserId());
                attendance.setConfirmStatus(1); // 已确认
                attendance.setConfirmTime(LocalDateTime.now());
            }
        }
        
        // 保存
        this.save(attendance);
        return attendance;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAddAttendance(List<Attendance> attendanceList) {
        if (attendanceList == null || attendanceList.isEmpty()) {
            throw new BusinessException("考勤列表不能为空");
        }
        
        for (Attendance attendance : attendanceList) {
            addAttendance(attendance);
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance updateAttendance(Attendance attendance) {
        if (attendance.getAttendanceId() == null) {
            throw new BusinessException("考勤ID不能为空");
        }
        
        // 检查考勤是否存在
        Attendance existAttendance = this.getById(attendance.getAttendanceId());
        if (existAttendance == null || existAttendance.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("考勤不存在");
        }
        
        // 数据权限：企业管理员或企业导师只能修改本企业的考勤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null) {
            throw new BusinessException("无权修改该考勤");
        }
        
        InternshipApply apply = internshipApplyMapper.selectById(existAttendance.getApplyId());
        if (apply == null || apply.getEnterpriseId() == null
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权修改该考勤");
        }
        
        // 已确认的考勤不允许修改
        if (existAttendance.getConfirmStatus() != null && existAttendance.getConfirmStatus() == 1) {
            throw new BusinessException("已确认的考勤不允许修改");
        }
        
        // 更新
        this.updateById(attendance);
        return this.getById(attendance.getAttendanceId());
    }
    
    @Override
    public Attendance getAttendanceById(Long attendanceId) {
        if (attendanceId == null) {
            throw new BusinessException("考勤ID不能为空");
        }
        
        Attendance attendance = this.getById(attendanceId);
        if (attendance == null || attendance.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("考勤不存在");
        }
        
        // 填充关联字段
        fillAttendanceRelatedFields(attendance);
        
        return attendance;
    }
    
    /**
     * 填充考勤关联字段（学生姓名、学号）
     */
    private void fillAttendanceRelatedFields(Attendance attendance) {
        if (attendance == null || attendance.getStudentId() == null) {
            return;
        }
        
        // 查询学生信息
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getStudentId, attendance.getStudentId())
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        
        if (student != null) {
            attendance.setStudentNo(student.getStudentNo());
            
            // 查询用户信息获取学生姓名
            if (student.getUserId() != null) {
                UserInfo user = userMapper.selectOne(
                        new LambdaQueryWrapper<UserInfo>()
                                .eq(UserInfo::getUserId, student.getUserId())
                                .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (user != null) {
                    attendance.setStudentName(user.getRealName());
                }
            }
        }
    }
    
    @Override
    public Page<Attendance> getAttendancePage(Page<Attendance> page, Long studentId, Long applyId,
                                              java.time.LocalDate attendanceDate, Integer attendanceType, Integer confirmStatus) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            // 系统管理员可以查看所有考勤，不添加过滤条件
        } else {
            String username = SecurityUtil.getCurrentUsername();
            if (username != null) {
                UserInfo user = userMapper.selectOne(
                        new LambdaQueryWrapper<UserInfo>()
                                .eq(UserInfo::getUsername, username)
                                .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (user != null) {
                    // 学生只能查看自己的考勤
                    if (dataPermissionUtil.hasRole("ROLE_STUDENT")) {
                        Student student = studentMapper.selectOne(
                                new LambdaQueryWrapper<Student>()
                                        .eq(Student::getUserId, user.getUserId())
                                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        );
                        if (student != null) {
                            wrapper.eq(Attendance::getStudentId, student.getStudentId());
                        } else {
                            // 如果没有学生信息，返回空结果
                            wrapper.eq(Attendance::getAttendanceId, -1L);
                        }
                    } else {
                        // 企业管理员或企业导师只能查看本企业的考勤
                        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                        if (currentUserEnterpriseId != null) {
                            // 通过applyId关联查询，过滤企业ID
                            // 查询本企业的所有申请ID
                            java.util.List<InternshipApply> applies = internshipApplyMapper.selectList(
                                    new LambdaQueryWrapper<InternshipApply>()
                                            .eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId)
                                            .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                            .select(InternshipApply::getApplyId)
                            );
                            if (applies != null && !applies.isEmpty()) {
                                java.util.List<Long> applyIds = applies.stream()
                                        .map(InternshipApply::getApplyId)
                                        .collect(java.util.stream.Collectors.toList());
                                wrapper.in(Attendance::getApplyId, applyIds);
                            } else {
                                // 如果没有申请，返回空结果
                                wrapper.eq(Attendance::getAttendanceId, -1L);
                            }
                        }
                    }
                }
            }
        }
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(Attendance::getStudentId, studentId);
        }
        if (applyId != null) {
            wrapper.eq(Attendance::getApplyId, applyId);
        }
        if (attendanceDate != null) {
            wrapper.eq(Attendance::getAttendanceDate, attendanceDate);
        }
        if (attendanceType != null) {
            wrapper.eq(Attendance::getAttendanceType, attendanceType);
        }
        if (confirmStatus != null) {
            wrapper.eq(Attendance::getConfirmStatus, confirmStatus);
        }
        
        // 按考勤日期倒序
        wrapper.orderByDesc(Attendance::getAttendanceDate);
        
        Page<Attendance> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (Attendance attendance : result.getRecords()) {
                fillAttendanceRelatedFields(attendance);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmAttendance(Long attendanceId, Integer confirmStatus, String confirmComment, 
                                    LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        if (attendanceId == null) {
            throw new BusinessException("考勤ID不能为空");
        }
        if (confirmStatus == null || (confirmStatus != 1 && confirmStatus != 2)) {
            throw new BusinessException("确认状态无效");
        }
        
        Attendance attendance = this.getById(attendanceId);
        if (attendance == null || attendance.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("考勤不存在");
        }
        
        // 数据权限：企业管理员或企业导师只能确认本企业的考勤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null) {
            throw new BusinessException("无权确认该考勤");
        }
        
        InternshipApply apply = internshipApplyMapper.selectById(attendance.getApplyId());
        if (apply == null || apply.getEnterpriseId() == null
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权确认该考勤");
        }
        
        // 如果提供了签到时间，更新签到时间
        if (checkInTime != null) {
            attendance.setCheckInTime(checkInTime);
        }
        
        // 如果提供了签退时间，更新签退时间
        if (checkOutTime != null) {
            attendance.setCheckOutTime(checkOutTime);
        }
        
        // 如果同时提供了签到和签退时间，重新计算工作时长
        if (checkInTime != null && checkOutTime != null) {
            Duration duration = Duration.between(checkInTime, checkOutTime);
            double hours = duration.toMinutes() / 60.0;
            attendance.setWorkHours(BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP));
        } else if (attendance.getCheckInTime() != null && attendance.getCheckOutTime() != null) {
            // 如果只更新了其中一个时间，也重新计算工作时长
            LocalDateTime inTime = checkInTime != null ? checkInTime : attendance.getCheckInTime();
            LocalDateTime outTime = checkOutTime != null ? checkOutTime : attendance.getCheckOutTime();
            Duration duration = Duration.between(inTime, outTime);
            double hours = duration.toMinutes() / 60.0;
            attendance.setWorkHours(BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP));
        }
        
        // 设置确认信息
        attendance.setConfirmStatus(confirmStatus);
        attendance.setConfirmTime(LocalDateTime.now());
        attendance.setConfirmComment(confirmComment);
        
        // 设置确认人ID
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                attendance.setConfirmUserId(user.getUserId());
            }
        }
        
        return this.updateById(attendance);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttendance(Long attendanceId) {
        if (attendanceId == null) {
            throw new BusinessException("考勤ID不能为空");
        }
        
        Attendance attendance = this.getById(attendanceId);
        if (attendance == null || attendance.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("考勤不存在");
        }
        
        // 数据权限：企业管理员或企业导师只能删除本企业的考勤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null) {
            throw new BusinessException("无权删除该考勤");
        }
        
        InternshipApply apply = internshipApplyMapper.selectById(attendance.getApplyId());
        if (apply == null || apply.getEnterpriseId() == null
                || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
            throw new BusinessException("无权删除该考勤");
        }
        
        // 软删除
        attendance.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(attendance);
    }
    
    @Override
    public AttendanceStatistics getAttendanceStatistics(Long studentId, Long applyId, 
                                                         java.time.LocalDate startDate, java.time.LocalDate endDate) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤
        String username = SecurityUtil.getCurrentUsername();
        if (username != null) {
            UserInfo user = userMapper.selectOne(
                    new LambdaQueryWrapper<UserInfo>()
                            .eq(UserInfo::getUsername, username)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (user != null) {
                // 学生端：只能查看自己的统计
                if (studentId == null) {
                    // 如果没有指定studentId，自动使用当前登录学生的ID
                    // 这里需要根据实际业务逻辑获取当前学生的ID
                    // 暂时通过applyId或其他方式获取
                }
            }
        }
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(Attendance::getStudentId, studentId);
        }
        if (applyId != null) {
            wrapper.eq(Attendance::getApplyId, applyId);
        }
        if (startDate != null) {
            wrapper.ge(Attendance::getAttendanceDate, startDate);
        }
        if (endDate != null) {
            wrapper.le(Attendance::getAttendanceDate, endDate);
        }
        
        // 查询所有符合条件的考勤记录
        List<Attendance> attendanceList = this.list(wrapper);
        
        // 统计计算
        AttendanceStatistics statistics = new AttendanceStatistics();
        int totalDays = attendanceList.size();
        int normalDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int leaveDays = 0;
        int absentDays = 0;
        int restDays = 0;
        double totalWorkHours = 0.0;
        
        for (Attendance attendance : attendanceList) {
            Integer type = attendance.getAttendanceType();
            if (type != null) {
                switch (type) {
                    case 1: // 正常
                        normalDays++;
                        break;
                    case 2: // 迟到
                        lateDays++;
                        break;
                    case 3: // 早退
                        earlyLeaveDays++;
                        break;
                    case 4: // 请假
                        leaveDays++;
                        break;
                    case 5: // 缺勤
                        absentDays++;
                        break;
                    case 6: // 休息
                        restDays++;
                        break;
                }
            }
            // 累计工作时长
            if (attendance.getWorkHours() != null) {
                totalWorkHours += attendance.getWorkHours().doubleValue();
            }
        }
        
        // 计算出勤率（正常出勤天数 / (总天数 - 请假天数 - 休息天数) * 100）
        double attendanceRate = 0.0;
        int effectiveTotalDays = totalDays - leaveDays - restDays;
        if (effectiveTotalDays > 0) {
            attendanceRate = (double) normalDays / effectiveTotalDays * 100;
        }
        
        statistics.setTotalDays(totalDays);
        statistics.setNormalDays(normalDays);
        statistics.setLateDays(lateDays);
        statistics.setEarlyLeaveDays(earlyLeaveDays);
        statistics.setLeaveDays(leaveDays);
        statistics.setAbsentDays(absentDays);
        statistics.setRestDays(restDays);
        statistics.setAttendanceRate(attendanceRate);
        statistics.setTotalWorkHours(totalWorkHours);
        
        return statistics;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance studentCheckIn(LocalDate attendanceDate) {
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法签到");
        }
        
        // 获取当前用户ID
        Long userId = dataPermissionUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        
        // 获取当前学生的实习申请（已通过的）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(InternshipApply::getStudentId, studentId)
                   .eq(InternshipApply::getStatus, 1) // 已通过
                   .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                   .orderByDesc(InternshipApply::getCreateTime)
                   .last("LIMIT 1");
        InternshipApply apply = internshipApplyMapper.selectOne(applyWrapper);
        if (apply == null) {
            throw new BusinessException("您还没有已通过的实习申请，无法签到");
        }
        
        // 如果未指定日期，使用今天
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
        
        // 检查今天是否已经签到
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        
        if (existAttendance != null) {
            // 如果已存在考勤记录，更新签到时间
            if (existAttendance.getCheckInTime() != null) {
                throw new BusinessException("今天已经签到过了");
            }
            existAttendance.setCheckInTime(now);
            // 如果考勤类型是缺勤，改为出勤
            if (existAttendance.getAttendanceType() != null && existAttendance.getAttendanceType() == 5) {
                existAttendance.setAttendanceType(1); // 改为出勤
            } else if (existAttendance.getAttendanceType() == null) {
                // 判断是否迟到（假设9:00为上班时间）
                LocalTime checkInTime = now.toLocalTime();
                LocalTime workStartTime = LocalTime.of(9, 0);
                if (checkInTime.isAfter(workStartTime)) {
                    existAttendance.setAttendanceType(2); // 迟到
                } else {
                    existAttendance.setAttendanceType(1); // 正常
                }
            }
            this.updateById(existAttendance);
            return existAttendance;
        } else {
            // 创建新的考勤记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setUserId(userId);
            attendance.setApplyId(apply.getApplyId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setCheckInTime(now);
            attendance.setConfirmStatus(0); // 待确认
            attendance.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            
            // 判断是否迟到（假设9:00为上班时间）
            LocalTime checkInTime = now.toLocalTime();
            LocalTime workStartTime = LocalTime.of(9, 0);
            if (checkInTime.isAfter(workStartTime)) {
                attendance.setAttendanceType(2); // 迟到
            } else {
                attendance.setAttendanceType(1); // 正常
            }
            
            this.save(attendance);
            return attendance;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance studentCheckOut(LocalDate attendanceDate) {
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法签退");
        }
        
        // 如果未指定日期，使用今天
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
        
        // 获取当前学生的实习申请（已通过的）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(InternshipApply::getStudentId, studentId)
                   .eq(InternshipApply::getStatus, 1) // 已通过
                   .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                   .orderByDesc(InternshipApply::getCreateTime)
                   .last("LIMIT 1");
        InternshipApply apply = internshipApplyMapper.selectOne(applyWrapper);
        if (apply == null) {
            throw new BusinessException("您还没有已通过的实习申请，无法签退");
        }
        
        // 查找今天的考勤记录
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance attendance = this.getOne(wrapper);
        
        if (attendance == null) {
            throw new BusinessException("请先签到后再签退");
        }
        
        if (attendance.getCheckOutTime() != null) {
            throw new BusinessException("今天已经签退过了");
        }
        
        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);
        
        // 计算工作时长
        if (attendance.getCheckInTime() != null) {
            Duration duration = Duration.between(attendance.getCheckInTime(), now);
            double hours = duration.toMinutes() / 60.0;
            attendance.setWorkHours(BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP));
            
            // 判断是否早退（假设18:00为下班时间）
            LocalTime checkOutTime = now.toLocalTime();
            LocalTime workEndTime = LocalTime.of(18, 0);
            if (checkOutTime.isBefore(workEndTime) && attendance.getAttendanceType() == 1) {
                attendance.setAttendanceType(3); // 早退
            }
        }
        
        this.updateById(attendance);
        return attendance;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance studentApplyLeave(LocalDate attendanceDate, String leaveType, String leaveReason) {
        if (attendanceDate == null) {
            throw new BusinessException("请假日期不能为空");
        }
        if (!StringUtils.hasText(leaveType)) {
            throw new BusinessException("请假类型不能为空");
        }
        if (!StringUtils.hasText(leaveReason)) {
            throw new BusinessException("请假原因不能为空");
        }
        
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法申请请假");
        }
        
        // 获取当前用户ID
        Long userId = dataPermissionUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        
        // 获取当前学生的实习申请（已通过的）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(InternshipApply::getStudentId, studentId)
                   .eq(InternshipApply::getStatus, 1) // 已通过
                   .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                   .orderByDesc(InternshipApply::getCreateTime)
                   .last("LIMIT 1");
        InternshipApply apply = internshipApplyMapper.selectOne(applyWrapper);
        if (apply == null) {
            throw new BusinessException("您还没有已通过的实习申请，无法申请请假");
        }
        
        // 检查该日期是否已有考勤记录
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        
        if (existAttendance != null) {
            // 如果已存在考勤记录，更新为请假
            if (existAttendance.getConfirmStatus() != null && existAttendance.getConfirmStatus() == 1) {
                throw new BusinessException("该日期考勤已确认，无法修改为请假");
            }
            existAttendance.setAttendanceType(4); // 请假
            existAttendance.setLeaveType(leaveType);
            existAttendance.setLeaveReason(leaveReason);
            existAttendance.setConfirmStatus(0); // 待确认
            this.updateById(existAttendance);
            return existAttendance;
        } else {
            // 创建新的请假记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setUserId(userId);
            attendance.setApplyId(apply.getApplyId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setAttendanceType(4); // 请假
            attendance.setLeaveType(leaveType);
            attendance.setLeaveReason(leaveReason);
            attendance.setConfirmStatus(0); // 待确认
            attendance.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            
            this.save(attendance);
            return attendance;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance studentSelectRest(LocalDate attendanceDate) {
        if (attendanceDate == null) {
            throw new BusinessException("休息日期不能为空");
        }
        
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法选择休息");
        }
        
        // 获取当前用户ID
        Long userId = dataPermissionUtil.getCurrentUserId();
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息");
        }
        
        // 获取当前学生的实习申请（已通过的）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(InternshipApply::getStudentId, studentId)
                   .eq(InternshipApply::getStatus, 1) // 已通过
                   .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                   .orderByDesc(InternshipApply::getCreateTime)
                   .last("LIMIT 1");
        InternshipApply apply = internshipApplyMapper.selectOne(applyWrapper);
        if (apply == null) {
            throw new BusinessException("您还没有已通过的实习申请，无法选择休息");
        }
        
        // 检查该日期是否已有考勤记录
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        
        if (existAttendance != null) {
            // 如果已存在考勤记录，更新为休息
            if (existAttendance.getConfirmStatus() != null && existAttendance.getConfirmStatus() == 1) {
                throw new BusinessException("该日期考勤已确认，无法修改为休息");
            }
            existAttendance.setAttendanceType(6); // 休息
            existAttendance.setConfirmStatus(0); // 待确认
            this.updateById(existAttendance);
            return existAttendance;
        } else {
            // 创建新的休息记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setUserId(userId);
            attendance.setApplyId(apply.getApplyId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setAttendanceType(6); // 休息
            attendance.setConfirmStatus(0); // 待确认
            attendance.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            
            this.save(attendance);
            return attendance;
        }
    }
    
    @Override
    public Attendance getTodayAttendance() {
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            // 如果不是学生，返回null（不抛异常，允许其他角色调用但返回空）
            return null;
        }
        
        // 获取当前学生的实习申请（已通过的）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.eq(InternshipApply::getStudentId, studentId)
                   .eq(InternshipApply::getStatus, 1) // 已通过
                   .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                   .orderByDesc(InternshipApply::getCreateTime)
                   .last("LIMIT 1");
        InternshipApply apply = internshipApplyMapper.selectOne(applyWrapper);
        if (apply == null) {
            // 没有已通过的申请，返回null
            return null;
        }
        
        // 查找今天的考勤记录
        LocalDate today = LocalDate.now();
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, today)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        Attendance attendance = this.getOne(wrapper);
        
        // 如果找到考勤记录，填充关联字段
        if (attendance != null) {
            fillAttendanceRelatedFields(attendance);
        }
        
        return attendance;
    }
}

