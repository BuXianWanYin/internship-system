package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.dto.TeacherAddDTO;
import com.server.internshipserver.domain.user.dto.TeacherUpdateDTO;
import com.server.internshipserver.domain.user.dto.TeacherRegisterDTO;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.SchoolAdminMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.service.user.TeacherService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.domain.user.SchoolAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 教师管理Service实现类
 * 实现教师信息的增删改查、用户关联等业务功能
 */
@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {
    
    private static final Logger logger = LoggerFactory.getLogger(TeacherServiceImpl.class);
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private SchoolAdminMapper schoolAdminMapper;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Override
    public Teacher getTeacherByUserId(Long userId) {
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getUserId, userId);
        // 注意：Teacher表不再有deleteFlag字段，需要通过关联user_info表来过滤
        Teacher teacher = this.getOne(wrapper);
        
        // 验证用户是否已删除
        if (teacher != null && teacher.getUserId() != null) {
            UserInfo user = userService.getUserById(teacher.getUserId());
            if (user == null || user.getDeleteFlag() == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                return null; // 用户已删除，返回null
            }
        }
        
        // 填充角色信息
        if (teacher != null && teacher.getUserId() != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
            teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
        }
        
        return teacher;
    }
    
    @Override
    public Teacher getTeacherByTeacherNo(String teacherNo) {
        if (!StringUtils.hasText(teacherNo)) {
            return null;
        }
        
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Teacher::getTeacherNo, teacherNo);
        // 注意：Teacher表不再有deleteFlag字段，需要通过关联user_info表来过滤
        Teacher teacher = this.getOne(wrapper);
        
        // 验证用户是否已删除
        if (teacher != null && teacher.getUserId() != null) {
            UserInfo user = userService.getUserById(teacher.getUserId());
            if (user == null || user.getDeleteFlag() == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
                return null; // 用户已删除，返回null
            }
        }
        
        return teacher;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher addTeacher(Teacher teacher) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(teacher.getUserId(), "用户ID");
        EntityValidationUtil.validateStringNotBlank(teacher.getTeacherNo(), "工号");
        
        // 检查工号是否已存在
        Teacher existTeacher = getTeacherByTeacherNo(teacher.getTeacherNo());
        if (existTeacher != null) {
            throw new BusinessException("工号已存在");
        }
        
        // 检查用户ID是否已被使用
        Teacher existUserTeacher = getTeacherByUserId(teacher.getUserId());
        if (existUserTeacher != null) {
            throw new BusinessException("该用户已经是教师");
        }
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(teacher);
        
        // 保存
        this.save(teacher);
        
        // 分配班主任角色（默认角色）
        userService.assignRoleToUser(teacher.getUserId(), Constants.ROLE_CLASS_TEACHER);
        
        return teacher;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher updateTeacher(Teacher teacher) {
        if (teacher.getTeacherId() == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        // 检查教师是否存在
        Teacher existTeacher = this.getById(teacher.getTeacherId());
        EntityValidationUtil.validateEntityExists(existTeacher, "教师");
        
        // 权限检查：检查当前用户是否可以编辑该教师对应的用户
        if (!dataPermissionUtil.canEditUser(existTeacher.getUserId())) {
            throw new BusinessException("无权限编辑该教师信息");
        }
        
        // 如果修改了工号，检查新工号是否已存在
        if (StringUtils.hasText(teacher.getTeacherNo()) 
                && !teacher.getTeacherNo().equals(existTeacher.getTeacherNo())) {
            Teacher teacherNoExist = getTeacherByTeacherNo(teacher.getTeacherNo());
            if (teacherNoExist != null) {
                throw new BusinessException("工号已存在");
            }
        }
        
        // 更新
        this.updateById(teacher);
        return this.getById(teacher.getTeacherId());
    }
    
    @Override
    public Teacher getTeacherById(Long teacherId) {
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        Teacher teacher = this.getById(teacherId);
        EntityValidationUtil.validateEntityExists(teacher, "教师");
        
        // 填充角色信息
        if (teacher.getUserId() != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
            teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
        }
        
        return teacher;
    }
    
    @Override
    public Page<Teacher> getTeacherPage(Page<Teacher> page, String teacherNo, Long collegeId, Long schoolId, Integer status) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        
        // 注意：Teacher表不再有deleteFlag字段，需要通过关联user_info表来过滤
        
        // 数据权限过滤：根据用户角色自动添加查询条件（先应用数据权限，再应用前端条件）
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID
        // 学院负责人：添加 college_id = 当前用户学院ID（包括院长自己）
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的教师（包括院长自己）
            // 如果前端传入了collegeId参数，需要验证是否与当前用户的collegeId一致
            if (collegeId != null && !collegeId.equals(currentUserCollegeId)) {
                // 如果传入的collegeId与当前用户的collegeId不一致，返回空结果
                wrapper.eq(Teacher::getCollegeId, -1L);
            } else {
                // 如果没有传入collegeId参数，或者传入的collegeId与当前用户的collegeId一致，则查询本学院的所有教师
                wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
            }
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的教师
            // 如果前端传入了schoolId参数，需要验证是否与当前用户的schoolId一致
            if (schoolId != null && !schoolId.equals(currentUserSchoolId)) {
                // 如果传入的schoolId与当前用户的schoolId不一致，返回空结果
                wrapper.eq(Teacher::getSchoolId, -1L);
            } else {
                // 如果没有传入schoolId参数，或者传入的schoolId与当前用户的schoolId一致，则查询本校的所有教师
                wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
            }
        } else {
            // 系统管理员：可以查看所有教师，应用前端传入的条件
            if (collegeId != null) {
                wrapper.eq(Teacher::getCollegeId, collegeId);
            }
            if (schoolId != null) {
                wrapper.eq(Teacher::getSchoolId, schoolId);
            }
        }
        
        // 其他条件查询（在数据权限过滤之后）
        if (StringUtils.hasText(teacherNo)) {
            wrapper.like(Teacher::getTeacherNo, teacherNo);
        }
        // 注意：不再通过Teacher.status查询，而是通过关联user_info表查询
        
        // 按创建时间倒序
        wrapper.orderByDesc(Teacher::getCreateTime);
        
        Page<Teacher> result = this.page(page, wrapper);
        
        // 通过关联user_info表过滤已删除的用户和状态不匹配的用户
        if (EntityValidationUtil.hasRecords(result)) {
            List<Long> userIds = result.getRecords().stream()
                    .map(Teacher::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!userIds.isEmpty()) {
                // 查询用户信息
                List<UserInfo> users = userService.list(
                        new LambdaQueryWrapper<UserInfo>()
                                .in(UserInfo::getUserId, userIds)
                                .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .apply(status != null, "status = {0}", status)
                );
                
                Set<Long> validUserIds = users.stream()
                        .map(UserInfo::getUserId)
                        .collect(Collectors.toSet());
                
                // 过滤掉已删除或状态不匹配的教师
                result.setRecords(result.getRecords().stream()
                        .filter(teacher -> teacher.getUserId() != null && validUserIds.contains(teacher.getUserId()))
                        .collect(Collectors.toList()));
                
                // 重新计算总数（简化处理，实际应该用JOIN查询）
                result.setTotal(result.getRecords().size());
            } else {
                result.setRecords(new ArrayList<>());
                result.setTotal(0);
            }
        }
        
        // 填充每个教师的角色信息
        if (EntityValidationUtil.hasRecords(result)) {
            for (Teacher teacher : result.getRecords()) {
                if (teacher.getUserId() != null) {
                    List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
                    teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
                }
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTeacher(Long teacherId) {
        if (teacherId == null) {
            throw new BusinessException("教师ID不能为空");
        }
        
        Teacher teacher = this.getById(teacherId);
        EntityValidationUtil.validateEntityExists(teacher, "教师");
        
        // 权限检查：检查当前用户是否可以删除该教师（删除权限与编辑权限相同）
        // 如果教师是待审核状态（auditStatus = 0），可能还没有角色，需要特殊处理
        boolean canDelete = false;
        if (teacher.getAuditStatus() != null && teacher.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
            // 待审核状态的教师，允许系统管理员、学校管理员、学院负责人删除
            // 直接检查是否有编辑用户的权限，这些角色都有权限
            String currentUsername = UserUtil.getCurrentUsername();
            if (currentUsername != null) {
                UserInfo currentUser = userMapper.selectOne(
                        new LambdaQueryWrapper<UserInfo>()
                                .eq(UserInfo::getUsername, currentUsername)
                                .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (currentUser != null) {
                    List<String> currentUserRoles = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                    if (currentUserRoles != null) {
                        canDelete = currentUserRoles.contains(Constants.ROLE_SYSTEM_ADMIN)
                                || currentUserRoles.contains(Constants.ROLE_SCHOOL_ADMIN)
                                || currentUserRoles.contains(Constants.ROLE_COLLEGE_LEADER);
                    }
                }
            }
        } else {
            // 已审核的教师，使用正常的权限检查
            canDelete = dataPermissionUtil.canEditUser(teacher.getUserId());
        }
        
        if (!canDelete) {
            throw new BusinessException("无权限删除该教师");
        }
        
        // 软删除教师表
        boolean teacherDeleted = this.removeById(teacherId);
        
        // 同时更新用户表：软删除和禁用状态
        if (teacherDeleted && teacher.getUserId() != null) {
            UserInfo user = userService.getUserById(teacher.getUserId());
            if (user != null) {
                // 设置用户为禁用状态
                user.setStatus(UserStatus.DISABLED.getCode());
                // 软删除用户（注意：UserService的deleteUser方法有额外的权限检查，这里直接更新）
                user.setDeleteFlag(DeleteFlag.DELETED.getCode());
                userService.updateById(user);
            }
        }
        
        return teacherDeleted;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher addTeacherWithUser(TeacherAddDTO addDTO) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(addDTO.getTeacherNo(), "工号");
        EntityValidationUtil.validateStringNotBlank(addDTO.getRealName(), "真实姓名");
        EntityValidationUtil.validateStringNotBlank(addDTO.getPassword(), "初始密码");
        EntityValidationUtil.validateIdNotNull(addDTO.getCollegeId(), "所属学院ID");
        
        // 检查工号是否已存在
        Teacher existTeacher = getTeacherByTeacherNo(addDTO.getTeacherNo());
        if (existTeacher != null) {
            throw new BusinessException("工号已存在");
        }
        
        // 生成用户名（优先使用提供的用户名，否则使用工号）
        String username = StringUtils.hasText(addDTO.getUsername()) ? addDTO.getUsername() : addDTO.getTeacherNo();
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 创建用户
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(addDTO.getPassword()); // UserService会自动加密
        user.setRealName(addDTO.getRealName());
        user.setGender(addDTO.getGender());
        user.setIdCard(addDTO.getIdCard());
        user.setPhone(addDTO.getPhone());
        user.setEmail(addDTO.getEmail());
        if (addDTO.getStatus() == null) {
            user.setStatus(UserStatus.ENABLED.getCode()); // 默认启用
        } else {
            user.setStatus(addDTO.getStatus());
        }
        user = userService.addUser(user);
        
        // 创建教师记录
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getUserId());
        teacher.setTeacherNo(addDTO.getTeacherNo());
        teacher.setCollegeId(addDTO.getCollegeId());
        teacher.setSchoolId(addDTO.getSchoolId());
        teacher.setTitle(addDTO.getTitle());
        
        // 如果department为空，自动填充为学院名称
        if (!StringUtils.hasText(addDTO.getDepartment()) && addDTO.getCollegeId() != null) {
            College college = collegeMapper.selectById(addDTO.getCollegeId());
            if (college != null) {
                teacher.setDepartment(college.getCollegeName());
            } else {
                teacher.setDepartment(addDTO.getDepartment());
            }
        } else {
            teacher.setDepartment(addDTO.getDepartment());
        }
        
        // 注意：不再设置teacher.status，统一使用user.status
        EntityDefaultValueUtil.setDefaultValues(teacher);
        
        // 保存教师
        this.save(teacher);
        
        // 分配角色：如果指定了角色代码，使用指定的角色，否则默认分配班主任角色
        String finalRoleCode = StringUtils.hasText(addDTO.getRoleCode()) ? addDTO.getRoleCode() : Constants.ROLE_CLASS_TEACHER;
        
        // 权限检查：检查当前用户是否可以分配该角色
        if (!dataPermissionUtil.canAssignRole(finalRoleCode)) {
            throw new BusinessException("无权限分配该角色：" + finalRoleCode);
        }
        
        userService.assignRoleToUser(teacher.getUserId(), finalRoleCode);
        
        // 如果分配的是学校管理员角色，需要创建SchoolAdmin记录
        if (Constants.ROLE_SCHOOL_ADMIN.equals(finalRoleCode) && addDTO.getSchoolId() != null) {
            // 检查是否已经存在SchoolAdmin记录
            SchoolAdmin existSchoolAdmin = schoolAdminMapper.selectOne(
                    new LambdaQueryWrapper<SchoolAdmin>()
                            .eq(SchoolAdmin::getUserId, teacher.getUserId())
                            .eq(SchoolAdmin::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (existSchoolAdmin == null) {
                // 创建SchoolAdmin记录
                SchoolAdmin schoolAdmin = new SchoolAdmin();
                schoolAdmin.setUserId(teacher.getUserId());
                schoolAdmin.setSchoolId(addDTO.getSchoolId());
                schoolAdmin.setStatus(addDTO.getStatus() != null ? addDTO.getStatus() : UserStatus.ENABLED.getCode());
                EntityDefaultValueUtil.setDefaultValues(schoolAdmin);
                schoolAdminMapper.insert(schoolAdmin);
            }
        }
        
        return teacher;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher updateTeacherWithUser(TeacherUpdateDTO updateDTO) {
        EntityValidationUtil.validateIdNotNull(updateDTO.getTeacherId(), "教师ID");
        EntityValidationUtil.validateIdNotNull(updateDTO.getUserId(), "用户ID");
        
        // 检查教师是否存在
        Teacher existTeacher = this.getById(updateDTO.getTeacherId());
        EntityValidationUtil.validateEntityExists(existTeacher, "教师");
        
        // 权限检查：检查当前用户是否可以编辑该教师对应的用户
        // 如果教师是待审核状态（auditStatus = 0），可能还没有角色，需要特殊处理
        boolean canEdit = false;
        if (existTeacher.getAuditStatus() != null && existTeacher.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
            // 待审核状态的教师，允许系统管理员、学校管理员、学院负责人编辑
            // 但学院负责人只能编辑本学院的教师
            String currentUsername = UserUtil.getCurrentUsername();
            if (currentUsername != null) {
                UserInfo currentUser = userMapper.selectOne(
                        new LambdaQueryWrapper<UserInfo>()
                                .eq(UserInfo::getUsername, currentUsername)
                                .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                );
                if (currentUser != null) {
                    List<String> currentUserRoles = userMapper.selectRoleCodesByUserId(currentUser.getUserId());
                    if (currentUserRoles != null) {
                        // 系统管理员可以编辑所有待审核教师
                        if (currentUserRoles.contains(Constants.ROLE_SYSTEM_ADMIN)) {
                            canEdit = true;
                        } 
                        // 学校管理员可以编辑本校的待审核教师
                        else if (currentUserRoles.contains(Constants.ROLE_SCHOOL_ADMIN)) {
                            Long currentUserSchoolId = dataPermissionUtil.getCurrentUserInfoSchoolId();
                            if (currentUserSchoolId != null && currentUserSchoolId.equals(existTeacher.getSchoolId())) {
                                canEdit = true;
                            }
                        } 
                        // 学院负责人只能编辑本学院的待审核教师
                        else if (currentUserRoles.contains(Constants.ROLE_COLLEGE_LEADER)) {
                            Long currentUserCollegeId = dataPermissionUtil.getCurrentUserInfoCollegeId();
                            if (currentUserCollegeId != null && currentUserCollegeId.equals(existTeacher.getCollegeId())) {
                                canEdit = true;
                            }
                        }
                    }
                }
            }
        } else {
            // 已审核的教师，使用正常的权限检查（会自动检查学院级别的数据权限）
            canEdit = dataPermissionUtil.canEditUser(existTeacher.getUserId());
        }
        
        if (!canEdit) {
            throw new BusinessException("无权限编辑该教师信息");
        }
        
        // 如果修改了工号，检查新工号是否已存在
        if (StringUtils.hasText(updateDTO.getTeacherNo()) && !updateDTO.getTeacherNo().equals(existTeacher.getTeacherNo())) {
            Teacher teacherNoExist = getTeacherByTeacherNo(updateDTO.getTeacherNo());
            if (teacherNoExist != null) {
                throw new BusinessException("工号已存在");
            }
        }
        
        // 更新用户信息
        UserInfo user = userService.getUserById(updateDTO.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        if (StringUtils.hasText(updateDTO.getRealName())) {
            user.setRealName(updateDTO.getRealName());
        }
        // 如果传入了gender字段（包括null和空字符串），就更新
        if (updateDTO.getGender() != null) {
            // 如果为空字符串，设置为null；否则设置为传入的值
            user.setGender(updateDTO.getGender().isEmpty() ? null : updateDTO.getGender());
        }
        if (StringUtils.hasText(updateDTO.getIdCard())) {
            user.setIdCard(updateDTO.getIdCard());
        }
        if (StringUtils.hasText(updateDTO.getPhone())) {
            user.setPhone(updateDTO.getPhone());
        }
        if (StringUtils.hasText(updateDTO.getEmail())) {
            user.setEmail(updateDTO.getEmail());
        }
        if (updateDTO.getStatus() != null) {
            user.setStatus(updateDTO.getStatus());
        }
        userService.updateUser(user);
        
        // 更新教师信息
        Teacher teacher = new Teacher();
        teacher.setTeacherId(updateDTO.getTeacherId());
        if (StringUtils.hasText(updateDTO.getTeacherNo())) {
            teacher.setTeacherNo(updateDTO.getTeacherNo());
        }
        if (updateDTO.getCollegeId() != null) {
            teacher.setCollegeId(updateDTO.getCollegeId());
            // 如果修改了学院，自动更新部门为学院名称
            College college = collegeMapper.selectById(updateDTO.getCollegeId());
            if (college != null) {
                teacher.setDepartment(college.getCollegeName());
            }
        }
        if (updateDTO.getSchoolId() != null) {
            teacher.setSchoolId(updateDTO.getSchoolId());
        }
        if (StringUtils.hasText(updateDTO.getTitle())) {
            teacher.setTitle(updateDTO.getTitle());
        }
        // 如果department为空且collegeId不为空，自动填充为学院名称
        if (!StringUtils.hasText(updateDTO.getDepartment()) && updateDTO.getCollegeId() != null) {
            College college = collegeMapper.selectById(updateDTO.getCollegeId());
            if (college != null) {
                teacher.setDepartment(college.getCollegeName());
            }
        } else if (StringUtils.hasText(updateDTO.getDepartment())) {
            teacher.setDepartment(updateDTO.getDepartment());
        }
        // 注意：不再设置teacher.status，统一使用user.status（已在上面更新）
        this.updateById(teacher);
        
        if (StringUtils.hasText(updateDTO.getRoleCode())) {
            userService.assignRoleToUser(updateDTO.getUserId(), updateDTO.getRoleCode());
        }
        
        return this.getById(updateDTO.getTeacherId());
    }
    
    @Override
    public List<Teacher> getTeacherListBySchool(Long schoolId, Long collegeId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        // 注意：Teacher表不再有deleteFlag和status字段，需要通过关联user_info表来过滤
        
        // 数据权限过滤（先应用数据权限，再应用前端条件）
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的教师
            // 如果前端传入了collegeId参数，需要验证是否与当前用户的collegeId一致
            if (collegeId != null && !collegeId.equals(currentUserCollegeId)) {
                // 如果传入的collegeId与当前用户的collegeId不一致，返回空结果
                wrapper.eq(Teacher::getCollegeId, -1L);
            } else {
                // 如果没有传入collegeId参数，或者传入的collegeId与当前用户的collegeId一致，则查询本学院的所有教师
                wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
            }
            // 如果前端传入了schoolId参数，需要验证是否与当前用户的schoolId一致（通过学院关联）
            if (schoolId != null) {
                // 查询当前用户学院所属的学校ID
                Teacher currentUserTeacher = this.getOne(
                        new LambdaQueryWrapper<Teacher>()
                                .eq(Teacher::getCollegeId, currentUserCollegeId)
                                .last("LIMIT 1")
                );
                if (currentUserTeacher != null && currentUserTeacher.getSchoolId() != null) {
                    if (!schoolId.equals(currentUserTeacher.getSchoolId())) {
                        // 如果传入的schoolId与当前用户学院的schoolId不一致，返回空结果
                        wrapper.eq(Teacher::getSchoolId, -1L);
                    }
                }
            }
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的教师
            // 如果前端传入了schoolId参数，需要验证是否与当前用户的schoolId一致
            if (schoolId != null && !schoolId.equals(currentUserSchoolId)) {
                // 如果传入的schoolId与当前用户的schoolId不一致，返回空结果
                wrapper.eq(Teacher::getSchoolId, -1L);
            } else {
                // 如果没有传入schoolId参数，或者传入的schoolId与当前用户的schoolId一致，则查询本校的所有教师
                wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
            }
            // 如果前端传入了collegeId参数，需要验证该学院是否属于当前用户的学校
            if (collegeId != null) {
                // 查询该学院所属的学校ID
                College college = collegeMapper.selectById(collegeId);
                if (college != null && college.getSchoolId() != null) {
                    if (!college.getSchoolId().equals(currentUserSchoolId)) {
                        // 如果传入的collegeId所属的学校与当前用户的schoolId不一致，返回空结果
                        wrapper.eq(Teacher::getCollegeId, -1L);
                    } else {
                        wrapper.eq(Teacher::getCollegeId, collegeId);
                    }
                } else {
                    wrapper.eq(Teacher::getCollegeId, -1L);
                }
            }
        } else {
            // 系统管理员：可以查看所有教师，应用前端传入的条件
            if (schoolId != null) {
                wrapper.eq(Teacher::getSchoolId, schoolId);
            }
            if (collegeId != null) {
                wrapper.eq(Teacher::getCollegeId, collegeId);
            }
        }
        
        wrapper.orderByDesc(Teacher::getCreateTime);
        List<Teacher> teachers = this.list(wrapper);
        
        // 通过关联user_info表过滤已删除和未启用的用户
        if (teachers != null && !teachers.isEmpty()) {
            List<Long> userIds = teachers.stream()
                    .map(Teacher::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!userIds.isEmpty()) {
                List<UserInfo> users = userService.list(
                        new LambdaQueryWrapper<UserInfo>()
                                .in(UserInfo::getUserId, userIds)
                                .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .eq(UserInfo::getStatus, UserStatus.ENABLED.getCode())
                );
                
                Set<Long> validUserIds = users.stream()
                        .map(UserInfo::getUserId)
                        .collect(Collectors.toSet());
                
                teachers = teachers.stream()
                        .filter(teacher -> teacher.getUserId() != null && validUserIds.contains(teacher.getUserId()))
                        .collect(Collectors.toList());
            } else {
                teachers = new ArrayList<>();
            }
        }
        
        return teachers;
    }
    
    @Override
    public List<Teacher> getAllTeachers(String teacherNo, Long collegeId, Long schoolId, Integer status) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        
        // 注意：Teacher表不再有deleteFlag字段，需要通过关联user_info表来过滤
        
        // 数据权限过滤
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            if (collegeId != null && !collegeId.equals(currentUserCollegeId)) {
                wrapper.eq(Teacher::getCollegeId, -1L);
            } else {
                wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
            }
        } else if (currentUserSchoolId != null) {
            if (schoolId != null && !schoolId.equals(currentUserSchoolId)) {
                wrapper.eq(Teacher::getSchoolId, -1L);
            } else {
                wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
            }
        } else {
            if (collegeId != null) {
                wrapper.eq(Teacher::getCollegeId, collegeId);
            }
            if (schoolId != null) {
                wrapper.eq(Teacher::getSchoolId, schoolId);
            }
        }
        
        // 其他条件查询
        if (StringUtils.hasText(teacherNo)) {
            wrapper.like(Teacher::getTeacherNo, teacherNo);
        }
        // 注意：不再通过Teacher.status查询，而是通过关联user_info表查询
        
        // 按创建时间倒序
        wrapper.orderByDesc(Teacher::getCreateTime);
        
        List<Teacher> teachers = this.list(wrapper);
        
        // 通过关联user_info表过滤已删除和状态不匹配的用户
        if (teachers != null && !teachers.isEmpty()) {
            List<Long> userIds = teachers.stream()
                    .map(Teacher::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            if (!userIds.isEmpty()) {
                LambdaQueryWrapper<UserInfo> userWrapper = new LambdaQueryWrapper<UserInfo>()
                        .in(UserInfo::getUserId, userIds)
                        .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
                if (status != null) {
                    userWrapper.eq(UserInfo::getStatus, status);
                }
                List<UserInfo> users = userService.list(userWrapper);
                
                Set<Long> validUserIds = users.stream()
                        .map(UserInfo::getUserId)
                        .collect(Collectors.toSet());
                
                teachers = teachers.stream()
                        .filter(teacher -> teacher.getUserId() != null && validUserIds.contains(teacher.getUserId()))
                        .collect(Collectors.toList());
            } else {
                teachers = new ArrayList<>();
            }
        }
        
        // 填充每个教师的角色信息
        if (teachers != null && !teachers.isEmpty()) {
            for (Teacher teacher : teachers) {
                if (teacher.getUserId() != null) {
                    List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
                    teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
                }
            }
        }
        
        return teachers;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Teacher registerTeacher(TeacherRegisterDTO registerDTO) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(registerDTO.getTeacherNo(), "工号");
        EntityValidationUtil.validateStringNotBlank(registerDTO.getUsername(), "用户名");
        EntityValidationUtil.validateStringNotBlank(registerDTO.getRealName(), "真实姓名");
        EntityValidationUtil.validateStringNotBlank(registerDTO.getPassword(), "密码");
        EntityValidationUtil.validateIdNotNull(registerDTO.getCollegeId(), "所属学院ID");
        
        // 检查工号是否已存在
        Teacher existTeacher = getTeacherByTeacherNo(registerDTO.getTeacherNo());
        if (existTeacher != null) {
            throw new BusinessException("工号已存在");
        }
        
        // 检查用户名是否已存在（用户名必填，用于登录）
        String username = registerDTO.getUsername();
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名已存在");
        }
        
        // 获取学院信息，用于获取学校ID和学院名称
        College college = collegeMapper.selectById(registerDTO.getCollegeId());
        if (college == null) {
            throw new BusinessException("所属学院不存在");
        }
        
        // 创建用户（注册时默认为启用状态，但需要审核才能登录）
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(registerDTO.getPassword()); // UserService会自动加密
        user.setRealName(registerDTO.getRealName());
        user.setGender(registerDTO.getGender());
        user.setIdCard(registerDTO.getIdCard());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setStatus(UserStatus.ENABLED.getCode()); // 注册时默认为启用状态
        user = userService.addUser(user);
        
        // 创建教师记录
        Teacher teacher = new Teacher();
        teacher.setUserId(user.getUserId());
        teacher.setTeacherNo(registerDTO.getTeacherNo());
        teacher.setCollegeId(registerDTO.getCollegeId());
        // 优先使用前端传来的schoolId，否则从学院获取
        teacher.setSchoolId(registerDTO.getSchoolId() != null ? registerDTO.getSchoolId() : college.getSchoolId());
        teacher.setTitle(registerDTO.getTitle());
        // 注意：不再设置department字段，因为数据库表中已无此字段
        
        // 注册时审核状态为待审核（status已在user中设置为启用，但需要审核才能登录）
        teacher.setAuditStatus(AuditStatus.PENDING.getCode());
        
        EntityDefaultValueUtil.setDefaultValues(teacher);
        
        // 保存教师
        this.save(teacher);
        
        // 注意：注册时不分配角色，审核通过后再分配角色
        
        return teacher;
    }
    
    @Override
    public Page<Teacher> getPendingApprovalTeacherPage(Page<Teacher> page, String teacherNo, String realName) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        
        // 注意：Teacher表不再有deleteFlag和status字段，需要通过关联user_info表来过滤
        
        // 只查询待审核状态的教师（auditStatus=0）
        wrapper.eq(Teacher::getAuditStatus, AuditStatus.PENDING.getCode());
        
        // 数据权限过滤
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的待审核教师
            wrapper.eq(Teacher::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的待审核教师
            wrapper.eq(Teacher::getSchoolId, currentUserSchoolId);
        }
        // 系统管理员：可以查看所有待审核教师，不添加额外过滤条件
        
        // 其他条件查询
        if (StringUtils.hasText(teacherNo)) {
            wrapper.like(Teacher::getTeacherNo, teacherNo);
        }
        
        // 如果提供了姓名，需要通过用户表关联查询
        if (StringUtils.hasText(realName)) {
            // 先查询符合条件的用户ID
            LambdaQueryWrapper<UserInfo> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.like(UserInfo::getRealName, realName)
                      .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            List<UserInfo> users = userMapper.selectList(userWrapper);
            if (users != null && !users.isEmpty()) {
                List<Long> userIds = users.stream()
                        .map(UserInfo::getUserId)
                        .collect(Collectors.toList());
                wrapper.in(Teacher::getUserId, userIds);
            } else {
                // 如果没有找到匹配的用户，返回空结果
                wrapper.eq(Teacher::getTeacherId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Teacher::getCreateTime);
        
        Page<Teacher> result = this.page(page, wrapper);
        
        // 填充每个教师的角色信息（虽然注册时没有角色，但为了统一处理）
        if (EntityValidationUtil.hasRecords(result)) {
            for (Teacher teacher : result.getRecords()) {
                if (teacher.getUserId() != null) {
                    List<String> roleCodes = userMapper.selectRoleCodesByUserId(teacher.getUserId());
                    teacher.setRoles(roleCodes != null ? roleCodes : new ArrayList<>());
                }
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveTeacherRegistration(Long teacherId, boolean approved, String auditOpinion) {
        EntityValidationUtil.validateIdNotNull(teacherId, "教师ID");
        
        // 查询教师信息
        Teacher teacher = this.getById(teacherId);
        EntityValidationUtil.validateEntityExists(teacher, "教师");
        
        // 检查教师状态是否为待审核
        if (teacher.getAuditStatus() == null || !teacher.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
            throw new BusinessException("该教师不是待审核状态");
        }
        
        // 数据权限检查：检查当前用户是否可以审核该教师
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能审核本院的教师
            if (!currentUserCollegeId.equals(teacher.getCollegeId())) {
                throw new BusinessException("无权限审核该教师");
            }
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能审核本校的教师
            if (!currentUserSchoolId.equals(teacher.getSchoolId())) {
                throw new BusinessException("无权限审核该教师");
            }
        }
        // 系统管理员：可以审核所有教师
        
        // 查询用户信息
        UserInfo user = userService.getUserById(teacher.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 设置审核状态（auditStatus），这是主要的审核结果字段
        Integer auditStatus = approved ? AuditStatus.APPROVED.getCode() : AuditStatus.REJECTED.getCode();
        teacher.setAuditStatus(auditStatus);
        teacher.setAuditOpinion(auditOpinion);
        teacher.setAuditTime(LocalDateTime.now());
        
        // 获取当前用户作为审核人
        UserInfo currentUser = UserUtil.getCurrentUserOrNull(userMapper);
        if (currentUser != null) {
            teacher.setAuditorId(currentUser.getUserId());
        }
        
        // 根据审核状态（auditStatus）设置启用状态（status）
        if (approved) {
            // 审核通过：激活用户账号（统一使用user.status）
            user.setStatus(UserStatus.ENABLED.getCode());
            
            // 分配默认角色（班主任角色）
            // 注意：审核流程本身已经有权限控制（只有系统管理员、学校管理员、学院负责人可以审核）
            // 这些角色都有权限分配 ROLE_CLASS_TEACHER 角色，所以应该能成功分配
            // 但如果权限检查失败，仍然尝试分配（可能是边缘情况）
            try {
                // 先检查权限，如果有权限则正常分配
                if (dataPermissionUtil.canAssignRole(Constants.ROLE_CLASS_TEACHER)) {
                    userService.assignRoleToUser(teacher.getUserId(), Constants.ROLE_CLASS_TEACHER);
                } else {
                    // 如果没有权限（这种情况理论上不应该发生），记录警告但仍然尝试分配
                    // 因为审核接口本身就有权限控制，审核人应该有权限分配角色
                    logger.warn("审核教师注册时，权限检查失败但继续分配角色。教师ID: {}, 审核人: {}", 
                            teacherId, UserUtil.getCurrentUsername());
                    // 直接调用assignRoleToUser，它会跳过权限检查（如果方法内部有检查）
                    // 或者使用userService的内部方法直接分配
                    userService.assignRoleToUser(teacher.getUserId(), Constants.ROLE_CLASS_TEACHER);
                }
            } catch (Exception e) {
                // 如果角色分配失败，记录错误并抛出异常，阻止审核通过
                // 因为角色分配是审核通过的必要步骤
                logger.error("审核教师注册时，分配角色失败。教师ID: {}, 错误: {}", teacherId, e.getMessage(), e);
                throw new BusinessException("分配角色失败：" + e.getMessage());
            }
        } else {
            // 审核拒绝：保持禁用状态（统一使用user.status）
            user.setStatus(UserStatus.DISABLED.getCode());
        }
        
        // 更新用户和教师状态
        userService.updateUser(user);
        this.updateById(teacher);
        
        return true;
    }
}

