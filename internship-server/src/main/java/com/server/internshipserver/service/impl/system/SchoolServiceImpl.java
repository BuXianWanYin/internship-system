package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.domain.user.SchoolAdmin;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.system.SchoolService;
import com.server.internshipserver.service.user.SchoolAdminService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校管理Service实现类
 */
@Service
public class SchoolServiceImpl extends ServiceImpl<SchoolMapper, School> implements SchoolService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private SchoolAdminService schoolAdminService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private UserService userService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public School addSchool(School school) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(school.getSchoolName(), "学校名称");
        EntityValidationUtil.validateStringNotBlank(school.getSchoolCode(), "学校代码");
        
        // 检查学校代码是否已存在
        UniquenessValidationUtil.validateUnique(this, School::getSchoolCode, school.getSchoolCode(), 
                School::getDeleteFlag, "学校代码");
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(school);
        
        // 保存学校
        this.save(school);
        
        // 处理学校管理员绑定（如果提供了管理员信息）
        if (school.getManagerUserId() != null || (school.getCreateNewUser() != null && school.getCreateNewUser())) {
            handleSchoolAdminBinding(school);
        }
        
        return school;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public School updateSchool(School school) {
        EntityValidationUtil.validateIdNotNull(school.getSchoolId(), "学校ID");
        
        // 检查学校是否存在
        School existSchool = this.getById(school.getSchoolId());
        EntityValidationUtil.validateEntityExists(existSchool, "学校");
        
        // 如果修改了学校代码，检查新代码是否已存在
        if (StringUtils.hasText(school.getSchoolCode()) 
                && !school.getSchoolCode().equals(existSchool.getSchoolCode())) {
            UniquenessValidationUtil.validateUniqueExcludeId(this, School::getSchoolCode, school.getSchoolCode(),
                    School::getSchoolId, school.getSchoolId(), School::getDeleteFlag, "学校代码");
        }
        
        // 更新学校
        this.updateById(school);
        
        // 处理学校管理员绑定
        handleSchoolAdminBinding(school);
        
        return this.getById(school.getSchoolId());
    }
    
    /**
     * 处理学校管理员绑定
     * @param school 学校信息
     */
    private void handleSchoolAdminBinding(School school) {
        Long schoolId = school.getSchoolId();
        Long managerUserId = school.getManagerUserId();
        Boolean createNewUser = school.getCreateNewUser();
        UserInfo newUserInfo = school.getNewUserInfo();
        
        // 检查该学校是否已有管理员
        SchoolAdmin currentAdmin = schoolAdminService.getSchoolAdminBySchoolId(schoolId);
        
        // 如果选择创建新用户
        if (createNewUser != null && createNewUser && newUserInfo != null) {
            // 创建新用户
            newUserInfo.setSchoolId(schoolId);
            newUserInfo.setRoles(new ArrayList<>());
            newUserInfo.getRoles().add(Constants.ROLE_SCHOOL_ADMIN);
            UserInfo createdUser = userService.addUser(newUserInfo);
            managerUserId = createdUser.getUserId();
        }
        
        // 如果提供了管理员用户ID，进行绑定
        if (managerUserId != null) {
            // 检查该用户是否已经是其他学校的学校管理员
            SchoolAdmin existAdmin = schoolAdminService.getSchoolAdminByUserId(managerUserId);
            if (existAdmin != null && !existAdmin.getSchoolId().equals(schoolId)) {
                throw new BusinessException("该用户已经是其他学校的学校管理员，无法绑定");
            }
            
            if (currentAdmin != null) {
                // 如果已有管理员且用户ID不同，则更新绑定
                if (!currentAdmin.getUserId().equals(managerUserId)) {
                    // 解除旧绑定（软删除）
                    schoolAdminService.deleteSchoolAdmin(currentAdmin.getAdminId());
                    // 创建新绑定
                    SchoolAdmin newAdmin = new SchoolAdmin();
                    newAdmin.setUserId(managerUserId);
                    newAdmin.setSchoolId(schoolId);
                    schoolAdminService.addSchoolAdmin(newAdmin);
                }
                // 如果用户ID相同，无需操作
            } else {
                // 如果没有管理员，创建新绑定
                SchoolAdmin newAdmin = new SchoolAdmin();
                newAdmin.setUserId(managerUserId);
                newAdmin.setSchoolId(schoolId);
                schoolAdminService.addSchoolAdmin(newAdmin);
            }
        } else {
            // 如果没有提供管理员用户ID，且当前有管理员，则解除绑定
            if (currentAdmin != null) {
                schoolAdminService.deleteSchoolAdmin(currentAdmin.getAdminId());
            }
        }
    }
    
    @Override
    public School getSchoolById(Long schoolId) {
        EntityValidationUtil.validateIdNotNull(schoolId, "学校ID");
        
        School school = this.getById(schoolId);
        EntityValidationUtil.validateEntityExists(school, "学校");
        
        // 数据权限检查：非系统管理员只能查看自己学校的学校信息
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null && !currentUserSchoolId.equals(schoolId)) {
                throw new BusinessException("无权查看该学校信息");
            }
        }
        
        // 填充当前绑定的管理员用户ID（用于编辑时显示）
        SchoolAdmin admin = schoolAdminService.getSchoolAdminBySchoolId(schoolId);
        if (admin != null) {
            school.setManagerUserId(admin.getUserId());
        }
        
        return school;
    }
    
    @Override
    public Page<School> getSchoolPage(Page<School> page, String schoolName, String schoolCode) {
        LambdaQueryWrapper<School> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(School::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(schoolName)) {
            wrapper.like(School::getSchoolName, schoolName);
        }
        if (StringUtils.hasText(schoolCode)) {
            wrapper.eq(School::getSchoolCode, schoolCode);
        }
        
        // 数据权限过滤：学校管理员只能看到自己的学校
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null) {
                wrapper.eq(School::getSchoolId, currentUserSchoolId);
            } else {
                // 如果没有学校ID，返回空结果
                wrapper.eq(School::getSchoolId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(School::getCreateTime);
        
        Page<School> result = this.page(page, wrapper);
        
        // 填充负责人信息（从学校管理员中获取）
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            List<Long> schoolIds = result.getRecords().stream()
                    .map(School::getSchoolId)
                    .collect(Collectors.toList());
            
            // 查询所有学校的管理员
            LambdaQueryWrapper<SchoolAdmin> adminWrapper = QueryWrapperUtil.buildNotDeletedWrapper(SchoolAdmin::getDeleteFlag);
            adminWrapper.in(SchoolAdmin::getSchoolId, schoolIds)
                    .eq(SchoolAdmin::getStatus, UserStatus.ENABLED.getCode());
            List<SchoolAdmin> schoolAdmins = schoolAdminService.list(adminWrapper);
            
            // 获取所有管理员的用户ID
            List<Long> userIds = schoolAdmins.stream()
                    .map(SchoolAdmin::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询用户信息
            List<UserInfo> users = null;
            if (!userIds.isEmpty()) {
                LambdaQueryWrapper<UserInfo> userWrapper = QueryWrapperUtil.buildNotDeletedWrapper(UserInfo::getDeleteFlag);
                userWrapper.in(UserInfo::getUserId, userIds);
                users = userMapper.selectList(userWrapper);
            }
            
            // 构建用户ID到用户信息的映射
            java.util.Map<Long, UserInfo> userMap = null;
            if (users != null && !users.isEmpty()) {
                userMap = users.stream()
                        .collect(Collectors.toMap(UserInfo::getUserId, user -> user, (v1, v2) -> v1));
            }
            
            // 构建学校ID到管理员的映射（取第一个启用的管理员）
            java.util.Map<Long, SchoolAdmin> schoolAdminMap = schoolAdmins.stream()
                    .collect(Collectors.toMap(
                            SchoolAdmin::getSchoolId,
                            admin -> admin,
                            (v1, v2) -> v1  // 如果有多个管理员，取第一个
                    ));
            
            // 填充负责人信息
            for (School school : result.getRecords()) {
                SchoolAdmin admin = schoolAdminMap.get(school.getSchoolId());
                if (admin != null && userMap != null) {
                    UserInfo user = userMap.get(admin.getUserId());
                    if (user != null) {
                        school.setManagerName(user.getRealName());
                        school.setManagerPhone(user.getPhone());
                    }
                }
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteSchool(Long schoolId) {
        if (schoolId == null) {
            throw new BusinessException("学校ID不能为空");
        }
        
        School school = this.getById(schoolId);
        EntityValidationUtil.validateEntityExists(school, "学校");
        
        // 停用：只设置状态为禁用，不删除数据
        school.setStatus(UserStatus.DISABLED.getCode());
        return this.updateById(school);
    }
    
    @Override
    public List<School> getAllSchools(String schoolName, String schoolCode) {
        LambdaQueryWrapper<School> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(School::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(schoolName)) {
            wrapper.like(School::getSchoolName, schoolName);
        }
        if (StringUtils.hasText(schoolCode)) {
            wrapper.eq(School::getSchoolCode, schoolCode);
        }
        
        // 数据权限过滤：学校管理员只能看到自己的学校
        if (!dataPermissionUtil.isSystemAdmin()) {
            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (currentUserSchoolId != null) {
                wrapper.eq(School::getSchoolId, currentUserSchoolId);
            } else {
                // 如果没有学校ID，返回空结果
                wrapper.eq(School::getSchoolId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(School::getCreateTime);
        
        List<School> schools = this.list(wrapper);
        
        // 填充负责人信息（从学校管理员中获取）
        if (schools != null && !schools.isEmpty()) {
            List<Long> schoolIds = schools.stream()
                    .map(School::getSchoolId)
                    .collect(Collectors.toList());
            
            // 查询所有学校的管理员
            LambdaQueryWrapper<SchoolAdmin> adminWrapper = QueryWrapperUtil.buildNotDeletedWrapper(SchoolAdmin::getDeleteFlag);
            adminWrapper.in(SchoolAdmin::getSchoolId, schoolIds)
                    .eq(SchoolAdmin::getStatus, UserStatus.ENABLED.getCode());
            List<SchoolAdmin> schoolAdmins = schoolAdminService.list(adminWrapper);
            
            // 获取所有管理员的用户ID
            List<Long> userIds = schoolAdmins.stream()
                    .map(SchoolAdmin::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            
            // 查询用户信息
            List<UserInfo> users = null;
            if (!userIds.isEmpty()) {
                LambdaQueryWrapper<UserInfo> userWrapper = QueryWrapperUtil.buildNotDeletedWrapper(UserInfo::getDeleteFlag);
                userWrapper.in(UserInfo::getUserId, userIds);
                users = userMapper.selectList(userWrapper);
            }
            
            // 构建用户ID到用户信息的映射
            java.util.Map<Long, UserInfo> userMap = null;
            if (users != null && !users.isEmpty()) {
                userMap = users.stream()
                        .collect(Collectors.toMap(UserInfo::getUserId, user -> user, (v1, v2) -> v1));
            }
            
            // 构建学校ID到管理员的映射（取第一个启用的管理员）
            java.util.Map<Long, SchoolAdmin> schoolAdminMap = schoolAdmins.stream()
                    .collect(Collectors.toMap(
                            SchoolAdmin::getSchoolId,
                            admin -> admin,
                            (v1, v2) -> v1  // 如果有多个管理员，取第一个
                    ));
            
            // 填充负责人信息
            for (School school : schools) {
                SchoolAdmin admin = schoolAdminMap.get(school.getSchoolId());
                if (admin != null && userMap != null) {
                    UserInfo user = userMap.get(admin.getUserId());
                    if (user != null) {
                        school.setManagerName(user.getRealName());
                        school.setManagerPhone(user.getPhone());
                    }
                }
            }
        }
        
        return schools;
    }
    
    @Override
    public List<School> getPublicSchoolList() {
        // 公开接口，不进行权限过滤，只返回所有未删除且启用的学校
        LambdaQueryWrapper<School> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(School::getDeleteFlag);
        wrapper.eq(School::getStatus, UserStatus.ENABLED.getCode()); // 只返回启用的学校
        wrapper.orderByAsc(School::getSchoolName); // 按学校名称排序
        
        List<School> schools = this.list(wrapper);
        
        // 填充 managerName 和 managerPhone（从 contactPerson 和 contactPhone 映射）
        for (School school : schools) {
            school.setManagerName(school.getContactPerson());
            school.setManagerPhone(school.getContactPhone());
        }
        
        return schools;
    }
}

