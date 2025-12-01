package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.Attendance;
import com.server.internshipserver.domain.internship.dto.AttendanceStatistics;

import java.util.List;

/**
 * 考勤管理Service接口
 */
public interface AttendanceService extends IService<Attendance> {
    
    /**
     * 确认考勤
     * @param attendance 考勤信息
     * @return 添加的考勤信息
     */
    Attendance addAttendance(Attendance attendance);
    
    /**
     * 批量确认考勤
     * @param attendanceList 考勤列表
     * @return 是否成功
     */
    boolean batchAddAttendance(List<Attendance> attendanceList);
    
    /**
     * 更新考勤信息
     * @param attendance 考勤信息
     * @return 更新后的考勤信息
     */
    Attendance updateAttendance(Attendance attendance);
    
    /**
     * 根据ID查询考勤详情
     * @param attendanceId 考勤ID
     * @return 考勤信息
     */
    Attendance getAttendanceById(Long attendanceId);
    
    /**
     * 分页查询考勤列表
     * @param page 分页参数
     * @param studentId 学生ID
     * @param applyId 申请ID
     * @param attendanceDate 考勤日期
     * @param attendanceType 考勤类型
     * @param confirmStatus 确认状态
     * @return 考勤列表
     */
    Page<Attendance> getAttendancePage(Page<Attendance> page, Long studentId, Long applyId,
                                     java.time.LocalDate attendanceDate, Integer attendanceType, Integer confirmStatus);
    
    /**
     * 确认考勤
     * @param attendanceId 考勤ID
     * @param confirmStatus 确认状态（1-已确认，2-已拒绝）
     * @param confirmComment 确认意见
     * @param checkInTime 签到时间（可选，用于编辑）
     * @param checkOutTime 签退时间（可选，用于编辑）
     * @return 是否成功
     */
    boolean confirmAttendance(Long attendanceId, Integer confirmStatus, String confirmComment, 
                             java.time.LocalDateTime checkInTime, java.time.LocalDateTime checkOutTime);
    
    /**
     * 删除考勤（软删除）
     * @param attendanceId 考勤ID
     * @return 是否成功
     */
    boolean deleteAttendance(Long attendanceId);
    
    /**
     * 查询考勤统计信息
     * @param studentId 学生ID（可选，学生端必填）
     * @param applyId 申请ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 考勤统计信息
     */
    AttendanceStatistics getAttendanceStatistics(
            Long studentId, Long applyId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    /**
     * 学生签到
     * @param attendanceDate 考勤日期（可选，默认为今天）
     * @param timeSlotId 时间段ID（可选，如果考勤组只有一个时间段则自动使用）
     * @return 考勤信息
     */
    Attendance studentCheckIn(java.time.LocalDate attendanceDate, Long timeSlotId);
    
    /**
     * 学生签退
     * @param attendanceDate 考勤日期（可选，默认为今天）
     * @param timeSlotId 时间段ID（可选，如果考勤组只有一个时间段则自动使用）
     * @return 考勤信息
     */
    Attendance studentCheckOut(java.time.LocalDate attendanceDate, Long timeSlotId);
    
    /**
     * 学生申请请假
     * @param attendanceDate 考勤日期
     * @param leaveType 请假类型（事假、病假、调休等）
     * @param leaveReason 请假原因
     * @return 考勤信息
     */
    Attendance studentApplyLeave(java.time.LocalDate attendanceDate, String leaveType, String leaveReason);
    
    /**
     * 学生选择休息
     * @param attendanceDate 考勤日期
     * @return 考勤信息
     */
    Attendance studentSelectRest(java.time.LocalDate attendanceDate);
    
    /**
     * 获取今天的考勤记录（学生端）
     * @return 考勤信息，如果不存在则返回null
     */
    Attendance getTodayAttendance();
}

