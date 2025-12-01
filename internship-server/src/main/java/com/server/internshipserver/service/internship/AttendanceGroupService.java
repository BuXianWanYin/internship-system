package com.server.internshipserver.service.internship;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.internship.AttendanceGroup;
import com.server.internshipserver.domain.internship.AttendanceGroupRule;
import com.server.internshipserver.domain.internship.AttendanceGroupStudent;
import com.server.internshipserver.domain.internship.AttendanceGroupTimeSlot;

import java.time.LocalDate;
import java.util.List;

/**
 * 考勤组管理Service接口
 */
public interface AttendanceGroupService extends IService<AttendanceGroup> {
    
    /**
     * 创建考勤组（包含时间段）
     * @param group 考勤组信息
     * @param timeSlots 时间段列表
     * @return 创建的考勤组信息
     */
    AttendanceGroup createAttendanceGroup(AttendanceGroup group, List<AttendanceGroupTimeSlot> timeSlots);
    
    /**
     * 更新考勤组（包含时间段）
     * @param group 考勤组信息
     * @param timeSlots 时间段列表
     * @return 更新后的考勤组信息
     */
    AttendanceGroup updateAttendanceGroup(AttendanceGroup group, List<AttendanceGroupTimeSlot> timeSlots);
    
    /**
     * 删除考勤组
     * @param groupId 考勤组ID
     * @return 是否成功
     */
    boolean deleteAttendanceGroup(Long groupId);
    
    /**
     * 分页查询考勤组列表
     * @param page 分页参数
     * @param groupName 考勤组名称
     * @return 考勤组列表
     */
    Page<AttendanceGroup> getAttendanceGroupPage(Page<AttendanceGroup> page, String groupName);
    
    /**
     * 根据ID查询考勤组详情（包含时间段和规则）
     * @param groupId 考勤组ID
     * @return 考勤组详情
     */
    AttendanceGroup getAttendanceGroupDetail(Long groupId);
    
    /**
     * 添加考勤组规则
     * @param rule 规则信息
     * @return 添加的规则信息
     */
    AttendanceGroupRule addRule(AttendanceGroupRule rule);
    
    /**
     * 删除考勤组规则
     * @param ruleId 规则ID
     * @return 是否成功
     */
    boolean deleteRule(Long ruleId);
    
    /**
     * 查询考勤组规则列表
     * @param groupId 考勤组ID
     * @return 规则列表
     */
    List<AttendanceGroupRule> getRuleList(Long groupId);
    
    /**
     * 分配学生到考勤组
     * @param groupId 考勤组ID
     * @param applyId 实习申请ID
     * @param effectiveStartDate 生效开始日期
     * @param effectiveEndDate 生效结束日期
     * @return 是否成功
     */
    boolean assignStudentToGroup(Long groupId, Long applyId, LocalDate effectiveStartDate, LocalDate effectiveEndDate);
    
    /**
     * 批量分配学生到考勤组
     * @param groupId 考勤组ID
     * @param applyIds 实习申请ID列表
     * @param effectiveStartDate 生效开始日期
     * @param effectiveEndDate 生效结束日期
     * @return 是否成功
     */
    boolean batchAssignStudentsToGroup(Long groupId, List<Long> applyIds, LocalDate effectiveStartDate, LocalDate effectiveEndDate);
    
    /**
     * 解除学生考勤组关联
     * @param applyId 实习申请ID
     * @return 是否成功
     */
    boolean unassignStudentFromGroup(Long applyId);
    
    /**
     * 查询考勤组的学生列表
     * @param groupId 考勤组ID
     * @return 学生列表
     */
    List<AttendanceGroupStudent> getGroupStudents(Long groupId);
    
    /**
     * 根据实习申请ID查询学生所属的考勤组
     * @param applyId 实习申请ID
     * @return 考勤组信息（包含时间段）
     */
    AttendanceGroup getGroupByApplyId(Long applyId);
    
    /**
     * 根据考勤组规则计算应出勤日期列表
     * @param groupId 考勤组ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 应出勤日期列表
     */
    List<LocalDate> calculateExpectedDates(Long groupId, LocalDate startDate, LocalDate endDate);
    
    /**
     * 计算缺勤天数
     * @param applyId 实习申请ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 缺勤天数
     */
    int calculateAbsentDays(Long applyId, LocalDate startDate, LocalDate endDate);
}

