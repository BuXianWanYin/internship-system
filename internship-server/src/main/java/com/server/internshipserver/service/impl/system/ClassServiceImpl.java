package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.constant.ConfigKeys;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.SystemConfigUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.system.ClassService;
import com.server.internshipserver.service.system.MajorService;
import com.server.internshipserver.service.user.TeacherService;
import com.server.internshipserver.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 班级管理Service实现类
 * 实现班级信息的增删改查、分享码管理、班主任任命等业务功能
 */
@Service
public class ClassServiceImpl extends ServiceImpl<ClassMapper, Class> implements ClassService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private TeacherService teacherService;
    
    @Autowired
    private MajorService majorService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    // 使用Constants中的常量
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Class addClass(Class classInfo) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(classInfo.getClassName(), "班级名称");
        EntityValidationUtil.validateStringNotBlank(classInfo.getClassCode(), "班级代码");
        EntityValidationUtil.validateIdNotNull(classInfo.getMajorId(), "所属专业ID");
        if (classInfo.getEnrollmentYear() == null) {
            throw new BusinessException("入学年份不能为空");
        }
        
        // 检查班级代码在同一专业内是否已存在
        UniquenessValidationUtil.validateUniqueInScope(this, Class::getClassCode, classInfo.getClassCode(),
                Class::getMajorId, classInfo.getMajorId(), Class::getDeleteFlag, "班级代码", "专业");
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(classInfo);
        if (classInfo.getShareCodeUseCount() == null) {
            classInfo.setShareCodeUseCount(0);
        }
        
        // 保存
        this.save(classInfo);
        return classInfo;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Class updateClass(Class classInfo) {
        EntityValidationUtil.validateIdNotNull(classInfo.getClassId(), "班级ID");
        
        // 检查班级是否存在
        Class existClass = this.getById(classInfo.getClassId());
        EntityValidationUtil.validateEntityExists(existClass, "班级");
        
        // 如果修改了班级代码，检查新代码在同一专业内是否已存在
        if (StringUtils.hasText(classInfo.getClassCode()) 
                && !classInfo.getClassCode().equals(existClass.getClassCode())) {
            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Class::getClassCode, classInfo.getClassCode())
                   .eq(Class::getMajorId, existClass.getMajorId())
                   .ne(Class::getClassId, classInfo.getClassId())
                   .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Class codeExistClass = this.getOne(wrapper);
            if (codeExistClass != null) {
                throw new BusinessException("该专业下班级代码已存在");
            }
        }
        
        // 更新
        this.updateById(classInfo);
        return this.getById(classInfo.getClassId());
    }
    
    @Override
    public Class getClassById(Long classId) {
        EntityValidationUtil.validateIdNotNull(classId, "班级ID");
        
        Class classInfo = this.getById(classId);
        EntityValidationUtil.validateEntityExists(classInfo, "班级");
        
        return classInfo;
    }
    
    @Override
    public Page<Class> getClassPage(Page<Class> page, String className, Long majorId, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Class::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(className)) {
            wrapper.like(Class::getClassName, className);
        }
        
        // 筛选条件：学校ID、学院ID、专业ID
        applyOrgFilter(wrapper, schoolId, collegeId, majorId);
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 按创建时间倒序
        wrapper.orderByDesc(Class::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    /**
     * 应用组织筛选条件（学校、学院、专业）
     */
    private void applyOrgFilter(LambdaQueryWrapper<Class> wrapper, Long schoolId, Long collegeId, Long majorId) {
        // 筛选条件：专业ID（优先级最高）
        if (majorId != null) {
            wrapper.eq(Class::getMajorId, majorId);
            return;
        }
        
        // 筛选条件：学院ID（通过专业关联）
        if (collegeId != null) {
            List<Long> majorIds = getMajorIdsByCollegeId(collegeId);
            if (EntityValidationUtil.isNotEmpty(majorIds)) {
                wrapper.in(Class::getMajorId, majorIds);
            } else {
                wrapper.eq(Class::getClassId, -1L); // 返回空结果
            }
            return;
        }
        
        // 筛选条件：学校ID（通过专业和学院关联）
        if (schoolId != null) {
            List<Long> majorIds = getMajorIdsBySchoolId(schoolId);
            if (EntityValidationUtil.isNotEmpty(majorIds)) {
                wrapper.in(Class::getMajorId, majorIds);
            } else {
                wrapper.eq(Class::getClassId, -1L); // 返回空结果
            }
        }
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<Class> wrapper) {
        // 先检查用户角色，如果是班主任，必须严格按班级ID过滤
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
            // 如果用户是班主任角色，必须通过班级ID过滤
            if (DataPermissionUtil.hasRole(roleCodes, Constants.ROLE_CLASS_TEACHER)) {
                List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
                // 班主任：只能查看管理的班级信息（支持多班级）
                // 如果没有管理的班级，返回空结果
                if (EntityValidationUtil.isNotEmpty(currentUserClassIds)) {
                    wrapper.in(Class::getClassId, currentUserClassIds);
                } else {
                    wrapper.eq(Class::getClassId, -1L); // 返回空结果
                }
                return;
            }
        }
        
        // 非班主任角色的数据权限过滤
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        // 学生：只能查看自己的班级
        if (EntityValidationUtil.isNotEmpty(currentUserClassIds)) {
            wrapper.in(Class::getClassId, currentUserClassIds);
            return;
        }
        
        // 学院负责人：只能查看本院的班级（通过专业关联）
        if (currentUserCollegeId != null) {
            List<Long> majorIds = getMajorIdsByCollegeId(currentUserCollegeId);
            if (EntityValidationUtil.isNotEmpty(majorIds)) {
                wrapper.in(Class::getMajorId, majorIds);
            } else {
                wrapper.eq(Class::getClassId, -1L); // 返回空结果
            }
            return;
        }
        
        // 学校管理员：只能查看本校的班级（通过专业和学院关联）
        if (currentUserSchoolId != null) {
            List<Long> majorIds = getMajorIdsBySchoolId(currentUserSchoolId);
            if (EntityValidationUtil.isNotEmpty(majorIds)) {
                wrapper.in(Class::getMajorId, majorIds);
            } else {
                wrapper.eq(Class::getClassId, -1L); // 返回空结果
            }
        }
    }
    
    /**
     * 根据学院ID获取专业ID列表
     */
    private List<Long> getMajorIdsByCollegeId(Long collegeId) {
        List<Major> majors = majorService.list(
                new LambdaQueryWrapper<Major>()
                        .eq(Major::getCollegeId, collegeId)
                        .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Major::getMajorId)
        );
        if (majors == null || majors.isEmpty()) {
            return null;
        }
        return majors.stream()
                .map(Major::getMajorId)
                .collect(Collectors.toList());
    }
    
    /**
     * 根据学校ID获取专业ID列表
     */
    private List<Long> getMajorIdsBySchoolId(Long schoolId) {
        // 先查询该学校下的学院ID列表
        List<College> colleges = collegeMapper.selectList(
                new LambdaQueryWrapper<College>()
                        .eq(College::getSchoolId, schoolId)
                        .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(College::getCollegeId)
        );
        if (colleges == null || colleges.isEmpty()) {
            return null;
        }
        
        List<Long> collegeIds = colleges.stream()
                .map(College::getCollegeId)
                .collect(Collectors.toList());
        
        // 再查询这些学院的专业ID列表
        List<Major> majors = majorService.list(
                new LambdaQueryWrapper<Major>()
                        .in(Major::getCollegeId, collegeIds)
                        .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                        .select(Major::getMajorId)
        );
        if (majors == null || majors.isEmpty()) {
            return null;
        }
        
        return majors.stream()
                .map(Major::getMajorId)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableClass(Long classId) {
        EntityValidationUtil.validateIdNotNull(classId, "班级ID");
        
        Class classInfo = this.getById(classId);
        EntityValidationUtil.validateEntityExists(classInfo, "班级");
        
        // 只设置停用状态，不删除数据
        classInfo.setStatus(UserStatus.DISABLED.getCode());
        return this.updateById(classInfo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableClass(Long classId) {
        EntityValidationUtil.validateIdNotNull(classId, "班级ID");
        
        Class classInfo = this.getById(classId);
        EntityValidationUtil.validateEntityExists(classInfo, "班级");
        
        // 设置启用状态
        classInfo.setStatus(UserStatus.ENABLED.getCode());
        return this.updateById(classInfo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteClass(Long classId) {
        EntityValidationUtil.validateIdNotNull(classId, "班级ID");
        
        Class classInfo = this.getById(classId);
        EntityValidationUtil.validateEntityExists(classInfo, "班级");
        
        // 软删除：同时设置删除标志和停用状态
        classInfo.setDeleteFlag(DeleteFlag.DELETED.getCode());
        classInfo.setStatus(UserStatus.DISABLED.getCode());
        return this.updateById(classInfo);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String generateShareCode(Long classId) {
        Class classInfo = getClassById(classId);
        
        // 如果已有分享码且未过期，直接返回
        if (StringUtils.hasText(classInfo.getShareCode()) 
                && classInfo.getShareCodeExpireTime() != null
                && classInfo.getShareCodeExpireTime().isAfter(LocalDateTime.now())) {
            return classInfo.getShareCode();
        }
        
        // 生成新的分享码
        String shareCode = generateUniqueShareCode();
        LocalDateTime now = LocalDateTime.now();
        // 从系统配置读取分享码有效期，默认30天
        int expireDays = SystemConfigUtil.getConfigValueInt(ConfigKeys.SHARE_CODE_EXPIRE_DAYS, Constants.SHARE_CODE_EXPIRE_DAYS);
        LocalDateTime expireTime = now.plusDays(expireDays);
        
        classInfo.setShareCode(shareCode);
        classInfo.setShareCodeGenerateTime(now);
        classInfo.setShareCodeExpireTime(expireTime);
        classInfo.setShareCodeUseCount(0);
        
        this.updateById(classInfo);
        return shareCode;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public String regenerateShareCode(Long classId) {
        Class classInfo = getClassById(classId);
        
        // 生成新的分享码
        String shareCode = generateUniqueShareCode();
        LocalDateTime now = LocalDateTime.now();
        // 从系统配置读取分享码有效期，默认30天
        int expireDays = SystemConfigUtil.getConfigValueInt(ConfigKeys.SHARE_CODE_EXPIRE_DAYS, Constants.SHARE_CODE_EXPIRE_DAYS);
        LocalDateTime expireTime = now.plusDays(expireDays);
        
        classInfo.setShareCode(shareCode);
        classInfo.setShareCodeGenerateTime(now);
        classInfo.setShareCodeExpireTime(expireTime);
        classInfo.setShareCodeUseCount(0); // 重新生成后重置使用次数
        
        this.updateById(classInfo);
        return shareCode;
    }
    
    @Override
    public Class validateShareCode(String shareCode) {
        if (!StringUtils.hasText(shareCode)) {
            return null;
        }
        
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Class::getShareCode, shareCode)
               .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        Class classInfo = this.getOne(wrapper);
        
        if (classInfo == null) {
            return null;
        }
        
        // 检查分享码是否过期
        if (classInfo.getShareCodeExpireTime() == null 
                || classInfo.getShareCodeExpireTime().isBefore(LocalDateTime.now())) {
            return null;
        }
        
        return classInfo;
    }
    
    @Override
    public Map<String, Object> getShareCodeInfo(Long classId) {
        Class classInfo = getClassById(classId);
        if (classInfo == null) {
            throw new BusinessException("班级不存在");
        }
        
        // 查询通过该分享码注册的学生数量（已审核通过的学生）
        Long registeredStudentCount = 0L;
        if (StringUtils.hasText(classInfo.getShareCode()) && classInfo.getShareCodeGenerateTime() != null) {
            // 查询该班级中通过分享码注册的学生数量
            // 通过创建时间和班级ID来判断（分享码注册的学生创建时间应该在分享码生成时间之后）
            registeredStudentCount = studentMapper.selectCount(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getClassId, classId)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .eq(Student::getStatus, UserStatus.ENABLED.getCode()) // 已审核通过
                            .ge(Student::getCreateTime, classInfo.getShareCodeGenerateTime())
            );
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("shareCode", classInfo.getShareCode());
        data.put("generateTime", classInfo.getShareCodeGenerateTime());
        data.put("expireTime", classInfo.getShareCodeExpireTime());
        data.put("useCount", classInfo.getShareCodeUseCount());
        data.put("registeredStudentCount", registeredStudentCount); // 通过分享码注册的学生数量
        
        return data;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void incrementShareCodeUseCount(String shareCode) {
        Class classInfo = validateShareCode(shareCode);
        if (classInfo == null) {
            throw new BusinessException("分享码无效或已过期");
        }
        
        classInfo.setShareCodeUseCount(classInfo.getShareCodeUseCount() + 1);
        this.updateById(classInfo);
    }
    
    /**
     * 生成唯一的分享码
     */
    private String generateUniqueShareCode() {
        Random random = new Random();
        String shareCode;
        int maxAttempts = 100; // 最多尝试100次
        
        // 从系统配置读取分享码长度，默认8位
        int shareCodeLength = SystemConfigUtil.getConfigValueInt(ConfigKeys.SHARE_CODE_LENGTH, Constants.SHARE_CODE_LENGTH);
        
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < shareCodeLength; i++) {
                sb.append(CHARACTERS.charAt(random.nextInt(CHARACTERS.length())));
            }
            shareCode = sb.toString();
            
            // 检查是否已存在
            LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Class::getShareCode, shareCode)
                   .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Class existClass = this.getOne(wrapper);
            
            if (existClass == null) {
                break; // 找到唯一的分享码
            }
            
            maxAttempts--;
        } while (maxAttempts > 0);
        
        if (maxAttempts <= 0) {
            throw new BusinessException("生成分享码失败，请重试");
        }
        
        return shareCode;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean appointClassTeacher(Long classId, Long teacherId) {
        EntityValidationUtil.validateIdNotNull(classId, "班级ID");
        EntityValidationUtil.validateIdNotNull(teacherId, "教师ID");
        
        // 查询班级信息
        Class classInfo = this.getById(classId);
        EntityValidationUtil.validateEntityExists(classInfo, "班级");
        
        // 数据权限检查：学院负责人只能任命本院的班级
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (currentUserCollegeId != null) {
            // 获取班级所属的专业
            Major major = majorService.getById(classInfo.getMajorId());
            if (major == null) {
                throw new BusinessException("专业不存在");
            }
            if (!currentUserCollegeId.equals(major.getCollegeId())) {
                throw new BusinessException("无权限任命该班级的班主任");
            }
        }
        
        // 查询教师信息
        Teacher teacher = teacherService.getById(teacherId);
        EntityValidationUtil.validateEntityExists(teacher, "教师");
        
        // 检查教师是否属于同一个学院（如果是学院负责人操作）
        if (currentUserCollegeId != null && !currentUserCollegeId.equals(teacher.getCollegeId())) {
            throw new BusinessException("该教师不属于本学院");
        }
        
        // 检查教师是否有用户ID
        if (teacher.getUserId() == null) {
            throw new BusinessException("该教师未关联用户账号");
        }
        
        // 设置班主任（使用userId，因为class_teacher_id外键引用user_info.user_id）
        classInfo.setClassTeacherId(teacher.getUserId());
        
        // 更新班级信息
        this.updateById(classInfo);
        
        // 分配班主任角色
        userService.assignRoleToUser(teacher.getUserId(), Constants.ROLE_CLASS_TEACHER);
        
        return true;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean removeClassTeacher(Long classId) {
        EntityValidationUtil.validateIdNotNull(classId, "班级ID");
        
        // 查询班级信息
        Class classInfo = this.getById(classId);
        EntityValidationUtil.validateEntityExists(classInfo, "班级");
        
        // 数据权限检查：学院负责人只能取消本院的班级班主任
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (currentUserCollegeId != null) {
            // 获取班级所属的专业
            Major major = majorService.getById(classInfo.getMajorId());
            if (major == null || !currentUserCollegeId.equals(major.getCollegeId())) {
                throw new BusinessException("无权限取消该班级的班主任");
            }
        }
        
        // 取消班主任（设置为null）
        classInfo.setClassTeacherId(null);
        
        // 更新班级信息
        return this.updateById(classInfo);
    }
    
    @Override
    public List<Class> getAllClasses(String className, Long majorId, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Class::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(className)) {
            wrapper.like(Class::getClassName, className);
        }
        
        // 筛选条件：学校ID、学院ID、专业ID
        applyOrgFilter(wrapper, schoolId, collegeId, majorId);
        
        // 数据权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 按创建时间倒序
        wrapper.orderByDesc(Class::getCreateTime);
        
        return this.list(wrapper);
    }
}

