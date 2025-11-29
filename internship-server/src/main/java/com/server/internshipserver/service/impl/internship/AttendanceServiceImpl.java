package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.AttendanceType;
import com.server.internshipserver.common.enums.ConfirmStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.StudentConfirmStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.Attendance;
import com.server.internshipserver.domain.internship.AttendanceGroup;
import com.server.internshipserver.domain.internship.AttendanceGroupTimeSlot;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.mapper.internship.AttendanceGroupTimeSlotMapper;
import com.server.internshipserver.domain.internship.dto.AttendanceStatistics;
import com.server.internshipserver.service.internship.AttendanceGroupService;
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
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.stream.Collectors;

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
    
    @Autowired
    private AttendanceGroupService attendanceGroupService;
    
    @Autowired
    private AttendanceGroupTimeSlotMapper timeSlotMapper;
    
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
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：根据申请类型判断权限
        // 系统管理员和学校管理员可以添加所有考勤
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isAdmin = false;
        if (currentUser != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            isAdmin = dataPermissionUtil.isSystemAdmin() || 
                     DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN);
        }
        if (!isAdmin) {
            if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
                // 合作企业实习：企业管理员或企业导师只能为本企业的实习生确认考勤
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                        || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    throw new BusinessException("无权为该申请确认考勤");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以为所管理班级的学生确认考勤
                UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
                if (user == null) {
                    throw new BusinessException("无权为该申请确认考勤");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权为该申请确认考勤");
                }
                // 验证学生是否属于班主任管理的班级
                Student student = studentMapper.selectById(attendance.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权为该学生确认考勤");
                }
            }
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
        if (attendance.getAttendanceType() != null && attendance.getAttendanceType().equals(AttendanceType.LEAVE.getCode()) && !StringUtils.hasText(attendance.getLeaveReason())) {
            throw new BusinessException("请假类型必须填写请假原因");
        }
        
        // 设置考勤信息
        attendance.setUserId(apply.getUserId());
        attendance.setConfirmStatus(ConfirmStatus.PENDING.getCode()); // 待确认
        EntityDefaultValueUtil.setDefaultValues(attendance);
        
        // 设置确认人ID
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null) {
            attendance.setConfirmUserId(user.getUserId());
            attendance.setConfirmStatus(ConfirmStatus.CONFIRMED.getCode());
            attendance.setConfirmTime(LocalDateTime.now());
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
        EntityValidationUtil.validateEntityExists(existAttendance, "考勤");
        
        InternshipApply apply = internshipApplyMapper.selectById(existAttendance.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：根据申请类型判断权限
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isAdmin = false;
        if (currentUser != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            isAdmin = dataPermissionUtil.isSystemAdmin() || 
                     DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN);
        }
        if (!isAdmin) {
            if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
                // 合作企业实习：企业管理员或企业导师只能修改本企业的考勤
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                        || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    throw new BusinessException("无权修改该考勤");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以修改所管理班级学生的考勤
                if (currentUser == null) {
                    throw new BusinessException("无权修改该考勤");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权修改该考勤");
                }
                Student student = studentMapper.selectById(existAttendance.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权修改该考勤");
                }
            } else {
                throw new BusinessException("申请类型无效");
            }
        }
        
        // 已确认的考勤不允许修改
        if (existAttendance.getConfirmStatus() != null && existAttendance.getConfirmStatus().equals(ConfirmStatus.CONFIRMED.getCode())) {
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
        EntityValidationUtil.validateEntityExists(attendance, "考勤");
        
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
        
        // 查询申请信息获取申请类型
        if (attendance.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(attendance.getApplyId());
            if (apply != null) {
                attendance.setApplyType(apply.getApplyType());
            }
        }
    }
    
    @Override
    public Page<Attendance> getAttendancePage(Page<Attendance> page, Long studentId, Long applyId,
                                              java.time.LocalDate attendanceDate, Integer attendanceType, Integer confirmStatus) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Attendance::getDeleteFlag);
        
        // 数据权限过滤
        // 系统管理员不添加限制
        if (dataPermissionUtil.isSystemAdmin()) {
            // 系统管理员可以查看所有考勤，不添加过滤条件
        } else {
            UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
            if (user != null) {
                    // 学生只能查看自己的考勤
                    if (dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
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
                            // 方式1：通过applyId关联查询，过滤企业ID（查询本企业的所有申请ID）
                            List<InternshipApply> applies = internshipApplyMapper.selectList(
                                    new LambdaQueryWrapper<InternshipApply>()
                                            .eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId)
                                            .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                            .select(InternshipApply::getApplyId)
                            );
                            
                            // 方式2：通过student.current_enterprise_id查询（查询当前实习企业为本企业的学生）
                            List<Student> students = studentMapper.selectList(
                                    new LambdaQueryWrapper<Student>()
                                            .eq(Student::getCurrentEnterpriseId, currentUserEnterpriseId)
                                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                            .select(Student::getStudentId)
                            );
                            
                            // 合并两种方式的查询结果
                            List<Long> applyIds = new ArrayList<>();
                            
                            // 添加方式1的申请ID
                            if (applies != null && !applies.isEmpty()) {
                                applyIds.addAll(applies.stream()
                                        .map(InternshipApply::getApplyId)
                                        .collect(Collectors.toList()));
                            }
                            
                            // 添加方式2的申请ID（通过学生ID查询其当前申请）
                            if (students != null && !students.isEmpty()) {
                                List<Long> studentIds = students.stream()
                                        .map(Student::getStudentId)
                                        .collect(Collectors.toList());
                                List<InternshipApply> studentApplies = internshipApplyMapper.selectList(
                                        new LambdaQueryWrapper<InternshipApply>()
                                                .in(InternshipApply::getStudentId, studentIds)
                                                .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                                .select(InternshipApply::getApplyId)
                                );
                                if (studentApplies != null && !studentApplies.isEmpty()) {
                                    applyIds.addAll(studentApplies.stream()
                                            .map(InternshipApply::getApplyId)
                                            .collect(java.util.stream.Collectors.toList()));
                                }
                            }
                            
                            // 去重
                            applyIds = applyIds.stream().distinct().collect(java.util.stream.Collectors.toList());
                            
                            if (!applyIds.isEmpty()) {
                                wrapper.in(Attendance::getApplyId, applyIds);
                            } else {
                                // 如果没有申请，返回空结果
                                wrapper.eq(Attendance::getAttendanceId, -1L);
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
        if (EntityValidationUtil.hasRecords(result)) {
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
        if (confirmStatus == null || (!confirmStatus.equals(ConfirmStatus.CONFIRMED.getCode()) && !confirmStatus.equals(ConfirmStatus.REJECTED.getCode()))) {
            throw new BusinessException("确认状态无效");
        }
        
        Attendance attendance = this.getById(attendanceId);
        EntityValidationUtil.validateEntityExists(attendance, "考勤");
        
        InternshipApply apply = internshipApplyMapper.selectById(attendance.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：根据申请类型判断权限
        // 系统管理员和学校管理员可以确认所有考勤
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isAdmin = false;
        if (currentUser != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            isAdmin = dataPermissionUtil.isSystemAdmin() || 
                     DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN);
        }
        if (!isAdmin) {
            if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
                // 合作企业实习：企业管理员或企业导师只能确认本企业的考勤
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                        || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    throw new BusinessException("无权确认该考勤");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以确认所管理班级学生的考勤
                UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
                if (user == null) {
                    throw new BusinessException("无权确认该考勤");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权确认该考勤");
                }
                // 验证学生是否属于班主任管理的班级
                Student student = studentMapper.selectById(attendance.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权确认该考勤");
                }
            } else {
                throw new BusinessException("申请类型无效");
            }
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
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null) {
                attendance.setConfirmUserId(user.getUserId());
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
        EntityValidationUtil.validateEntityExists(attendance, "考勤");
        
        InternshipApply apply = internshipApplyMapper.selectById(attendance.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 数据权限：根据申请类型判断权限
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isAdmin = false;
        if (currentUser != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            isAdmin = dataPermissionUtil.isSystemAdmin() || 
                     DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_SCHOOL_ADMIN);
        }
        if (!isAdmin) {
            if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
                // 合作企业实习：企业管理员或企业导师只能删除本企业的考勤
                Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (currentUserEnterpriseId == null || apply.getEnterpriseId() == null
                        || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                    throw new BusinessException("无权删除该考勤");
                }
            } else if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                // 自主实习：班主任可以删除所管理班级学生的考勤
                if (currentUser == null) {
                    throw new BusinessException("无权删除该考勤");
                }
                List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                if (!DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                    throw new BusinessException("无权删除该考勤");
                }
                Student student = studentMapper.selectById(attendance.getStudentId());
                if (student == null || student.getClassId() == null) {
                    throw new BusinessException("学生信息不完整");
                }
                List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
                if (classIds == null || !classIds.contains(student.getClassId())) {
                    throw new BusinessException("无权删除该考勤");
                }
            } else {
                throw new BusinessException("申请类型无效");
            }
        }
        
        // 软删除
        attendance.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(attendance);
    }
    
    @Override
    public AttendanceStatistics getAttendanceStatistics(Long studentId, Long applyId, 
                                                         java.time.LocalDate startDate, java.time.LocalDate endDate) {
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        QueryWrapperUtil.notDeleted(wrapper, Attendance::getDeleteFlag);
        
        // 数据权限过滤
        String username = UserUtil.getCurrentUsername();
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
        int normalDays = 0;
        int lateDays = 0;
        int earlyLeaveDays = 0;
        int leaveDays = 0;
        int absentDays = 0;
        int restDays = 0;
        double totalWorkHours = 0.0;
        
        // 获取实习申请信息，用于计算应出勤日期和缺勤天数
        InternshipApply apply = null;
        if (applyId != null) {
            apply = internshipApplyMapper.selectById(applyId);
        } else if (studentId != null && attendanceList.size() > 0) {
            // 如果只提供了studentId，使用第一条记录的applyId
            Long firstApplyId = attendanceList.get(0).getApplyId();
            if (firstApplyId != null) {
                apply = internshipApplyMapper.selectById(firstApplyId);
            }
        }
        
        // 计算应出勤日期和缺勤天数（如果提供了applyId和日期范围）
        int expectedDays = 0;
        if (apply != null && startDate != null && endDate != null) {
            // 获取学生所属的考勤组
            AttendanceGroup group = attendanceGroupService.getGroupByApplyId(apply.getApplyId());
            if (group != null) {
                // 计算应出勤日期
                List<LocalDate> expectedDates = attendanceGroupService.calculateExpectedDates(
                        group.getGroupId(), startDate, endDate);
                expectedDays = expectedDates.size();
                
                // 使用考勤组规则计算缺勤天数
                absentDays = attendanceGroupService.calculateAbsentDays(
                        apply.getApplyId(), startDate, endDate);
            }
        }
        
        // 统计实际考勤记录
        for (Attendance attendance : attendanceList) {
            Integer type = attendance.getAttendanceType();
            if (type != null) {
                if (type.equals(AttendanceType.ATTENDANCE.getCode())) {
                    normalDays++;
                } else if (type.equals(AttendanceType.LATE.getCode())) {
                    lateDays++;
                } else if (type.equals(AttendanceType.EARLY_LEAVE.getCode())) {
                    earlyLeaveDays++;
                } else if (type.equals(AttendanceType.LEAVE.getCode())) {
                    leaveDays++;
                } else if (type.equals(AttendanceType.ABSENT.getCode())) {
                    // 如果使用考勤组规则计算缺勤，这里不再累加
                    if (expectedDays == 0) {
                        absentDays++;
                    }
                } else if (type.equals(AttendanceType.REST.getCode())) {
                    restDays++;
                }
            }
            // 累计工作时长
            if (attendance.getWorkHours() != null) {
                totalWorkHours += attendance.getWorkHours().doubleValue();
            }
        }
        
        // 总出勤天数 = 实际考勤记录数
        int totalDays = attendanceList.size();
        
        // 计算出勤率（正常出勤天数 / (应出勤天数 - 请假天数 - 休息天数) * 100）
        double attendanceRate = 0.0;
        int effectiveTotalDays = expectedDays > 0 ? expectedDays : totalDays;
        effectiveTotalDays = effectiveTotalDays - leaveDays - restDays;
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
    public Attendance studentCheckIn(LocalDate attendanceDate, Long timeSlotId) {
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
        
        // 获取学生信息，查询当前实习申请
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        if (student.getCurrentApplyId() == null) {
            throw new BusinessException("您还没有确认上岗的实习申请，无法签到");
        }
        
        // 查询当前实习申请
        InternshipApply apply = internshipApplyMapper.selectById(student.getCurrentApplyId());
        EntityValidationUtil.validateEntityExists(apply, "您的实习申请");
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException("实习申请与学生不匹配");
        }
        
        // 验证学生确认状态为已确认上岗
        if (apply.getStudentConfirmStatus() == null || !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            throw new BusinessException("您还没有确认上岗，无法签到");
        }
        
        // 如果未指定日期，使用今天
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
        
        // 获取学生所属的考勤组（仅合作企业实习需要）
        AttendanceGroup group = null;
        AttendanceGroupTimeSlot selectedTimeSlot = null;
        Long selectedTimeSlotId = timeSlotId;
        
        // 只有合作企业实习才需要考勤组
        if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.COOPERATION.getCode())) {
            group = attendanceGroupService.getGroupByApplyId(apply.getApplyId());
            if (group != null) {
                // 获取考勤组的时间段列表
                List<AttendanceGroupTimeSlot> timeSlots = group.getTimeSlots();
                if (timeSlots == null || timeSlots.isEmpty()) {
                    throw new BusinessException("考勤组未配置时间段，无法打卡");
                }
                
                // 如果提供了timeSlotId，验证它是否属于该考勤组
                if (selectedTimeSlotId != null) {
                    final Long finalTimeSlotId = selectedTimeSlotId;
                    selectedTimeSlot = timeSlots.stream()
                            .filter(slot -> slot.getSlotId().equals(finalTimeSlotId))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException("时间段不存在或不属于该考勤组"));
                } else {
                    // 如果没有提供timeSlotId，检查是否有唯一的时间段
                    if (timeSlots.size() == 1) {
                        selectedTimeSlot = timeSlots.get(0);
                        selectedTimeSlotId = selectedTimeSlot.getSlotId();
                    } else {
                        throw new BusinessException("考勤组有多个时间段，请选择要使用的时间段");
                    }
                }
            }
        }
        // 自主实习不需要考勤组，可以直接打卡
        
        // 检查今天是否已经签到
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        
        LocalDateTime now = LocalDateTime.now();
        LocalTime checkInTime = now.toLocalTime();
        
        if (existAttendance != null) {
            // 如果已存在考勤记录，更新签到时间
            if (existAttendance.getCheckInTime() != null) {
                throw new BusinessException("今天已经签到过了");
            }
            existAttendance.setCheckInTime(now);
            existAttendance.setGroupId(group != null ? group.getGroupId() : null);
            existAttendance.setTimeSlotId(selectedTimeSlotId);
            
            // 如果考勤类型是缺勤，改为出勤
            if (existAttendance.getAttendanceType() != null && existAttendance.getAttendanceType().equals(AttendanceType.ABSENT.getCode())) {
                existAttendance.setAttendanceType(AttendanceType.ATTENDANCE.getCode());
            } else if (existAttendance.getAttendanceType() == null) {
                // 判断是否迟到（使用时间段的startTime）
                LocalTime workStartTime = selectedTimeSlot != null ? selectedTimeSlot.getStartTime() : LocalTime.of(9, 0);
                if (checkInTime.isAfter(workStartTime)) {
                    existAttendance.setAttendanceType(AttendanceType.LATE.getCode());
                } else {
                    existAttendance.setAttendanceType(AttendanceType.ATTENDANCE.getCode());
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
            attendance.setGroupId(group != null ? group.getGroupId() : null);
            attendance.setTimeSlotId(selectedTimeSlotId);
            attendance.setConfirmStatus(ConfirmStatus.PENDING.getCode());
            EntityDefaultValueUtil.setDefaultValues(attendance);
            
            // 判断是否迟到（使用时间段的startTime）
            LocalTime workStartTime = selectedTimeSlot != null ? selectedTimeSlot.getStartTime() : LocalTime.of(9, 0);
            if (checkInTime.isAfter(workStartTime)) {
                attendance.setAttendanceType(AttendanceType.LATE.getCode());
            } else {
                attendance.setAttendanceType(AttendanceType.ATTENDANCE.getCode());
            }
            
            this.save(attendance);
            return attendance;
        }
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Attendance studentCheckOut(LocalDate attendanceDate, Long timeSlotId) {
        // 获取当前学生ID
        Long studentId = dataPermissionUtil.getCurrentStudentId();
        if (studentId == null) {
            throw new BusinessException("当前用户不是学生，无法签退");
        }
        
        // 如果未指定日期，使用今天
        if (attendanceDate == null) {
            attendanceDate = LocalDate.now();
        }
        
        // 获取学生信息，查询当前实习申请
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        if (student.getCurrentApplyId() == null) {
            throw new BusinessException("您还没有确认上岗的实习申请，无法签退");
        }
        
        // 查询当前实习申请
        InternshipApply apply = internshipApplyMapper.selectById(student.getCurrentApplyId());
        EntityValidationUtil.validateEntityExists(apply, "您的实习申请");
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException("实习申请与学生不匹配");
        }
        
        // 验证学生确认状态为已确认上岗
        if (apply.getStudentConfirmStatus() == null || !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            throw new BusinessException("您还没有确认上岗，无法签退");
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
        
        // 获取考勤组和时间段信息
        AttendanceGroup group = null;
        AttendanceGroupTimeSlot timeSlot = null;
        if (attendance.getGroupId() != null) {
            group = attendanceGroupService.getById(attendance.getGroupId());
            if (group != null && attendance.getTimeSlotId() != null) {
                timeSlot = timeSlotMapper.selectById(attendance.getTimeSlotId());
            }
        }
        
        LocalDateTime now = LocalDateTime.now();
        attendance.setCheckOutTime(now);
        
        // 如果签到时没有设置时间段，现在设置
        if (attendance.getTimeSlotId() == null && timeSlotId != null) {
            attendance.setTimeSlotId(timeSlotId);
            if (timeSlot == null) {
                timeSlot = timeSlotMapper.selectById(timeSlotId);
            }
        }
        
        // 计算工作时长
        if (attendance.getCheckInTime() != null) {
            Duration duration = Duration.between(attendance.getCheckInTime(), now);
            double hours = duration.toMinutes() / 60.0;
            attendance.setWorkHours(BigDecimal.valueOf(hours).setScale(2, BigDecimal.ROUND_HALF_UP));
            
            // 判断是否早退（使用时间段的endTime）
            LocalTime checkOutTime = now.toLocalTime();
            LocalTime workEndTime = timeSlot != null ? timeSlot.getEndTime() : LocalTime.of(18, 0);
            if (checkOutTime.isBefore(workEndTime) && attendance.getAttendanceType() != null && attendance.getAttendanceType().equals(AttendanceType.ATTENDANCE.getCode())) {
                attendance.setAttendanceType(AttendanceType.EARLY_LEAVE.getCode());
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
        
        // 获取学生信息，查询当前实习申请
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        if (student.getCurrentApplyId() == null) {
            throw new BusinessException("您还没有确认上岗的实习申请，无法申请请假");
        }
        
        // 查询当前实习申请
        InternshipApply apply = internshipApplyMapper.selectById(student.getCurrentApplyId());
        EntityValidationUtil.validateEntityExists(apply, "您的实习申请");
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException("实习申请与学生不匹配");
        }
        
        // 验证学生确认状态为已确认上岗
        if (apply.getStudentConfirmStatus() == null || !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            throw new BusinessException("您还没有确认上岗，无法申请请假");
        }
        
        // 检查该日期是否已有考勤记录
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        
        if (existAttendance != null) {
            // 如果已存在考勤记录，更新为请假
            if (existAttendance.getConfirmStatus() != null && existAttendance.getConfirmStatus().equals(ConfirmStatus.CONFIRMED.getCode())) {
                throw new BusinessException("该日期考勤已确认，无法修改为请假");
            }
            existAttendance.setAttendanceType(AttendanceType.LEAVE.getCode());
            existAttendance.setLeaveType(leaveType);
            existAttendance.setLeaveReason(leaveReason);
            existAttendance.setConfirmStatus(ConfirmStatus.PENDING.getCode());
            this.updateById(existAttendance);
            return existAttendance;
        } else {
            // 创建新的请假记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setUserId(userId);
            attendance.setApplyId(apply.getApplyId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setAttendanceType(AttendanceType.LEAVE.getCode());
            attendance.setLeaveType(leaveType);
            attendance.setLeaveReason(leaveReason);
            attendance.setConfirmStatus(ConfirmStatus.PENDING.getCode());
            EntityDefaultValueUtil.setDefaultValues(attendance);
            
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
        
        // 获取学生信息，查询当前实习申请
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        if (student.getCurrentApplyId() == null) {
            throw new BusinessException("您还没有确认上岗的实习申请，无法选择休息");
        }
        
        // 查询当前实习申请
        InternshipApply apply = internshipApplyMapper.selectById(student.getCurrentApplyId());
        EntityValidationUtil.validateEntityExists(apply, "您的实习申请");
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            throw new BusinessException("实习申请与学生不匹配");
        }
        
        // 验证学生确认状态为已确认上岗
        if (apply.getStudentConfirmStatus() == null || !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            throw new BusinessException("您还没有确认上岗，无法选择休息");
        }
        
        // 检查该日期是否已有考勤记录
        LambdaQueryWrapper<Attendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Attendance::getApplyId, apply.getApplyId())
               .eq(Attendance::getAttendanceDate, attendanceDate)
               .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Attendance existAttendance = this.getOne(wrapper);
        
        if (existAttendance != null) {
            // 如果已存在考勤记录，更新为休息
            if (existAttendance.getConfirmStatus() != null && existAttendance.getConfirmStatus().equals(ConfirmStatus.CONFIRMED.getCode())) {
                throw new BusinessException("该日期考勤已确认，无法修改为休息");
            }
            existAttendance.setAttendanceType(AttendanceType.REST.getCode());
            existAttendance.setConfirmStatus(ConfirmStatus.PENDING.getCode());
            this.updateById(existAttendance);
            return existAttendance;
        } else {
            // 创建新的休息记录
            Attendance attendance = new Attendance();
            attendance.setStudentId(studentId);
            attendance.setUserId(userId);
            attendance.setApplyId(apply.getApplyId());
            attendance.setAttendanceDate(attendanceDate);
            attendance.setAttendanceType(AttendanceType.REST.getCode());
            attendance.setConfirmStatus(ConfirmStatus.PENDING.getCode());
            EntityDefaultValueUtil.setDefaultValues(attendance);
            
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
        
        // 获取学生信息，查询当前实习申请
        Student student = studentMapper.selectById(studentId);
        if (student == null || student.getCurrentApplyId() == null) {
            // 没有当前实习申请，返回null
            return null;
        }
        
        // 查询当前实习申请
        InternshipApply apply = internshipApplyMapper.selectById(student.getCurrentApplyId());
        if (apply == null || apply.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            // 申请不存在或已删除，返回null
            return null;
        }
        
        // 验证申请属于当前学生
        if (!apply.getStudentId().equals(studentId)) {
            return null;
        }
        
        // 验证学生确认状态为已确认上岗
        if (apply.getStudentConfirmStatus() == null || !apply.getStudentConfirmStatus().equals(StudentConfirmStatus.CONFIRMED.getCode())) {
            // 未确认上岗，返回null
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

