package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.domain.user.EnterpriseMentor;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.EnterpriseMentorMapper;
import com.server.internshipserver.service.user.EnterpriseMentorService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

/**
 * 企业导师管理Service实现类
 */
@Service
public class EnterpriseMentorServiceImpl extends ServiceImpl<EnterpriseMentorMapper, EnterpriseMentor> implements EnterpriseMentorService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Override
    public EnterpriseMentor getEnterpriseMentorByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<EnterpriseMentor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseMentor::getUserId, userId)
               .eq(EnterpriseMentor::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseMentor addEnterpriseMentorWithUser(String mentorName, Long enterpriseId, 
                                                         String position, String department,
                                                         String phone, String email, 
                                                         String password, Integer status) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(mentorName, "导师姓名");
        EntityValidationUtil.validateIdNotNull(enterpriseId, "所属企业ID");
        EntityValidationUtil.validateStringNotBlank(password, "初始密码");
        
        // 数据权限检查：只有系统管理员或企业管理员可以添加企业导师
        // 企业管理员只能添加自己企业的导师
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(enterpriseId)) {
                throw new BusinessException("无权为该企业添加导师");
            }
        }
        
        // 生成用户名（使用手机号或邮箱，如果都没有则使用企业ID+姓名）
        String username;
        if (StringUtils.hasText(phone)) {
            username = phone;
        } else if (StringUtils.hasText(email)) {
            username = email.split("@")[0];
        } else {
            username = "MENTOR_" + enterpriseId + "_" + System.currentTimeMillis();
        }
        
        // 检查用户名是否已存在
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            // 如果用户名已存在，添加时间戳
            username = username + "_" + System.currentTimeMillis();
        }
        
        // 创建用户
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(password); // UserService会自动加密
        user.setRealName(mentorName);
        user.setPhone(phone);
        user.setEmail(email);
        if (status == null) {
            user.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        } else {
            user.setStatus(status);
        }
        user = userService.addUser(user);
        
        // 创建企业导师记录
        EnterpriseMentor mentor = new EnterpriseMentor();
        mentor.setUserId(user.getUserId());
        mentor.setEnterpriseId(enterpriseId);
        mentor.setMentorName(mentorName);
        mentor.setPosition(position);
        mentor.setDepartment(department);
        mentor.setPhone(phone);
        mentor.setEmail(email);
        if (status == null) {
            EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(mentor);
        } else {
            mentor.setStatus(status);
            EntityDefaultValueUtil.setDefaultValues(mentor);
        }
        
        // 保存企业导师
        this.save(mentor);
        
        // 权限检查：检查当前用户是否可以分配企业导师角色
        if (!dataPermissionUtil.canAssignRole(Constants.ROLE_ENTERPRISE_MENTOR)) {
            throw new BusinessException("无权限分配企业导师角色");
        }
        
        // 分配企业导师角色
        userService.assignRoleToUser(mentor.getUserId(), Constants.ROLE_ENTERPRISE_MENTOR);
        
        return mentor;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public EnterpriseMentor updateEnterpriseMentorWithUser(Long mentorId, Long userId,
                                                            String mentorName, Long enterpriseId,
                                                            String position, String department,
                                                            String phone, String email, Integer status) {
        if (mentorId == null) {
            throw new BusinessException("企业导师ID不能为空");
        }
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 检查企业导师是否存在
        EnterpriseMentor existMentor = this.getById(mentorId);
        EntityValidationUtil.validateEntityExists(existMentor, "企业导师");
        
        // 数据权限检查：只有系统管理员或企业管理员可以编辑企业导师
        // 企业管理员只能编辑自己企业的导师
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(existMentor.getEnterpriseId())) {
                throw new BusinessException("无权编辑该企业导师");
            }
            // 如果修改了企业ID，检查新企业ID是否属于当前用户
            if (enterpriseId != null && !enterpriseId.equals(existMentor.getEnterpriseId())) {
                if (!enterpriseId.equals(currentUserEnterpriseId)) {
                    throw new BusinessException("无权将该导师转移到其他企业");
                }
            }
        }
        
        // 更新用户信息
        UserInfo user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(mentorName)) {
            user.setRealName(mentorName);
        }
        if (StringUtils.hasText(phone)) {
            user.setPhone(phone);
        }
        if (StringUtils.hasText(email)) {
            user.setEmail(email);
        }
        if (status != null) {
            user.setStatus(status);
        }
        userService.updateUser(user);
        
        // 更新企业导师信息
        EnterpriseMentor mentor = new EnterpriseMentor();
        mentor.setMentorId(mentorId);
        if (StringUtils.hasText(mentorName)) {
            mentor.setMentorName(mentorName);
        }
        if (enterpriseId != null) {
            mentor.setEnterpriseId(enterpriseId);
        }
        if (StringUtils.hasText(position)) {
            mentor.setPosition(position);
        }
        if (StringUtils.hasText(department)) {
            mentor.setDepartment(department);
        }
        if (StringUtils.hasText(phone)) {
            mentor.setPhone(phone);
        }
        if (StringUtils.hasText(email)) {
            mentor.setEmail(email);
        }
        if (status != null) {
            mentor.setStatus(status);
        }
        this.updateById(mentor);
        
        return this.getById(mentorId);
    }
    
    @Override
    public EnterpriseMentor getEnterpriseMentorById(Long mentorId) {
        if (mentorId == null) {
            throw new BusinessException("企业导师ID不能为空");
        }
        
        EnterpriseMentor mentor = this.getById(mentorId);
        EntityValidationUtil.validateEntityExists(mentor, "企业导师");
        
        return mentor;
    }
    
    @Override
    public Page<EnterpriseMentor> getEnterpriseMentorPage(Page<EnterpriseMentor> page, 
                                                            String mentorName, Long enterpriseId, Integer status) {
        LambdaQueryWrapper<EnterpriseMentor> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, EnterpriseMentor::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(mentorName)) {
            wrapper.like(EnterpriseMentor::getMentorName, mentorName);
        }
        if (enterpriseId != null) {
            wrapper.eq(EnterpriseMentor::getEnterpriseId, enterpriseId);
        }
        if (status != null) {
            wrapper.eq(EnterpriseMentor::getStatus, status);
        }
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        // 系统管理员：不添加过滤条件
        // 学校管理员：只能查看和本校有合作关系的企业的导师
        // 班主任：只能查看和管理的班级有合作关系的企业的导师
        // 企业管理员：只能查看本企业的导师
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            // 企业管理员：只能查看本企业的导师
            wrapper.eq(EnterpriseMentor::getEnterpriseId, currentUserEnterpriseId);
        } else {
            // 学校管理员或班主任：只能查看有合作关系的企业的导师
            List<Long> cooperationEnterpriseIds = dataPermissionUtil.getCooperationEnterpriseIds();
            if (cooperationEnterpriseIds != null) {
                if (cooperationEnterpriseIds.isEmpty()) {
                    // 如果没有合作关系，返回空结果
                    wrapper.eq(EnterpriseMentor::getEnterpriseId, -1L);
                } else {
                    wrapper.in(EnterpriseMentor::getEnterpriseId, cooperationEnterpriseIds);
                }
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(EnterpriseMentor::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteEnterpriseMentor(Long mentorId) {
        if (mentorId == null) {
            throw new BusinessException("企业导师ID不能为空");
        }
        
        EnterpriseMentor mentor = this.getById(mentorId);
        EntityValidationUtil.validateEntityExists(mentor, "企业导师");
        
        // 数据权限检查：只有系统管理员或企业管理员可以删除企业导师
        // 企业管理员只能删除自己企业的导师
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(mentor.getEnterpriseId())) {
                throw new BusinessException("无权删除该企业导师");
            }
        }
        
        // 软删除
        mentor.setDeleteFlag(DeleteFlag.DELETED.getCode());
        this.updateById(mentor);
        
        return true;
    }
    
    @Override
    public List<EnterpriseMentor> getAllEnterpriseMentors(String mentorName, Long enterpriseId, Integer status) {
        LambdaQueryWrapper<EnterpriseMentor> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, EnterpriseMentor::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(mentorName)) {
            wrapper.like(EnterpriseMentor::getMentorName, mentorName);
        }
        if (enterpriseId != null) {
            wrapper.eq(EnterpriseMentor::getEnterpriseId, enterpriseId);
        }
        if (status != null) {
            wrapper.eq(EnterpriseMentor::getStatus, status);
        }
        
        // 数据权限过滤
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            // 企业管理员：只能查看本企业的导师
            wrapper.eq(EnterpriseMentor::getEnterpriseId, currentUserEnterpriseId);
        } else {
            // 学校管理员或班主任：只能查看有合作关系的企业的导师
            List<Long> cooperationEnterpriseIds = dataPermissionUtil.getCooperationEnterpriseIds();
            if (cooperationEnterpriseIds != null) {
                if (cooperationEnterpriseIds.isEmpty()) {
                    wrapper.eq(EnterpriseMentor::getEnterpriseId, -1L);
                } else {
                    wrapper.in(EnterpriseMentor::getEnterpriseId, cooperationEnterpriseIds);
                }
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(EnterpriseMentor::getCreateTime);
        
        return this.list(wrapper);
    }
}

