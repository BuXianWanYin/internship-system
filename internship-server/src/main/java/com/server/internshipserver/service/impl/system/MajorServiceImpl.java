package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import com.server.internshipserver.domain.internship.InternshipPlan;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.mapper.internship.InternshipPlanMapper;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.service.system.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 专业管理Service实现类
 * 实现专业信息的增删改查等业务功能
 */
@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private com.server.internshipserver.mapper.system.SchoolMapper schoolMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private InternshipPlanMapper internshipPlanMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Major addMajor(Major major) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(major.getMajorName(), "专业名称");
        EntityValidationUtil.validateStringNotBlank(major.getMajorCode(), "专业代码");
        EntityValidationUtil.validateIdNotNull(major.getCollegeId(), "所属学院ID");
        
        // 检查专业代码在同一学院内是否已存在
        UniquenessValidationUtil.validateUniqueInScope(this, Major::getMajorCode, major.getMajorCode(),
                Major::getCollegeId, major.getCollegeId(), Major::getDeleteFlag, "专业代码", "学院");
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(major);
        
        // 保存
        this.save(major);
        return major;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Major updateMajor(Major major) {
        EntityValidationUtil.validateIdNotNull(major.getMajorId(), "专业ID");
        
        // 检查专业是否存在
        Major existMajor = this.getById(major.getMajorId());
        EntityValidationUtil.validateEntityExists(existMajor, "专业");
        
        // 如果修改了专业代码，检查新代码在同一学院内是否已存在
        if (StringUtils.hasText(major.getMajorCode()) 
                && !major.getMajorCode().equals(existMajor.getMajorCode())) {
            LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Major::getMajorCode, major.getMajorCode())
                   .eq(Major::getCollegeId, existMajor.getCollegeId())
                   .ne(Major::getMajorId, major.getMajorId())
                   .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            Major codeExistMajor = this.getOne(wrapper);
            if (codeExistMajor != null) {
                throw new BusinessException("该学院下专业代码已存在");
            }
        }
        
        // 更新
        this.updateById(major);
        return this.getById(major.getMajorId());
    }
    
    @Override
    public Major getMajorById(Long majorId) {
        EntityValidationUtil.validateIdNotNull(majorId, "专业ID");
        
        Major major = this.getById(majorId);
        EntityValidationUtil.validateEntityExists(major, "专业");
        
        return major;
    }
    
    @Override
    public Page<Major> getMajorPage(Page<Major> page, String majorName, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Major::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(majorName)) {
            wrapper.like(Major::getMajorName, majorName);
        }
        
        // 筛选条件：学校ID（如果指定了schoolId，需要通过学院关联）
        if (schoolId != null) {
            // 先查询该学校下的学院ID列表
            List<College> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<College>()
                            .eq(College::getSchoolId, schoolId)
                            .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(College::getCollegeId)
            );
            if (colleges != null && !colleges.isEmpty()) {
                List<Long> collegeIds = colleges.stream()
                        .map(College::getCollegeId)
                        .collect(Collectors.toList());
                wrapper.in(Major::getCollegeId, collegeIds);
            } else {
                // 如果该学校没有学院，返回空结果
                wrapper.eq(Major::getMajorId, -1L);
            }
        }
        
        // 筛选条件：学院ID
        if (collegeId != null) {
            wrapper.eq(Major::getCollegeId, collegeId);
        }
        
        // 数据权限过滤：根据学院ID过滤
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID（通过学院关联）
        // 学院负责人：添加 college_id = 当前用户学院ID
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的专业
            wrapper.eq(Major::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的专业（通过学院关联）
            // 先查询本校的学院ID列表，然后使用in方法
            List<College> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<College>()
                            .eq(College::getSchoolId, currentUserSchoolId)
                            .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(College::getCollegeId)
            );
            if (colleges != null && !colleges.isEmpty()) {
                List<Long> collegeIds = colleges.stream()
                        .map(College::getCollegeId)
                        .collect(Collectors.toList());
                wrapper.in(Major::getCollegeId, collegeIds);
            } else {
                // 如果没有学院，返回空结果
                wrapper.eq(Major::getMajorId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Major::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean disableMajor(Long majorId) {
        EntityValidationUtil.validateIdNotNull(majorId, "专业ID");
        
        Major major = this.getById(majorId);
        EntityValidationUtil.validateEntityExists(major, "专业");
        
        // 检查是否有班级关联，如果有班级则不允许停用
        LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(Class::getMajorId, majorId)
                   .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long classCount = classMapper.selectCount(classWrapper);
        if (classCount > 0) {
            throw new BusinessException("该专业下还有" + classCount + "个班级，不允许停用");
        }
        
        // 检查是否有学生关联，如果有学生则不允许停用
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getMajorId, majorId)
                     .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long studentCount = studentMapper.selectCount(studentWrapper);
        if (studentCount > 0) {
            throw new BusinessException("该专业下还有" + studentCount + "名学生，不允许停用");
        }
        
        // 检查是否有实习计划关联，如果有实习计划则不允许停用
        LambdaQueryWrapper<InternshipPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(InternshipPlan::getMajorId, majorId)
                  .eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long planCount = internshipPlanMapper.selectCount(planWrapper);
        if (planCount > 0) {
            throw new BusinessException("该专业下还有" + planCount + "个实习计划，不允许停用");
        }
        
        // 只设置停用状态，不删除数据
        major.setStatus(UserStatus.DISABLED.getCode());
        return this.updateById(major);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean enableMajor(Long majorId) {
        EntityValidationUtil.validateIdNotNull(majorId, "专业ID");
        
        Major major = this.getById(majorId);
        EntityValidationUtil.validateEntityExists(major, "专业");
        
        // 设置启用状态
        major.setStatus(UserStatus.ENABLED.getCode());
        return this.updateById(major);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMajor(Long majorId) {
        EntityValidationUtil.validateIdNotNull(majorId, "专业ID");
        
        Major major = this.getById(majorId);
        EntityValidationUtil.validateEntityExists(major, "专业");
        
        // 检查是否有班级关联，如果有班级则不允许删除
        LambdaQueryWrapper<Class> classWrapper = new LambdaQueryWrapper<>();
        classWrapper.eq(Class::getMajorId, majorId)
                   .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long classCount = classMapper.selectCount(classWrapper);
        if (classCount > 0) {
            throw new BusinessException("该专业下还有" + classCount + "个班级，不允许删除");
        }
        
        // 检查是否有学生关联，如果有学生则不允许删除
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getMajorId, majorId)
                     .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long studentCount = studentMapper.selectCount(studentWrapper);
        if (studentCount > 0) {
            throw new BusinessException("该专业下还有" + studentCount + "名学生，不允许删除");
        }
        
        // 检查是否有实习计划关联，如果有实习计划则不允许删除
        LambdaQueryWrapper<InternshipPlan> planWrapper = new LambdaQueryWrapper<>();
        planWrapper.eq(InternshipPlan::getMajorId, majorId)
                  .eq(InternshipPlan::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        long planCount = internshipPlanMapper.selectCount(planWrapper);
        if (planCount > 0) {
            throw new BusinessException("该专业下还有" + planCount + "个实习计划，不允许删除");
        }
        
        // 使用MyBatis Plus逻辑删除
        // 先设置状态为禁用，然后逻辑删除
        major.setStatus(UserStatus.DISABLED.getCode());
        this.updateById(major);
        return this.removeById(majorId);
    }
    
    @Override
    public List<Major> getAllMajors(String majorName, Long collegeId, Long schoolId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Major::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(majorName)) {
            wrapper.like(Major::getMajorName, majorName);
        }
        
        // 筛选条件：学校ID（如果指定了schoolId，需要通过学院关联）
        if (schoolId != null) {
            List<College> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<College>()
                            .eq(College::getSchoolId, schoolId)
                            .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(College::getCollegeId)
            );
            if (colleges != null && !colleges.isEmpty()) {
                List<Long> collegeIds = colleges.stream()
                        .map(College::getCollegeId)
                        .collect(Collectors.toList());
                wrapper.in(Major::getCollegeId, collegeIds);
            } else {
                wrapper.eq(Major::getMajorId, -1L);
            }
        }
        
        // 筛选条件：学院ID
        if (collegeId != null) {
            wrapper.eq(Major::getCollegeId, collegeId);
        }
        
        // 数据权限过滤
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserCollegeId != null) {
            wrapper.eq(Major::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            List<College> colleges = collegeMapper.selectList(
                    new LambdaQueryWrapper<College>()
                            .eq(College::getSchoolId, currentUserSchoolId)
                            .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(College::getCollegeId)
            );
            if (colleges != null && !colleges.isEmpty()) {
                List<Long> collegeIds = colleges.stream()
                        .map(College::getCollegeId)
                        .collect(Collectors.toList());
                wrapper.in(Major::getCollegeId, collegeIds);
            } else {
                wrapper.eq(Major::getMajorId, -1L);
            }
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Major::getCreateTime);
        
        List<Major> majors = this.list(wrapper);
        
        // 填充学院名称和学校名称
        if (majors != null && !majors.isEmpty()) {
            List<Long> collegeIds = majors.stream()
                    .map(Major::getCollegeId)
                    .filter(java.util.Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            if (!collegeIds.isEmpty()) {
                List<College> colleges = collegeMapper.selectBatchIds(collegeIds);
                if (colleges != null && !colleges.isEmpty()) {
                    java.util.Map<Long, College> collegeMap = colleges.stream()
                            .filter(c -> c != null && c.getCollegeId() != null)
                            .collect(Collectors.toMap(College::getCollegeId, c -> c, (v1, v2) -> v1));
                    
                    // 获取学校ID列表
                    List<Long> schoolIds = colleges.stream()
                            .map(College::getSchoolId)
                            .filter(java.util.Objects::nonNull)
                            .distinct()
                            .collect(Collectors.toList());
                    
                    // 查询学校信息
                    if (!schoolIds.isEmpty() && schoolMapper != null) {
                        List<com.server.internshipserver.domain.system.School> schools = schoolMapper.selectBatchIds(schoolIds);
                        if (schools != null && !schools.isEmpty()) {
                            java.util.Map<Long, String> schoolNameMap = schools.stream()
                                    .filter(s -> s != null && s.getSchoolId() != null && s.getSchoolName() != null)
                                    .collect(Collectors.toMap(
                                            com.server.internshipserver.domain.system.School::getSchoolId,
                                            com.server.internshipserver.domain.system.School::getSchoolName,
                                            (v1, v2) -> v1
                                    ));
                            
                            for (Major major : majors) {
                                if (major.getCollegeId() != null) {
                                    College college = collegeMap.get(major.getCollegeId());
                                    if (college != null && college.getSchoolId() != null) {
                                        major.setSchoolName(schoolNameMap.get(college.getSchoolId()));
                                    }
                                }
                            }
                        }
                    }
                    
                    for (Major major : majors) {
                        if (major.getCollegeId() != null) {
                            College college = collegeMap.get(major.getCollegeId());
                            if (college != null) {
                                major.setCollegeName(college.getCollegeName());
                                // schoolName将在Controller层通过SchoolService填充
                            }
                        }
                    }
                }
            }
        }
        
        return majors;
    }
}

