package com.server.internshipserver.service.impl.internship;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.internship.Attendance;
import com.server.internshipserver.domain.internship.AttendanceGroup;
import com.server.internshipserver.domain.internship.AttendanceGroupRule;
import com.server.internshipserver.domain.internship.AttendanceGroupStudent;
import com.server.internshipserver.domain.internship.AttendanceGroupTimeSlot;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.internship.AttendanceGroupMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.internship.AttendanceGroupRuleMapper;
import com.server.internshipserver.mapper.internship.AttendanceGroupStudentMapper;
import com.server.internshipserver.mapper.internship.AttendanceGroupTimeSlotMapper;
import com.server.internshipserver.mapper.internship.AttendanceMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.internship.AttendanceGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 考勤组管理Service实现类
 */
@Service
public class AttendanceGroupServiceImpl extends ServiceImpl<AttendanceGroupMapper, AttendanceGroup> implements AttendanceGroupService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private AttendanceGroupTimeSlotMapper timeSlotMapper;
    
    @Autowired
    private AttendanceGroupRuleMapper ruleMapper;
    
    @Autowired
    private AttendanceGroupStudentMapper groupStudentMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private AttendanceMapper attendanceMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceGroup createAttendanceGroup(AttendanceGroup group, List<AttendanceGroupTimeSlot> timeSlots) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(group.getGroupName(), "考勤组名称");
        if (group.getWorkDaysType() == null) {
            throw new BusinessException("工作日类型不能为空");
        }
        
        if (timeSlots == null || timeSlots.isEmpty()) {
            throw new BusinessException("至少需要配置一个时间段");
        }
        
        // 数据权限检查：只有企业管理员可以创建考勤组
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
            throw new BusinessException("只有企业管理员可以创建考勤组");
        }
        
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null) {
                throw new BusinessException("无法获取当前用户的企业ID，请确保您已关联企业");
            }
            if (group.getEnterpriseId() != null) {
                if (!currentUserEnterpriseId.equals(group.getEnterpriseId())) {
                    throw new BusinessException("无权为该企业创建考勤组");
                }
            } else {
                group.setEnterpriseId(currentUserEnterpriseId);
            }
        } else {
            // 系统管理员必须传递企业ID
            EntityValidationUtil.validateIdNotNull(group.getEnterpriseId(), "企业ID");
        }
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(group);
        group.setMentorId(currentUser.getUserId());
        group.setCreateUserId(currentUser.getUserId());
        
        // 验证工作日配置
        validateWorkDaysConfig(group);
        
        // 保存考勤组
        this.save(group);
        
        // 保存时间段
        saveTimeSlots(group.getGroupId(), timeSlots);
        
        return group;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceGroup updateAttendanceGroup(AttendanceGroup group, List<AttendanceGroupTimeSlot> timeSlots) {
        EntityValidationUtil.validateIdNotNull(group.getGroupId(), "考勤组ID");
        EntityValidationUtil.validateStringNotBlank(group.getGroupName(), "考勤组名称");
        
        if (timeSlots == null || timeSlots.isEmpty()) {
            throw new BusinessException("至少需要配置一个时间段");
        }
        
        // 检查考勤组是否存在
        AttendanceGroup existGroup = this.getById(group.getGroupId());
        EntityValidationUtil.validateEntityExists(existGroup, "考勤组");
        
        // 数据权限检查：只有企业管理员可以编辑考勤组
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
            throw new BusinessException("只有企业管理员可以编辑考勤组");
        }
        
        // 检查企业ID权限
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(existGroup.getEnterpriseId())) {
                throw new BusinessException("无权编辑该考勤组");
            }
        }
        
        // 验证工作日配置
        validateWorkDaysConfig(group);
        
        // 更新考勤组
        group.setUpdateUserId(currentUser.getUserId());
        this.updateById(group);
        
        // 删除旧的时间段（软删除）
        LambdaQueryWrapper<AttendanceGroupTimeSlot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceGroupTimeSlot::getGroupId, group.getGroupId())
               .eq(AttendanceGroupTimeSlot::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<AttendanceGroupTimeSlot> oldSlots = timeSlotMapper.selectList(wrapper);
        for (AttendanceGroupTimeSlot oldSlot : oldSlots) {
            oldSlot.setDeleteFlag(DeleteFlag.DELETED.getCode());
            timeSlotMapper.updateById(oldSlot);
        }
        
        // 保存新的时间段
        saveTimeSlots(group.getGroupId(), timeSlots);
        
        return this.getById(group.getGroupId());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteAttendanceGroup(Long groupId) {
        EntityValidationUtil.validateIdNotNull(groupId, "考勤组ID");
        
        AttendanceGroup group = this.getById(groupId);
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        
        // 数据权限检查
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
            throw new BusinessException("只有企业管理员可以删除考勤组");
        }
        
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(group.getEnterpriseId())) {
                throw new BusinessException("无权删除该考勤组");
            }
        }
        
        // 检查是否有学生关联
        LambdaQueryWrapper<AttendanceGroupStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(AttendanceGroupStudent::getGroupId, groupId)
                     .eq(AttendanceGroupStudent::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long studentCount = groupStudentMapper.selectCount(studentWrapper);
        if (studentCount > 0) {
            throw new BusinessException("该考勤组下还有学生，无法删除");
        }
        
        // 软删除考勤组
        group.setDeleteFlag(DeleteFlag.DELETED.getCode());
        this.updateById(group);
        
        // 软删除时间段
        LambdaQueryWrapper<AttendanceGroupTimeSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(AttendanceGroupTimeSlot::getGroupId, groupId)
                  .eq(AttendanceGroupTimeSlot::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<AttendanceGroupTimeSlot> slots = timeSlotMapper.selectList(slotWrapper);
        for (AttendanceGroupTimeSlot slot : slots) {
            slot.setDeleteFlag(DeleteFlag.DELETED.getCode());
            timeSlotMapper.updateById(slot);
        }
        
        return true;
    }
    
    @Override
    public Page<AttendanceGroup> getAttendanceGroupPage(Page<AttendanceGroup> page, String groupName) {
        LambdaQueryWrapper<AttendanceGroup> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(AttendanceGroup::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(groupName)) {
            wrapper.like(AttendanceGroup::getGroupName, groupName);
        }
        
        // 数据权限过滤
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId != null) {
                wrapper.eq(AttendanceGroup::getEnterpriseId, currentUserEnterpriseId);
            } else {
                // 如果没有企业ID，返回空结果
                wrapper.eq(AttendanceGroup::getGroupId, -1L);
            }
        }
        
        // 查询考勤组列表
        Page<AttendanceGroup> result = this.page(page, wrapper);
        
        // 填充统计信息和关联字段
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (AttendanceGroup group : result.getRecords()) {
                fillGroupStatistics(group);
                fillGroupRelatedFields(group);
            }
        }
        
        return result;
    }
    
    @Override
    public AttendanceGroup getAttendanceGroupDetail(Long groupId) {
        EntityValidationUtil.validateIdNotNull(groupId, "考勤组ID");
        
        AttendanceGroup group = this.getById(groupId);
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        
        // 数据权限检查
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(group.getEnterpriseId())) {
                throw new BusinessException("无权查看该考勤组");
            }
        }
        
        // 填充时间段列表
        LambdaQueryWrapper<AttendanceGroupTimeSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(AttendanceGroupTimeSlot::getGroupId, groupId)
                  .eq(AttendanceGroupTimeSlot::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                  .orderByAsc(AttendanceGroupTimeSlot::getSortOrder)
                  .orderByDesc(AttendanceGroupTimeSlot::getIsDefault);
        List<AttendanceGroupTimeSlot> timeSlots = timeSlotMapper.selectList(slotWrapper);
        group.setTimeSlots(timeSlots);
        
        // 填充规则列表
        LambdaQueryWrapper<AttendanceGroupRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(AttendanceGroupRule::getGroupId, groupId)
                  .eq(AttendanceGroupRule::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                  .orderByAsc(AttendanceGroupRule::getRuleType)
                  .orderByAsc(AttendanceGroupRule::getRuleDate);
        List<AttendanceGroupRule> rules = ruleMapper.selectList(ruleWrapper);
        group.setRules(rules);
        
        // 填充统计信息和关联字段
        fillGroupStatistics(group);
        fillGroupRelatedFields(group);
        
        return group;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public AttendanceGroupRule addRule(AttendanceGroupRule rule) {
        EntityValidationUtil.validateIdNotNull(rule.getGroupId(), "考勤组ID");
        if (rule.getRuleType() == null) {
            throw new BusinessException("规则类型不能为空");
        }
        
        // 检查考勤组是否存在
        AttendanceGroup group = this.getById(rule.getGroupId());
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        
        // 数据权限检查：只有企业管理员可以添加规则
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
            throw new BusinessException("只有企业管理员可以添加规则");
        }
        
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(group.getEnterpriseId())) {
                throw new BusinessException("无权为该考勤组添加规则");
            }
        }
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValues(rule, null);
        
        // 保存规则
        ruleMapper.insert(rule);
        
        return rule;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteRule(Long ruleId) {
        EntityValidationUtil.validateIdNotNull(ruleId, "规则ID");
        
        AttendanceGroupRule rule = ruleMapper.selectById(ruleId);
        EntityValidationUtil.validateEntityExists(rule, "规则");
        
        // 数据权限检查
        AttendanceGroup group = this.getById(rule.getGroupId());
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN)) {
            throw new BusinessException("只有企业管理员可以删除规则");
        }
        
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(group.getEnterpriseId())) {
                throw new BusinessException("无权删除该规则");
            }
        }
        
        // 软删除
        rule.setDeleteFlag(DeleteFlag.DELETED.getCode());
        ruleMapper.updateById(rule);
        
        return true;
    }
    
    @Override
    public List<AttendanceGroupRule> getRuleList(Long groupId) {
        EntityValidationUtil.validateIdNotNull(groupId, "考勤组ID");
        
        LambdaQueryWrapper<AttendanceGroupRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceGroupRule::getGroupId, groupId)
              .eq(AttendanceGroupRule::getDeleteFlag, DeleteFlag.NORMAL.getCode())
              .orderByAsc(AttendanceGroupRule::getRuleType)
              .orderByAsc(AttendanceGroupRule::getRuleDate);
        
        return ruleMapper.selectList(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignStudentToGroup(Long groupId, Long applyId, LocalDate effectiveStartDate, LocalDate effectiveEndDate) {
        EntityValidationUtil.validateIdNotNull(groupId, "考勤组ID");
        EntityValidationUtil.validateIdNotNull(applyId, "实习申请ID");
        if (effectiveStartDate == null) {
            throw new BusinessException("生效开始日期不能为空");
        }
        
        // 检查考勤组是否存在
        AttendanceGroup group = this.getById(groupId);
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        
        // 检查实习申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "实习申请");
        
        // 数据权限检查
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN) 
                && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
            throw new BusinessException("无权分配学生到考勤组");
        }
        
        // 企业导师只能分配自己指导的学生
        if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR) && !dataPermissionUtil.isSystemAdmin()) {
            Long currentUserMentorId = dataPermissionUtil.getCurrentUserMentorId();
            if (currentUserMentorId == null || !currentUserMentorId.equals(apply.getMentorId())) {
                throw new BusinessException("只能分配自己指导的学生");
            }
        }
        
        // 检查企业ID是否匹配
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(group.getEnterpriseId())
                    || apply.getEnterpriseId() == null || !currentUserEnterpriseId.equals(apply.getEnterpriseId())) {
                throw new BusinessException("考勤组和企业不匹配");
            }
        }
        
        // 检查生效日期范围
        if (apply.getInternshipStartDate() != null && effectiveStartDate.isBefore(apply.getInternshipStartDate())) {
            throw new BusinessException("生效开始日期不能早于实习开始日期");
        }
        if (apply.getInternshipEndDate() != null && effectiveEndDate != null && effectiveEndDate.isAfter(apply.getInternshipEndDate())) {
            throw new BusinessException("生效结束日期不能晚于实习结束日期");
        }
        
        // 检查该申请是否已分配到其他考勤组
        LambdaQueryWrapper<AttendanceGroupStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceGroupStudent::getApplyId, applyId)
              .eq(AttendanceGroupStudent::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        AttendanceGroupStudent existAssignment = groupStudentMapper.selectOne(wrapper);
        
        if (existAssignment != null) {
            // 如果已分配，先解除旧关联
            existAssignment.setDeleteFlag(DeleteFlag.DELETED.getCode());
            groupStudentMapper.updateById(existAssignment);
        }
        
        // 创建新关联
        AttendanceGroupStudent assignment = new AttendanceGroupStudent();
        assignment.setGroupId(groupId);
        assignment.setApplyId(applyId);
        assignment.setStudentId(apply.getStudentId());
        assignment.setEffectiveStartDate(effectiveStartDate);
        assignment.setEffectiveEndDate(effectiveEndDate);
        assignment.setCreateUserId(currentUser.getUserId());
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(assignment);
        
        groupStudentMapper.insert(assignment);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean batchAssignStudentsToGroup(Long groupId, List<Long> applyIds, LocalDate effectiveStartDate, LocalDate effectiveEndDate) {
        if (applyIds == null || applyIds.isEmpty()) {
            throw new BusinessException("请选择要分配的学生");
        }
        
        for (Long applyId : applyIds) {
            assignStudentToGroup(groupId, applyId, effectiveStartDate, effectiveEndDate);
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean unassignStudentFromGroup(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "实习申请ID");
        
        LambdaQueryWrapper<AttendanceGroupStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceGroupStudent::getApplyId, applyId)
              .eq(AttendanceGroupStudent::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        AttendanceGroupStudent assignment = groupStudentMapper.selectOne(wrapper);
        
        if (assignment == null) {
            throw new BusinessException("该学生未分配到考勤组");
        }
        
        // 数据权限检查
        AttendanceGroup group = this.getById(assignment.getGroupId());
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
        
        if (!dataPermissionUtil.isSystemAdmin() && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_ADMIN) 
                && !DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
            throw new BusinessException("无权解除学生考勤组关联");
        }
        
        // 检查企业ID权限
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(group.getEnterpriseId())) {
                throw new BusinessException("无权解除该学生的考勤组关联");
            }
        }
        
        // 软删除
        assignment.setDeleteFlag(DeleteFlag.DELETED.getCode());
        groupStudentMapper.updateById(assignment);
        
        return true;
    }
    
    @Override
    public List<AttendanceGroupStudent> getGroupStudents(Long groupId) {
        EntityValidationUtil.validateIdNotNull(groupId, "考勤组ID");
        
        LambdaQueryWrapper<AttendanceGroupStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceGroupStudent::getGroupId, groupId)
              .eq(AttendanceGroupStudent::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 数据权限过滤：企业导师只能看到自己指导的学生
        if (!dataPermissionUtil.isSystemAdmin()) {
            UserInfo currentUser = UserUtil.getCurrentUser(userMapper);
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
            if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_ENTERPRISE_MENTOR)) {
                Long currentUserMentorId = dataPermissionUtil.getCurrentUserMentorId();
                if (currentUserMentorId != null) {
                    // 查询该导师指导的申请ID列表
                    LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
                    applyWrapper.eq(InternshipApply::getMentorId, currentUserMentorId)
                               .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                               .select(InternshipApply::getApplyId);
                    List<InternshipApply> applies = internshipApplyMapper.selectList(applyWrapper);
                    if (applies != null && !applies.isEmpty()) {
                        List<Long> applyIds = applies.stream()
                                .map(InternshipApply::getApplyId)
                                .collect(Collectors.toList());
                        wrapper.in(AttendanceGroupStudent::getApplyId, applyIds);
                    } else {
                        wrapper.eq(AttendanceGroupStudent::getApplyId, -1L);
                    }
                }
            }
        }
        
        return groupStudentMapper.selectList(wrapper);
    }
    
    @Override
    public AttendanceGroup getGroupByApplyId(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "实习申请ID");
        
        LambdaQueryWrapper<AttendanceGroupStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AttendanceGroupStudent::getApplyId, applyId)
              .eq(AttendanceGroupStudent::getDeleteFlag, DeleteFlag.NORMAL.getCode())
              .eq(AttendanceGroupStudent::getStatus, UserStatus.ENABLED.getCode());
        AttendanceGroupStudent assignment = groupStudentMapper.selectOne(wrapper);
        
        if (assignment == null) {
            return null;
        }
        
        // 检查生效日期
        LocalDate now = LocalDate.now();
        if (assignment.getEffectiveStartDate() != null && now.isBefore(assignment.getEffectiveStartDate())) {
            return null; // 还未生效
        }
        if (assignment.getEffectiveEndDate() != null && now.isAfter(assignment.getEffectiveEndDate())) {
            return null; // 已过期
        }
        
        AttendanceGroup group = this.getById(assignment.getGroupId());
        if (group == null || !group.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())
                || !group.getStatus().equals(UserStatus.ENABLED.getCode())) {
            return null;
        }
        
        // 填充时间段
        LambdaQueryWrapper<AttendanceGroupTimeSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(AttendanceGroupTimeSlot::getGroupId, group.getGroupId())
                  .eq(AttendanceGroupTimeSlot::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                  .orderByAsc(AttendanceGroupTimeSlot::getSortOrder)
                  .orderByDesc(AttendanceGroupTimeSlot::getIsDefault);
        List<AttendanceGroupTimeSlot> timeSlots = timeSlotMapper.selectList(slotWrapper);
        group.setTimeSlots(timeSlots);
        
        // 填充关联字段
        fillGroupRelatedFields(group);
        
        return group;
    }
    
    @Override
    public List<LocalDate> calculateExpectedDates(Long groupId, LocalDate startDate, LocalDate endDate) {
        EntityValidationUtil.validateIdNotNull(groupId, "考勤组ID");
        if (startDate == null) {
            throw new BusinessException("开始日期不能为空");
        }
        if (endDate == null) {
            throw new BusinessException("结束日期不能为空");
        }
        
        if (startDate.isAfter(endDate)) {
            throw new BusinessException("开始日期不能晚于结束日期");
        }
        
        AttendanceGroup group = this.getById(groupId);
        EntityValidationUtil.validateEntityExists(group, "考勤组");
        
        // 获取节假日规则
        LambdaQueryWrapper<AttendanceGroupRule> ruleWrapper = new LambdaQueryWrapper<>();
        ruleWrapper.eq(AttendanceGroupRule::getGroupId, groupId)
                  .eq(AttendanceGroupRule::getRuleType, 2) // 节假日
                  .eq(AttendanceGroupRule::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<AttendanceGroupRule> holidayRules = ruleMapper.selectList(ruleWrapper);
        
        // 获取特殊日期规则
        LambdaQueryWrapper<AttendanceGroupRule> specialRuleWrapper = new LambdaQueryWrapper<>();
        specialRuleWrapper.eq(AttendanceGroupRule::getGroupId, groupId)
                         .eq(AttendanceGroupRule::getRuleType, 3) // 特殊日期
                         .eq(AttendanceGroupRule::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<AttendanceGroupRule> specialRules = ruleMapper.selectList(specialRuleWrapper);
        
        // 构建节假日日期集合
        Set<LocalDate> holidayDates = new HashSet<>();
        for (AttendanceGroupRule rule : holidayRules) {
            if (rule.getRuleDate() != null) {
                holidayDates.add(rule.getRuleDate());
            } else if (rule.getRuleStartDate() != null && rule.getRuleEndDate() != null) {
                LocalDate date = rule.getRuleStartDate();
                while (!date.isAfter(rule.getRuleEndDate())) {
                    holidayDates.add(date);
                    date = date.plusDays(1);
                }
            }
        }
        
        // 构建特殊日期映射（日期 -> 是否工作日）
        Set<LocalDate> specialWorkdays = new HashSet<>();
        Set<LocalDate> specialNonWorkdays = new HashSet<>();
        for (AttendanceGroupRule rule : specialRules) {
            if (rule.getRuleDate() != null) {
                // 解析ruleValue判断是否为工作日
                if (rule.getRuleValue() != null && rule.getRuleValue().contains("\"is_workday\":true")) {
                    specialWorkdays.add(rule.getRuleDate());
                } else {
                    specialNonWorkdays.add(rule.getRuleDate());
                }
            }
        }
        
        // 根据工作日配置计算应出勤日期
        List<LocalDate> expectedDates = new ArrayList<>();
        LocalDate currentDate = startDate;
        
        while (!currentDate.isAfter(endDate)) {
            // 检查是否为节假日
            if (holidayDates.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            // 检查特殊日期
            if (specialNonWorkdays.contains(currentDate)) {
                currentDate = currentDate.plusDays(1);
                continue;
            }
            if (specialWorkdays.contains(currentDate)) {
                expectedDates.add(currentDate);
                currentDate = currentDate.plusDays(1);
                continue;
            }
            
            // 根据工作日类型判断
            boolean isWorkday = isWorkdayByType(currentDate, group.getWorkDaysType(), group.getWorkDaysConfig());
            if (isWorkday) {
                expectedDates.add(currentDate);
            }
            
            currentDate = currentDate.plusDays(1);
        }
        
        return expectedDates;
    }
    
    @Override
    public int calculateAbsentDays(Long applyId, LocalDate startDate, LocalDate endDate) {
        EntityValidationUtil.validateIdNotNull(applyId, "实习申请ID");
        
        // 获取学生所属的考勤组
        AttendanceGroup group = getGroupByApplyId(applyId);
        if (group == null) {
            return 0; // 没有考勤组，不计算缺勤
        }
        
        // 计算应出勤日期
        List<LocalDate> expectedDates = calculateExpectedDates(group.getGroupId(), startDate, endDate);
        
        // 获取实际考勤记录
        LambdaQueryWrapper<Attendance> attendanceWrapper = new LambdaQueryWrapper<>();
        attendanceWrapper.eq(Attendance::getApplyId, applyId)
                        .ge(Attendance::getAttendanceDate, startDate)
                        .le(Attendance::getAttendanceDate, endDate)
                        .eq(Attendance::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<Attendance> attendances = attendanceMapper.selectList(attendanceWrapper);
        
        // 构建已出勤日期集合（正常、迟到、早退、请假、休息都算已处理）
        Set<LocalDate> attendedDates = new HashSet<>();
        LocalDate now = LocalDate.now();
        
        for (Attendance attendance : attendances) {
            LocalDate attendanceDate = attendance.getAttendanceDate();
            Integer attendanceType = attendance.getAttendanceType();
            
            // 正常、迟到、早退、请假、休息都算已处理（不算缺勤）
            if (attendanceType != null && (attendanceType == 1 || attendanceType == 2 || attendanceType == 3 
                    || attendanceType == 4 || attendanceType == 6)) {
                attendedDates.add(attendanceDate);
            }
        }
        
        // 统计缺勤天数：应出勤日期中，已过去且没有考勤记录的日期
        int absentDays = 0;
        for (LocalDate expectedDate : expectedDates) {
            if (expectedDate.isBefore(now) && !attendedDates.contains(expectedDate)) {
                absentDays++;
            }
        }
        
        return absentDays;
    }
    
    /**
     * 保存时间段列表
     */
    private void saveTimeSlots(Long groupId, List<AttendanceGroupTimeSlot> timeSlots) {
        // 验证至少有一个默认时间段
        long defaultCount = timeSlots.stream()
                .filter(slot -> slot.getIsDefault() != null && slot.getIsDefault() == 1)
                .count();
        if (defaultCount == 0) {
            throw new BusinessException("至少需要设置一个默认时间段");
        }
        if (defaultCount > 1) {
            throw new BusinessException("只能设置一个默认时间段");
        }
        
        int sortOrder = 0;
        for (AttendanceGroupTimeSlot slot : timeSlots) {
            EntityValidationUtil.validateStringNotBlank(slot.getSlotName(), "时间段名称");
            if (slot.getStartTime() == null) {
                throw new BusinessException("上班时间不能为空");
            }
            if (slot.getEndTime() == null) {
                throw new BusinessException("下班时间不能为空");
            }
            
            slot.setGroupId(groupId);
            slot.setSortOrder(sortOrder++);
            EntityDefaultValueUtil.setDefaultValues(slot, null);
            
            timeSlotMapper.insert(slot);
        }
    }
    
    /**
     * 验证工作日配置
     */
    private void validateWorkDaysConfig(AttendanceGroup group) {
        Integer workDaysType = group.getWorkDaysType();
        if (workDaysType == null || workDaysType < 1 || workDaysType > 4) {
            throw new BusinessException("工作日类型无效");
        }
        
        // 如果选择自定义，必须提供workDaysConfig
        if (workDaysType == 4) {
            if (!StringUtils.hasText(group.getWorkDaysConfig())) {
                throw new BusinessException("自定义工作日类型必须配置工作日");
            }
            // 验证JSON格式
            try {
                // 简单验证JSON格式（实际可以使用JSON库验证）
                if (!group.getWorkDaysConfig().startsWith("{") || !group.getWorkDaysConfig().endsWith("}")) {
                    throw new BusinessException("工作日配置格式错误");
                }
            } catch (Exception e) {
                throw new BusinessException("工作日配置格式错误");
            }
        }
    }
    
    /**
     * 根据工作日类型判断指定日期是否为工作日
     */
    private boolean isWorkdayByType(LocalDate date, Integer workDaysType, String workDaysConfig) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        int dayValue = dayOfWeek.getValue(); // 1=Monday, 7=Sunday
        
        switch (workDaysType) {
            case 1: // 周一到周五
                return dayValue >= 1 && dayValue <= 5;
            case 2: // 周一到周六
                return dayValue >= 1 && dayValue <= 6;
            case 3: // 周一到周日
                return true;
            case 4: // 自定义
                if (workDaysConfig == null) {
                    return false;
                }
                // 解析JSON配置
                String dayKey = getDayKey(dayValue);
                return workDaysConfig.contains("\"" + dayKey + "\":true");
            default:
                return false;
        }
    }
    
    /**
     * 获取星期几的JSON key
     */
    private String getDayKey(int dayValue) {
        switch (dayValue) {
            case 1: return "monday";
            case 2: return "tuesday";
            case 3: return "wednesday";
            case 4: return "thursday";
            case 5: return "friday";
            case 6: return "saturday";
            case 7: return "sunday";
            default: return "";
        }
    }
    
    /**
     * 填充考勤组统计信息
     */
    private void fillGroupStatistics(AttendanceGroup group) {
        // 统计学生数量
        LambdaQueryWrapper<AttendanceGroupStudent> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(AttendanceGroupStudent::getGroupId, group.getGroupId())
                     .eq(AttendanceGroupStudent::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long studentCount = groupStudentMapper.selectCount(studentWrapper);
        group.setStudentCount((int) studentCount);
        
        // 统计时间段数量
        LambdaQueryWrapper<AttendanceGroupTimeSlot> slotWrapper = new LambdaQueryWrapper<>();
        slotWrapper.eq(AttendanceGroupTimeSlot::getGroupId, group.getGroupId())
                  .eq(AttendanceGroupTimeSlot::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long timeSlotCount = timeSlotMapper.selectCount(slotWrapper);
        group.setTimeSlotCount((int) timeSlotCount);
    }
    
    /**
     * 填充考勤组关联字段（企业名称、创建人姓名）
     */
    private void fillGroupRelatedFields(AttendanceGroup group) {
        if (group == null) {
            return;
        }
        
        // 填充企业名称
        if (group.getEnterpriseId() != null) {
            Enterprise enterprise = enterpriseMapper.selectById(group.getEnterpriseId());
            if (enterprise != null) {
                group.setEnterpriseName(enterprise.getEnterpriseName());
            }
        }
        
        // 填充创建人姓名
        if (group.getCreateUserId() != null) {
            UserInfo user = userMapper.selectById(group.getCreateUserId());
            if (user != null) {
                group.setCreateUserName(user.getRealName());
            }
        }
    }
}

