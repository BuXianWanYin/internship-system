package com.server.internshipserver.service.impl.system;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UniquenessValidationUtil;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.service.system.CollegeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学院管理Service实现类
 */
@Service
public class CollegeServiceImpl extends ServiceImpl<CollegeMapper, College> implements CollegeService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Autowired
    private com.server.internshipserver.mapper.system.SchoolMapper schoolMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public College addCollege(College college) {
        // 参数校验
        EntityValidationUtil.validateStringNotBlank(college.getCollegeName(), "学院名称");
        EntityValidationUtil.validateStringNotBlank(college.getCollegeCode(), "学院代码");
        EntityValidationUtil.validateIdNotNull(college.getSchoolId(), "所属学校ID");
        
        // 检查学院代码在同一学校内是否已存在
        UniquenessValidationUtil.validateUniqueInScope(this, College::getCollegeCode, college.getCollegeCode(),
                College::getSchoolId, college.getSchoolId(), College::getDeleteFlag, "学院代码", "学校");
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(college);
        
        // 如果指定了院长用户ID，填充院长姓名
        if (college.getDeanUserId() != null) {
            UserInfo deanUser = userMapper.selectById(college.getDeanUserId());
            if (deanUser != null) {
                college.setDeanName(deanUser.getRealName());
            }
        }
        
        // 保存
        this.save(college);
        return college;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public College updateCollege(College college) {
        if (college.getCollegeId() == null) {
            throw new BusinessException("学院ID不能为空");
        }
        
        // 检查学院是否存在
        College existCollege = this.getById(college.getCollegeId());
        EntityValidationUtil.validateEntityExists(existCollege, "学院");
        
        // 如果修改了学院代码，检查新代码在同一学校内是否已存在
        if (StringUtils.hasText(college.getCollegeCode()) 
                && !college.getCollegeCode().equals(existCollege.getCollegeCode())) {
            LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(College::getCollegeCode, college.getCollegeCode())
                   .eq(College::getSchoolId, existCollege.getSchoolId())
                   .ne(College::getCollegeId, college.getCollegeId())
                   .eq(College::getDeleteFlag, DeleteFlag.NORMAL.getCode());
            College codeExistCollege = this.getOne(wrapper);
            if (codeExistCollege != null) {
                throw new BusinessException("该学校下学院代码已存在");
            }
        }
        
        // 如果指定了院长用户ID，验证该用户是否为该学校的教师，并填充院长姓名
        if (college.getDeanUserId() != null) {
            // 验证该用户是否为该学校的教师
            Teacher teacher = teacherMapper.selectOne(
                    new LambdaQueryWrapper<Teacher>()
                            .eq(Teacher::getUserId, college.getDeanUserId())
                            .eq(Teacher::getSchoolId, existCollege.getSchoolId())
                            .eq(Teacher::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (teacher == null) {
                throw new BusinessException("指定的院长用户不是该学校的教师");
            }
            
            // 填充院长姓名
            UserInfo deanUser = userMapper.selectById(college.getDeanUserId());
            if (deanUser != null) {
                college.setDeanName(deanUser.getRealName());
            }
        } else {
            // 如果清空了院长用户ID，也清空院长姓名
            college.setDeanName(null);
        }
        
        // 更新
        this.updateById(college);
        return this.getById(college.getCollegeId());
    }
    
    @Override
    public College getCollegeById(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException("学院ID不能为空");
        }
        
        College college = this.getById(collegeId);
        EntityValidationUtil.validateEntityExists(college, "学院");
        
        return college;
    }
    
    @Override
    public Page<College> getCollegePage(Page<College> page, String collegeName, Long schoolId) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, College::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(collegeName)) {
            wrapper.like(College::getCollegeName, collegeName);
        }
        
        // 数据权限过滤：根据学校ID或班级ID过滤
        // 系统管理员：不添加过滤条件
        // 学校管理员：添加 school_id = 当前用户学校ID
        // 学院负责人：添加 college_id = 当前用户学院ID
        // 班主任：添加 college_id IN 当前用户管理的班级所在学院ID列表（通过班级->专业->学院关联，支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            // 班主任：只能查看管理的班级所在学院（支持多班级）
            // 先查询所有管理的班级信息
            List<Class> classList = classMapper.selectList(
                    new LambdaQueryWrapper<Class>()
                            .in(Class::getClassId, currentUserClassIds)
                            .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (classList != null && !classList.isEmpty()) {
                // 获取所有班级的专业ID列表
                List<Long> majorIds = classList.stream()
                        .map(Class::getMajorId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                if (!majorIds.isEmpty()) {
                    // 查询所有专业所属的学院ID列表
                    List<Major> majors = majorMapper.selectList(
                            new LambdaQueryWrapper<Major>()
                                    .in(Major::getMajorId, majorIds)
                                    .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                    .select(Major::getCollegeId)
                    );
                    if (majors != null && !majors.isEmpty()) {
                        List<Long> collegeIds = majors.stream()
                                .map(Major::getCollegeId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.toList());
                        if (!collegeIds.isEmpty()) {
                            // 过滤学院
                            wrapper.in(College::getCollegeId, collegeIds);
                        } else {
                            // 如果没有学院，返回空结果
                            wrapper.eq(College::getCollegeId, -1L);
                        }
                    } else {
                        // 如果没有专业或学院，返回空结果
                        wrapper.eq(College::getCollegeId, -1L);
                    }
                } else {
                    // 如果没有专业，返回空结果
                    wrapper.eq(College::getCollegeId, -1L);
                }
            } else {
                // 如果没有班级，返回空结果
                wrapper.eq(College::getCollegeId, -1L);
            }
        } else if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院
            wrapper.eq(College::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校
            wrapper.eq(College::getSchoolId, currentUserSchoolId);
        } else if (schoolId != null) {
            // 系统管理员可以指定学校ID查询
            wrapper.eq(College::getSchoolId, schoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(College::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCollege(Long collegeId) {
        if (collegeId == null) {
            throw new BusinessException("学院ID不能为空");
        }
        
        College college = this.getById(collegeId);
        EntityValidationUtil.validateEntityExists(college, "学院");
        
        // 软删除
        college.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(college);
    }
    
    @Override
    public List<College> getAllColleges(String collegeName, Long schoolId) {
        LambdaQueryWrapper<College> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, College::getDeleteFlag);
        
        // 条件查询
        if (StringUtils.hasText(collegeName)) {
            wrapper.like(College::getCollegeName, collegeName);
        }
        
        // 数据权限过滤（复用分页查询的逻辑）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            List<Class> classList = classMapper.selectList(
                    new LambdaQueryWrapper<Class>()
                            .in(Class::getClassId, currentUserClassIds)
                            .eq(Class::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (classList != null && !classList.isEmpty()) {
                List<Long> majorIds = classList.stream()
                        .map(Class::getMajorId)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());
                if (!majorIds.isEmpty()) {
                    List<Major> majors = majorMapper.selectList(
                            new LambdaQueryWrapper<Major>()
                                    .in(Major::getMajorId, majorIds)
                                    .eq(Major::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                    .select(Major::getCollegeId)
                    );
                    if (majors != null && !majors.isEmpty()) {
                        List<Long> collegeIds = majors.stream()
                                .map(Major::getCollegeId)
                                .filter(Objects::nonNull)
                                .distinct()
                                .collect(Collectors.toList());
                        if (!collegeIds.isEmpty()) {
                            wrapper.in(College::getCollegeId, collegeIds);
                        } else {
                            wrapper.eq(College::getCollegeId, -1L);
                        }
                    } else {
                        wrapper.eq(College::getCollegeId, -1L);
                    }
                } else {
                    wrapper.eq(College::getCollegeId, -1L);
                }
            } else {
                wrapper.eq(College::getCollegeId, -1L);
            }
        } else if (currentUserCollegeId != null) {
            wrapper.eq(College::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            wrapper.eq(College::getSchoolId, currentUserSchoolId);
        } else if (schoolId != null) {
            wrapper.eq(College::getSchoolId, schoolId);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(College::getCreateTime);
        
        List<College> colleges = this.list(wrapper);
        
        // 填充学校名称
        if (colleges != null && !colleges.isEmpty()) {
            List<Long> schoolIds = colleges.stream()
                    .map(College::getSchoolId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
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
                    for (College college : colleges) {
                        if (college.getSchoolId() != null) {
                            college.setSchoolName(schoolNameMap.get(college.getSchoolId()));
                        }
                    }
                }
            }
        }
        
        return colleges;
    }
}

