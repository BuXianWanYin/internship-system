package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.domain.user.SchoolAdmin;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.UserRole;
import com.server.internshipserver.domain.user.Role;
import com.server.internshipserver.mapper.user.SchoolAdminMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.UserRoleMapper;
import com.server.internshipserver.service.user.PermissionService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.service.user.RoleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户管理Service实现类
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserInfo> implements UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private RoleService roleService;
    
    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private PermissionService permissionService;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private SchoolAdminMapper schoolAdminMapper;
    
    @Autowired
    private com.server.internshipserver.mapper.user.EnterpriseMapper enterpriseMapper;
    
    @Override
    public UserInfo getUserByUsername(String username) {
        if (!StringUtils.hasText(username)) {
            return null;
        }
        
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUsername, username)
               .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        UserInfo user = this.getOne(wrapper);
        
        // 填充角色信息
        if (user != null) {
            List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(user.getUserId());
            user.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
        }
        
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo addUser(UserInfo user) {
        // 参数校验
        if (!StringUtils.hasText(user.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(user.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        if (!StringUtils.hasText(user.getRealName())) {
            throw new BusinessException("真实姓名不能为空");
        }
        
        // 检查用户名是否已存在
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserInfo::getUsername, user.getUsername())
               .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        UserInfo existUser = this.getOne(wrapper);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // 设置默认值
        if (user.getStatus() == null) {
            user.setStatus(1); // 默认启用
        }
        user.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        // 保存
        this.save(user);
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo updateUser(UserInfo user) {
        if (user.getUserId() == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 检查用户是否存在
        UserInfo existUser = this.getById(user.getUserId());
        if (existUser == null || existUser.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 权限检查：检查当前用户是否可以编辑目标用户
        if (!dataPermissionUtil.canEditUser(user.getUserId())) {
            throw new BusinessException("无权限编辑该用户信息");
        }
        
        // 如果修改了用户名，检查新用户名是否已存在
        if (StringUtils.hasText(user.getUsername()) 
                && !user.getUsername().equals(existUser.getUsername())) {
            LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(UserInfo::getUsername, user.getUsername())
                   .ne(UserInfo::getUserId, user.getUserId())
                   .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            UserInfo usernameExistUser = this.getOne(wrapper);
            if (usernameExistUser != null) {
                throw new BusinessException("用户名已存在");
            }
        }
        
        // 如果修改了密码，需要加密
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            // 不更新密码
            user.setPassword(null);
        }
        
        // 更新
        this.updateById(user);
        return this.getById(user.getUserId());
    }
    
    @Override
    public UserInfo getUserById(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 填充角色信息
        List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(userId);
        user.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
        
        return user;
    }
    
    @Override
    public Page<UserInfo> getUserPage(Page<UserInfo> page, String username, String realName, String phone, 
                                      Integer status, String roleCodes, Long schoolId, Long collegeId, Long classId) {
        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        wrapper.eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        // 条件查询
        if (StringUtils.hasText(username)) {
            wrapper.like(UserInfo::getUsername, username);
        }
        if (StringUtils.hasText(realName)) {
            wrapper.like(UserInfo::getRealName, realName);
        }
        if (StringUtils.hasText(phone)) {
            wrapper.like(UserInfo::getPhone, phone);
        }
        if (status != null) {
            wrapper.eq(UserInfo::getStatus, status);
        }
        
        // 角色筛选：通过user_role表查询
        List<Long> roleFilterUserIds = getRoleFilterUserIds(roleCodes);
        
        // 组织筛选：通过Student、Teacher表关联查询
        List<Long> orgFilterUserIds = getOrgFilterUserIds(classId, collegeId, schoolId);
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        List<Long> dataPermissionUserIds = getDataPermissionUserIds();
        
        // 合并角色筛选、组织筛选和数据权限过滤的user_id列表
        List<Long> finalUserIds = mergeFilterUserIds(roleFilterUserIds, orgFilterUserIds, dataPermissionUserIds);
        
        // 应用筛选结果
        applyFilterToWrapper(wrapper, finalUserIds);
        // 系统管理员不添加限制
        
        // 按创建时间倒序
        wrapper.orderByDesc(UserInfo::getCreateTime);
        
        Page<UserInfo> result = this.page(page, wrapper);
        
        // 填充每个用户的角色信息
        if (result != null && result.getRecords() != null && !result.getRecords().isEmpty()) {
            for (UserInfo user : result.getRecords()) {
                List<String> userRoleCodes = this.baseMapper.selectRoleCodesByUserId(user.getUserId());
                user.setRoles(userRoleCodes != null ? userRoleCodes : new ArrayList<>());
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 检查用户角色，确保至少保留一个管理员
        List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(userId);
        if (roleCodes != null && !roleCodes.isEmpty()) {
            // 检查是否为系统管理员
            if (roleCodes.contains("ROLE_SYSTEM_ADMIN")) {
                // 统计启用的系统管理员数量
                Role systemAdminRole = roleService.getRoleByRoleCode("ROLE_SYSTEM_ADMIN");
                if (systemAdminRole != null) {
                    List<UserRole> systemAdminUserRoles = userRoleMapper.selectList(
                            new LambdaQueryWrapper<UserRole>()
                                    .eq(UserRole::getRoleId, systemAdminRole.getRoleId())
                                    .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    );
                    if (systemAdminUserRoles != null && !systemAdminUserRoles.isEmpty()) {
                        List<Long> systemAdminUserIds = systemAdminUserRoles.stream()
                                .map(UserRole::getUserId)
                                .distinct()
                                .collect(Collectors.toList());
                        
                        // 查询这些用户中启用的数量
                        long enabledSystemAdminCount = this.count(
                                new LambdaQueryWrapper<UserInfo>()
                                        .in(UserInfo::getUserId, systemAdminUserIds)
                                        .eq(UserInfo::getStatus, 1)
                                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        );
                        
                        if (enabledSystemAdminCount <= 1) {
                            throw new BusinessException("系统至少需要保留一个启用的系统管理员，无法停用");
                        }
                    }
                }
            }
            
            // 检查是否为学校管理员
            if (roleCodes.contains("ROLE_SCHOOL_ADMIN")) {
                SchoolAdmin schoolAdmin = schoolAdminMapper.selectOne(
                        new LambdaQueryWrapper<SchoolAdmin>()
                                .eq(SchoolAdmin::getUserId, userId)
                                .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (schoolAdmin != null) {
                    Long schoolId = schoolAdmin.getSchoolId();
                    // 统计该学校启用的学校管理员数量
                    List<SchoolAdmin> schoolAdmins = schoolAdminMapper.selectList(
                            new LambdaQueryWrapper<SchoolAdmin>()
                                    .eq(SchoolAdmin::getSchoolId, schoolId)
                                    .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    );
                    if (schoolAdmins != null && !schoolAdmins.isEmpty()) {
                        List<Long> schoolAdminUserIds = schoolAdmins.stream()
                                .map(SchoolAdmin::getUserId)
                                .distinct()
                                .collect(Collectors.toList());
                        
                        // 查询这些用户中启用的数量
                        long enabledSchoolAdminCount = this.count(
                                new LambdaQueryWrapper<UserInfo>()
                                        .in(UserInfo::getUserId, schoolAdminUserIds)
                                        .eq(UserInfo::getStatus, 1)
                                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        );
                        
                        if (enabledSchoolAdminCount <= 1) {
                            throw new BusinessException("该学校至少需要保留一个启用的学校管理员，无法停用");
                        }
                    }
                }
            }
            
            // 检查是否为企业管理员
            if (roleCodes.contains("ROLE_ENTERPRISE_ADMIN")) {
                com.server.internshipserver.domain.user.Enterprise enterprise = enterpriseMapper.selectOne(
                        new LambdaQueryWrapper<com.server.internshipserver.domain.user.Enterprise>()
                                .eq(com.server.internshipserver.domain.user.Enterprise::getUserId, userId)
                                .eq(com.server.internshipserver.domain.user.Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (enterprise != null) {
                    Long enterpriseId = enterprise.getEnterpriseId();
                    // 统计该企业启用的企业管理员数量
                    List<com.server.internshipserver.domain.user.Enterprise> enterprises = enterpriseMapper.selectList(
                            new LambdaQueryWrapper<com.server.internshipserver.domain.user.Enterprise>()
                                    .eq(com.server.internshipserver.domain.user.Enterprise::getEnterpriseId, enterpriseId)
                                    .eq(com.server.internshipserver.domain.user.Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    );
                    if (enterprises != null && !enterprises.isEmpty()) {
                        List<Long> enterpriseUserIds = enterprises.stream()
                                .map(com.server.internshipserver.domain.user.Enterprise::getUserId)
                                .filter(id -> id != null)
                                .distinct()
                                .collect(Collectors.toList());
                        
                        if (!enterpriseUserIds.isEmpty()) {
                            // 查询这些用户中启用的数量
                            long enabledEnterpriseAdminCount = this.count(
                                    new LambdaQueryWrapper<UserInfo>()
                                            .in(UserInfo::getUserId, enterpriseUserIds)
                                            .eq(UserInfo::getStatus, 1)
                                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            );
                            
                            if (enabledEnterpriseAdminCount <= 1) {
                                throw new BusinessException("该企业至少需要保留一个启用的企业管理员，无法停用");
                            }
                        }
                    }
                }
            }
        }
        
        // 软删除
        user.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(user);
    }
    
    @Override
    public boolean canDeleteUser(Long userId) {
        if (userId == null) {
            return false;
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            return false;
        }
        
        // 数据权限检查：班主任只能检查自己管理的班级的学生
        if (!checkDataPermissionForUser(userId)) {
            return false;
        }
        
        // 检查用户角色，确保至少保留一个管理员
        List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(userId);
        if (roleCodes != null && !roleCodes.isEmpty()) {
            // 检查是否为系统管理员
            if (roleCodes.contains("ROLE_SYSTEM_ADMIN")) {
                Role systemAdminRole = roleService.getRoleByRoleCode("ROLE_SYSTEM_ADMIN");
                if (systemAdminRole != null) {
                    List<UserRole> systemAdminUserRoles = userRoleMapper.selectList(
                            new LambdaQueryWrapper<UserRole>()
                                    .eq(UserRole::getRoleId, systemAdminRole.getRoleId())
                                    .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    );
                    if (systemAdminUserRoles != null && !systemAdminUserRoles.isEmpty()) {
                        List<Long> systemAdminUserIds = systemAdminUserRoles.stream()
                                .map(UserRole::getUserId)
                                .distinct()
                                .collect(Collectors.toList());
                        
                        long enabledSystemAdminCount = this.count(
                                new LambdaQueryWrapper<UserInfo>()
                                        .in(UserInfo::getUserId, systemAdminUserIds)
                                        .eq(UserInfo::getStatus, 1)
                                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        );
                        
                        if (enabledSystemAdminCount <= 1) {
                            return false;
                        }
                    }
                }
            }
            
            // 检查是否为学校管理员
            if (roleCodes.contains("ROLE_SCHOOL_ADMIN")) {
                SchoolAdmin schoolAdmin = schoolAdminMapper.selectOne(
                        new LambdaQueryWrapper<SchoolAdmin>()
                                .eq(SchoolAdmin::getUserId, userId)
                                .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (schoolAdmin != null) {
                    Long schoolId = schoolAdmin.getSchoolId();
                    List<SchoolAdmin> schoolAdmins = schoolAdminMapper.selectList(
                            new LambdaQueryWrapper<SchoolAdmin>()
                                    .eq(SchoolAdmin::getSchoolId, schoolId)
                                    .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    );
                    if (schoolAdmins != null && !schoolAdmins.isEmpty()) {
                        List<Long> schoolAdminUserIds = schoolAdmins.stream()
                                .map(SchoolAdmin::getUserId)
                                .distinct()
                                .collect(Collectors.toList());
                        
                        long enabledSchoolAdminCount = this.count(
                                new LambdaQueryWrapper<UserInfo>()
                                        .in(UserInfo::getUserId, schoolAdminUserIds)
                                        .eq(UserInfo::getStatus, 1)
                                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        );
                        
                        if (enabledSchoolAdminCount <= 1) {
                            return false;
                        }
                    }
                }
            }
            
            // 检查是否为企业管理员
            if (roleCodes.contains("ROLE_ENTERPRISE_ADMIN")) {
                com.server.internshipserver.domain.user.Enterprise enterprise = enterpriseMapper.selectOne(
                        new LambdaQueryWrapper<com.server.internshipserver.domain.user.Enterprise>()
                                .eq(com.server.internshipserver.domain.user.Enterprise::getUserId, userId)
                                .eq(com.server.internshipserver.domain.user.Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (enterprise != null) {
                    Long enterpriseId = enterprise.getEnterpriseId();
                    List<com.server.internshipserver.domain.user.Enterprise> enterprises = enterpriseMapper.selectList(
                            new LambdaQueryWrapper<com.server.internshipserver.domain.user.Enterprise>()
                                    .eq(com.server.internshipserver.domain.user.Enterprise::getEnterpriseId, enterpriseId)
                                    .eq(com.server.internshipserver.domain.user.Enterprise::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    );
                    if (enterprises != null && !enterprises.isEmpty()) {
                        List<Long> enterpriseUserIds = enterprises.stream()
                                .map(com.server.internshipserver.domain.user.Enterprise::getUserId)
                                .filter(id -> id != null)
                                .distinct()
                                .collect(Collectors.toList());
                        
                        if (!enterpriseUserIds.isEmpty()) {
                            long enabledEnterpriseAdminCount = this.count(
                                    new LambdaQueryWrapper<UserInfo>()
                                            .in(UserInfo::getUserId, enterpriseUserIds)
                                            .eq(UserInfo::getStatus, 1)
                                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            );
                            
                            if (enabledEnterpriseAdminCount <= 1) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean resetPassword(Long userId, String newPassword) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 加密新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        return this.updateById(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean assignRoleToUser(Long userId, String roleCode) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (!StringUtils.hasText(roleCode)) {
            throw new BusinessException("角色代码不能为空");
        }
        
        // 检查用户是否存在
        UserInfo user = this.getById(userId);
        if (user == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 查询角色是否存在
        Role role = roleService.getRoleByRoleCode(roleCode);
        if (role == null || role.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("角色不存在：" + roleCode);
        }
        
        // 检查用户是否已经拥有该角色
        LambdaQueryWrapper<UserRole> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(UserRole::getUserId, userId)
               .eq(UserRole::getRoleId, role.getRoleId())
               .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        UserRole existUserRole = userRoleMapper.selectOne(wrapper);
        if (existUserRole != null) {
            // 用户已经拥有该角色，直接返回成功
            return true;
        }
        
        // 创建用户角色关联
        UserRole userRole = new UserRole();
        userRole.setUserId(userId);
        userRole.setRoleId(role.getRoleId());
        userRole.setDeleteFlag(DeleteFlag.NORMAL.getCode());
        
        boolean success = userRoleMapper.insert(userRole) > 0;
        
        // 角色分配成功后，清除用户权限缓存（如果Redis连接失败，不影响主流程）
        if (success) {
            try {
                permissionService.clearUserPermissionCache(userId);
            } catch (Exception e) {
                // Redis连接失败不影响角色分配，只记录日志
                logger.warn("清除用户权限缓存失败，userId: {}, error: {}", userId, e.getMessage());
            }
        }
        
        return success;
    }
    
    @Override
    public List<Role> getUserRoles(Long userId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        
        // 查询用户角色代码
        List<String> roleCodes = this.baseMapper.selectRoleCodesByUserId(userId);
        if (roleCodes == null || roleCodes.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 根据角色代码查询角色信息
        List<Role> roles = new ArrayList<>();
        for (String roleCode : roleCodes) {
            Role role = roleService.getRoleByRoleCode(roleCode);
            if (role != null) {
                roles.add(role);
            }
        }
        
        return roles;
    }
    
    /**
     * 获取角色筛选的用户ID列表
     */
    private List<Long> getRoleFilterUserIds(String roleCodes) {
        if (!StringUtils.hasText(roleCodes)) {
            return null;
        }
        
        // 解析角色代码（支持逗号分隔的多个角色）
        String[] roleCodeArray = roleCodes.split(",");
        List<String> roleCodeList = new ArrayList<>();
        for (String roleCode : roleCodeArray) {
            if (StringUtils.hasText(roleCode.trim())) {
                roleCodeList.add(roleCode.trim());
            }
        }
        
        if (roleCodeList.isEmpty()) {
            return null;
        }
        
        // 查询这些角色的role_id列表
        List<Role> roles = new ArrayList<>();
        for (String roleCode : roleCodeList) {
            Role role = roleService.getRoleByRoleCode(roleCode);
            if (role != null) {
                roles.add(role);
            }
        }
        
        if (roles.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<Long> roleIds = roles.stream()
                .map(Role::getRoleId)
                .collect(Collectors.toList());
        
        // 查询拥有这些角色的user_id列表
        List<UserRole> userRoles = userRoleMapper.selectList(
                new LambdaQueryWrapper<UserRole>()
                        .in(UserRole::getRoleId, roleIds)
                        .eq(UserRole::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(UserRole::getUserId)
        );
        
        if (userRoles == null || userRoles.isEmpty()) {
            return new ArrayList<>();
        }
        
        return userRoles.stream()
                .map(UserRole::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 获取组织筛选的用户ID列表
     */
    private List<Long> getOrgFilterUserIds(Long classId, Long collegeId, Long schoolId) {
        if (classId == null && collegeId == null && schoolId == null) {
            return null;
        }
        
        List<Long> orgUserIds = new ArrayList<>();
        
        // 班级筛选（优先级最高）
        if (classId != null) {
            addClassUserIds(orgUserIds, classId);
            return orgUserIds.isEmpty() ? new ArrayList<>() : orgUserIds.stream().distinct().collect(Collectors.toList());
        }
        
        // 学院筛选
        if (collegeId != null) {
            addCollegeUserIds(orgUserIds, collegeId);
            return orgUserIds.isEmpty() ? new ArrayList<>() : orgUserIds.stream().distinct().collect(Collectors.toList());
        }
        
        // 学校筛选
        if (schoolId != null) {
            addSchoolUserIds(orgUserIds, schoolId);
            return orgUserIds.isEmpty() ? new ArrayList<>() : orgUserIds.stream().distinct().collect(Collectors.toList());
        }
        
        return new ArrayList<>();
    }
    
    /**
     * 添加班级用户ID
     */
    private void addClassUserIds(List<Long> orgUserIds, Long classId) {
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getClassId, classId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Student::getUserId)
        );
        if (students != null && !students.isEmpty()) {
            orgUserIds.addAll(students.stream()
                    .map(Student::getUserId)
                    .collect(Collectors.toList()));
        }
    }
    
    /**
     * 添加学院用户ID
     */
    private void addCollegeUserIds(List<Long> orgUserIds, Long collegeId) {
        // 查询本院的学生
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getCollegeId, collegeId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Student::getUserId)
        );
        if (students != null && !students.isEmpty()) {
            orgUserIds.addAll(students.stream()
                    .map(Student::getUserId)
                    .collect(Collectors.toList()));
        }
        
        // 查询本院的教师
        List<Teacher> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getCollegeId, collegeId)
                        .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Teacher::getUserId)
        );
        if (teachers != null && !teachers.isEmpty()) {
            orgUserIds.addAll(teachers.stream()
                    .map(Teacher::getUserId)
                    .collect(Collectors.toList()));
        }
    }
    
    /**
     * 添加学校用户ID
     */
    private void addSchoolUserIds(List<Long> orgUserIds, Long schoolId) {
        // 查询本校的学生
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getSchoolId, schoolId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Student::getUserId)
        );
        if (students != null && !students.isEmpty()) {
            orgUserIds.addAll(students.stream()
                    .map(Student::getUserId)
                    .collect(Collectors.toList()));
        }
        
        // 查询本校的教师
        List<Teacher> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getSchoolId, schoolId)
                        .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Teacher::getUserId)
        );
        if (teachers != null && !teachers.isEmpty()) {
            orgUserIds.addAll(teachers.stream()
                    .map(Teacher::getUserId)
                    .collect(Collectors.toList()));
        }
        
        // 查询本校的学校管理员
        List<SchoolAdmin> schoolAdmins = schoolAdminMapper.selectList(
                new LambdaQueryWrapper<SchoolAdmin>()
                        .eq(SchoolAdmin::getSchoolId, schoolId)
                        .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(SchoolAdmin::getUserId)
        );
        if (schoolAdmins != null && !schoolAdmins.isEmpty()) {
            orgUserIds.addAll(schoolAdmins.stream()
                    .map(SchoolAdmin::getUserId)
                    .collect(Collectors.toList()));
        }
    }
    
    /**
     * 获取数据权限过滤的用户ID列表
     */
    private List<Long> getDataPermissionUserIds() {
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        // 班主任：只能查看管理的班级的学生用户（支持多班级）
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            return getClassTeacherUserIds(currentUserClassIds);
        }
        
        // 学院负责人：只能查看本院的用户（学生和教师）
        if (currentUserCollegeId != null) {
            return getCollegeLeaderUserIds(currentUserCollegeId);
        }
        
        // 学校管理员：只能查看本校的用户（学生、教师、学校管理员）
        if (currentUserSchoolId != null) {
            return getSchoolAdminUserIds(currentUserSchoolId);
        }
        
        // 系统管理员不添加限制
        return null;
    }
    
    /**
     * 获取班主任管理的班级学生用户ID列表
     */
    private List<Long> getClassTeacherUserIds(List<Long> classIds) {
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .in(Student::getClassId, classIds)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Student::getUserId)
        );
        if (students == null || students.isEmpty()) {
            return new ArrayList<>();
        }
        return students.stream()
                .map(Student::getUserId)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 获取学院负责人管理的用户ID列表
     */
    private List<Long> getCollegeLeaderUserIds(Long collegeId) {
        List<Long> userIds = new ArrayList<>();
        
        // 查询本院的学生
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getCollegeId, collegeId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Student::getUserId)
        );
        if (students != null && !students.isEmpty()) {
            userIds.addAll(students.stream()
                    .map(Student::getUserId)
                    .collect(Collectors.toList()));
        }
        
        // 查询本院的教师
        List<Teacher> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getCollegeId, collegeId)
                        .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Teacher::getUserId)
        );
        if (teachers != null && !teachers.isEmpty()) {
            userIds.addAll(teachers.stream()
                    .map(Teacher::getUserId)
                    .collect(Collectors.toList()));
        }
        
        return userIds;
    }
    
    /**
     * 获取学校管理员管理的用户ID列表
     */
    private List<Long> getSchoolAdminUserIds(Long schoolId) {
        List<Long> userIds = new ArrayList<>();
        
        // 查询本校的学生
        List<Student> students = studentMapper.selectList(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getSchoolId, schoolId)
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Student::getUserId)
        );
        if (students != null && !students.isEmpty()) {
            userIds.addAll(students.stream()
                    .map(Student::getUserId)
                    .collect(Collectors.toList()));
        }
        
        // 查询本校的教师
        List<Teacher> teachers = teacherMapper.selectList(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getSchoolId, schoolId)
                        .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Teacher::getUserId)
        );
        if (teachers != null && !teachers.isEmpty()) {
            userIds.addAll(teachers.stream()
                    .map(Teacher::getUserId)
                    .collect(Collectors.toList()));
        }
        
        // 查询本校的学校管理员
        List<SchoolAdmin> schoolAdmins = schoolAdminMapper.selectList(
                new LambdaQueryWrapper<SchoolAdmin>()
                        .eq(SchoolAdmin::getSchoolId, schoolId)
                        .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(SchoolAdmin::getUserId)
        );
        if (schoolAdmins != null && !schoolAdmins.isEmpty()) {
            userIds.addAll(schoolAdmins.stream()
                    .map(SchoolAdmin::getUserId)
                    .collect(Collectors.toList()));
        }
        
        return userIds;
    }
    
    /**
     * 合并多个筛选条件的用户ID列表（取交集）
     */
    private List<Long> mergeFilterUserIds(List<Long> roleFilterUserIds, 
                                          List<Long> orgFilterUserIds, 
                                          List<Long> dataPermissionUserIds) {
        List<Long> result = null;
        
        // 三个筛选都存在，取交集
        if (roleFilterUserIds != null && orgFilterUserIds != null && dataPermissionUserIds != null) {
            result = roleFilterUserIds.stream()
                    .filter(orgFilterUserIds::contains)
                    .filter(dataPermissionUserIds::contains)
                    .collect(Collectors.toList());
        }
        // 两个筛选存在
        else if (roleFilterUserIds != null && orgFilterUserIds != null) {
            result = roleFilterUserIds.stream()
                    .filter(orgFilterUserIds::contains)
                    .collect(Collectors.toList());
        } else if (roleFilterUserIds != null && dataPermissionUserIds != null) {
            result = roleFilterUserIds.stream()
                    .filter(dataPermissionUserIds::contains)
                    .collect(Collectors.toList());
        } else if (orgFilterUserIds != null && dataPermissionUserIds != null) {
            result = orgFilterUserIds.stream()
                    .filter(dataPermissionUserIds::contains)
                    .collect(Collectors.toList());
        }
        // 只有一个筛选存在
        else if (roleFilterUserIds != null) {
            result = roleFilterUserIds;
        } else if (orgFilterUserIds != null) {
            result = orgFilterUserIds;
        } else if (dataPermissionUserIds != null) {
            result = dataPermissionUserIds;
        }
        
        return result;
    }
    
    /**
     * 将筛选结果应用到查询包装器
     */
    private void applyFilterToWrapper(LambdaQueryWrapper<UserInfo> wrapper, List<Long> finalUserIds) {
        if (finalUserIds == null) {
            // 系统管理员不添加限制
            return;
        }
        
        if (finalUserIds.isEmpty()) {
            // 如果没有符合条件的用户，返回空结果
            wrapper.eq(UserInfo::getUserId, -1L);
        } else {
            wrapper.in(UserInfo::getUserId, finalUserIds);
        }
    }
    
    /**
     * 检查当前用户是否有权限操作指定用户（数据权限检查）
     */
    private boolean checkDataPermissionForUser(Long targetUserId) {
        // 系统管理员有所有权限
        if (dataPermissionUtil.isSystemAdmin()) {
            return true;
        }
        
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        // 班主任：只能操作自己管理的班级的学生
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, targetUserId)
                            .in(Student::getClassId, currentUserClassIds)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            return student != null;
        }
        
        // 学院负责人：只能操作本院的用户
        if (currentUserCollegeId != null) {
            // 检查是否为本院的学生
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, targetUserId)
                            .eq(Student::getCollegeId, currentUserCollegeId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                return true;
            }
            // 检查是否为本院的教师
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, targetUserId)
                            .eq(Teacher::getCollegeId, currentUserCollegeId)
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            return teacher != null;
        }
        
        // 学校管理员：只能操作本校的用户
        if (currentUserSchoolId != null) {
            // 检查是否为本校的学生
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, targetUserId)
                            .eq(Student::getSchoolId, currentUserSchoolId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                return true;
            }
            // 检查是否为本校的教师
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, targetUserId)
                            .eq(Teacher::getSchoolId, currentUserSchoolId)
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (teacher != null) {
                return true;
            }
            // 检查是否为本校的学校管理员
            SchoolAdmin schoolAdmin = schoolAdminMapper.selectOne(
                    new LambdaQueryWrapper<SchoolAdmin>()
                            .eq(SchoolAdmin::getUserId, targetUserId)
                            .eq(SchoolAdmin::getSchoolId, currentUserSchoolId)
                            .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            return schoolAdmin != null;
        }
        
        // 其他角色：无权限
        return false;
    }
    
    @Override
    public UserInfo getCurrentUser() {
        String username = com.server.internshipserver.common.utils.SecurityUtil.getCurrentUsername();
        if (username == null) {
            throw new BusinessException("未登录或登录已过期");
        }
        UserInfo user = getUserByUsername(username);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public UserInfo updateCurrentUserProfile(UserInfo user) {
        // 获取当前用户
        UserInfo currentUser = getCurrentUser();
        
        // 只允许更新以下字段：realName, phone, email, avatar
        if (StringUtils.hasText(user.getRealName())) {
            currentUser.setRealName(user.getRealName());
        }
        if (StringUtils.hasText(user.getPhone())) {
            currentUser.setPhone(user.getPhone());
        }
        if (StringUtils.hasText(user.getEmail())) {
            currentUser.setEmail(user.getEmail());
        }
        if (user.getAvatar() != null) {
            currentUser.setAvatar(user.getAvatar());
        }
        
        // 更新用户信息
        this.updateById(currentUser);
        
        return currentUser;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean changePassword(String oldPassword, String newPassword) {
        // 参数校验
        if (!StringUtils.hasText(oldPassword)) {
            throw new BusinessException("旧密码不能为空");
        }
        if (!StringUtils.hasText(newPassword)) {
            throw new BusinessException("新密码不能为空");
        }
        if (newPassword.length() < 6 || newPassword.length() > 20) {
            throw new BusinessException("新密码长度必须在6-20个字符之间");
        }
        
        // 获取当前用户
        UserInfo currentUser = getCurrentUser();
        
        // 验证旧密码
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        
        // 加密新密码
        String encodedPassword = passwordEncoder.encode(newPassword);
        currentUser.setPassword(encodedPassword);
        
        // 更新密码
        this.updateById(currentUser);
        
        return true;
    }
}

