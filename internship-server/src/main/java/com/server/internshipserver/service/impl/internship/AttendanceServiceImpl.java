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
import com.server.internshipserver.mapper.internship.AttendanceMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.internship.AttendanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.math.BigDecimal;

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
        
        return attendance;
    }
    
    @Override
    public Page<Attendance> getAttendancePage(Page<Attendance> page, Long studentId, Long applyId,
                                              java.time.LocalDate attendanceDate, Integer attendanceType, Integer confirmStatus) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
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
                // 企业管理员或企业导师只能查看本企业的考勤
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId != null) {
                    // 通过applyId关联查询，过滤企业ID
                    // TODO: 实现更精确的数据权限过滤
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
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmAttendance(Long attendanceId, Integer confirmStatus, String confirmComment) {
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
                }
            }
            // 累计工作时长
            if (attendance.getWorkHours() != null) {
                totalWorkHours += attendance.getWorkHours().doubleValue();
            }
        }
        
        // 计算出勤率（正常出勤天数 / 总天数 * 100）
        double attendanceRate = 0.0;
        if (totalDays > 0) {
            attendanceRate = (double) normalDays / totalDays * 100;
        }
        
        statistics.setTotalDays(totalDays);
        statistics.setNormalDays(normalDays);
        statistics.setLateDays(lateDays);
        statistics.setEarlyLeaveDays(earlyLeaveDays);
        statistics.setLeaveDays(leaveDays);
        statistics.setAbsentDays(absentDays);
        statistics.setAttendanceRate(attendanceRate);
        statistics.setTotalWorkHours(totalWorkHours);
        
        return statistics;
    }
}

