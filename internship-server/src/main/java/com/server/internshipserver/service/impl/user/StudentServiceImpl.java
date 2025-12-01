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
import com.server.internshipserver.common.utils.ExcelUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.domain.user.dto.StudentImportDTO;
import com.server.internshipserver.domain.user.dto.StudentImportResult;
import com.server.internshipserver.domain.user.dto.StudentQueryDTO;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.service.system.ClassService;
import com.server.internshipserver.service.system.CollegeService;
import com.server.internshipserver.service.system.MajorService;
import com.server.internshipserver.service.user.StudentService;
import com.server.internshipserver.service.user.UserService;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 学生管理Service实现类
 */
@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private ClassService classService;
    
    @Autowired
    private MajorService majorService;
    
    @Autowired
    private CollegeService collegeService;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    
    @Override
    public Student getStudentByUserId(Long userId) {
        // 允许返回null，不使用验证工具
        if (userId == null) {
            return null;
        }
        
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Student::getUserId, userId)
               .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        return this.getOne(wrapper);
    }
    
    @Override
    public Student getStudentByStudentNo(String studentNo) {
        if (!StringUtils.hasText(studentNo)) {
            return null;
        }
        
        LambdaQueryWrapper<Student> wrapper = QueryWrapperUtil.buildNotDeletedWrapper(Student::getDeleteFlag);
        wrapper.eq(Student::getStudentNo, studentNo);
        return this.getOne(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student addStudent(Student student) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(student.getUserId(), "用户ID");
        EntityValidationUtil.validateStringNotBlank(student.getStudentNo(), "学号");
        EntityValidationUtil.validateIdNotNull(student.getClassId(), "班级ID");
        if (student.getEnrollmentYear() == null) {
            throw new BusinessException("入学年份不能为空");
        }
        
        // 数据权限检查：班主任只能添加到自己管理的班级（支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            if (!currentUserClassIds.contains(student.getClassId())) {
                throw new BusinessException("无权限添加学生到该班级");
            }
        }
        
        // 检查学号是否已存在
        Student existStudent = getStudentByStudentNo(student.getStudentNo());
        if (existStudent != null) {
            throw new BusinessException("学号已存在");
        }
        
        // 检查用户ID是否已被使用
        Student existUserStudent = getStudentByUserId(student.getUserId());
        if (existUserStudent != null) {
            throw new BusinessException("该用户已经是学生");
        }
        
        // 设置默认值
        EntityDefaultValueUtil.setDefaultValuesWithEnabledStatus(student);
        
        // 保存
        this.save(student);
        return student;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student updateStudent(Student student) {
        EntityValidationUtil.validateIdNotNull(student.getStudentId(), "学生ID");
        
        // 检查学生是否存在
        Student existStudent = this.getById(student.getStudentId());
        EntityValidationUtil.validateEntityExists(existStudent, "学生");
        
        // 数据权限检查：班主任只能修改管理的班级的学生（支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            // 检查原班级是否在管理范围内
            if (!currentUserClassIds.contains(existStudent.getClassId())) {
                throw new BusinessException("无权限修改该学生信息");
            }
            // 如果修改了班级，检查新班级是否也在管理范围内
            if (student.getClassId() != null && !student.getClassId().equals(existStudent.getClassId())) {
                if (!currentUserClassIds.contains(student.getClassId())) {
                    throw new BusinessException("无权限将学生转移到该班级");
                }
            }
        }
        
        // 如果修改了学号，检查新学号是否已存在
        if (StringUtils.hasText(student.getStudentNo()) 
                && !student.getStudentNo().equals(existStudent.getStudentNo())) {
            Student studentNoExist = getStudentByStudentNo(student.getStudentNo());
            if (studentNoExist != null) {
                throw new BusinessException("学号已存在");
            }
        }
        
        // 更新
        this.updateById(student);
        return this.getById(student.getStudentId());
    }
    
    @Override
    public Student getStudentById(Long studentId) {
        EntityValidationUtil.validateIdNotNull(studentId, "学生ID");
        
        Student student = this.getById(studentId);
        EntityValidationUtil.validateEntityExists(student, "学生");
        
        // 数据权限检查：班主任只能查看管理的班级的学生（支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            if (!currentUserClassIds.contains(student.getClassId())) {
                throw new BusinessException("无权限查看该学生信息");
            }
        }
        
        return student;
    }
    
    @Override
    public Page<Student> getStudentPage(Page<Student> page, StudentQueryDTO queryDTO) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Student::getDeleteFlag);
        
        // 条件查询
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getStudentNo())) {
                wrapper.like(Student::getStudentNo, queryDTO.getStudentNo());
            }
            if (queryDTO.getClassId() != null) {
                wrapper.eq(Student::getClassId, queryDTO.getClassId());
            }
            if (queryDTO.getMajorId() != null) {
                wrapper.eq(Student::getMajorId, queryDTO.getMajorId());
            }
            if (queryDTO.getCollegeId() != null) {
                wrapper.eq(Student::getCollegeId, queryDTO.getCollegeId());
            }
            if (queryDTO.getSchoolId() != null) {
                wrapper.eq(Student::getSchoolId, queryDTO.getSchoolId());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(Student::getStatus, queryDTO.getStatus());
            }
            if (queryDTO.getEnrollmentYear() != null) {
                wrapper.eq(Student::getEnrollmentYear, queryDTO.getEnrollmentYear());
            }
        }
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            // 班主任：只能查看管理的班级的学生（支持多班级）
            wrapper.in(Student::getClassId, currentUserClassIds);
        } else if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的学生
            wrapper.eq(Student::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的学生
            wrapper.eq(Student::getSchoolId, currentUserSchoolId);
        } else if (currentUserId != null && !dataPermissionUtil.isSystemAdmin()) {
            // 学生：只能查看自己的信息
            if (dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
                wrapper.eq(Student::getUserId, currentUserId);
            }
        }
        // 系统管理员不添加限制
        
        // 按创建时间倒序
        wrapper.orderByDesc(Student::getCreateTime);
        
        Page<Student> result = this.page(page, wrapper);
        
        // 填充企业信息（如果有当前实习企业）
        if (EntityValidationUtil.hasRecords(result)) {
            for (Student student : result.getRecords()) {
                String enterpriseName = null;
                
                // 优先从企业表获取企业名称（合作企业）
                if (student.getCurrentEnterpriseId() != null) {
                    Enterprise enterprise = enterpriseMapper.selectById(student.getCurrentEnterpriseId());
                    if (enterprise != null) {
                        enterpriseName = enterprise.getEnterpriseName();
                    }
                }
                
                // 如果企业表中没有找到，或者当前企业ID为null，尝试从申请表中获取
                if (enterpriseName == null && student.getCurrentApplyId() != null) {
                    InternshipApply apply = internshipApplyMapper.selectById(student.getCurrentApplyId());
                    if (apply != null) {
                        if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                            // 自主实习，使用自主实习企业名称
                            enterpriseName = apply.getSelfEnterpriseName();
                        } else if (apply.getEnterpriseId() != null) {
                            // 合作企业，如果企业表中没找到，再尝试从申请关联的企业获取
                            Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                            if (enterprise != null) {
                                enterpriseName = enterprise.getEnterpriseName();
                            }
                        }
                    }
                }
                
                // 如果还是没有找到，尝试查询学生最新的有效申请
                if (enterpriseName == null && student.getStudentId() != null) {
                    LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
                    applyWrapper.eq(InternshipApply::getStudentId, student.getStudentId())
                               .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                               .in(InternshipApply::getStatus, Arrays.asList(3, 7, 8, 11, 13)) // 已录用、实习结束、已评价、实习中、实习结束
                               .orderByDesc(InternshipApply::getCreateTime)
                               .last("LIMIT 1");
                    InternshipApply latestApply = internshipApplyMapper.selectOne(applyWrapper);
                    if (latestApply != null) {
                        if (latestApply.getApplyType() != null && latestApply.getApplyType().equals(ApplyType.SELF.getCode())) {
                            enterpriseName = latestApply.getSelfEnterpriseName();
                        } else if (latestApply.getEnterpriseId() != null) {
                            Enterprise enterprise = enterpriseMapper.selectById(latestApply.getEnterpriseId());
                            if (enterprise != null) {
                                enterpriseName = enterprise.getEnterpriseName();
                            }
                        }
                    }
                }
                
                student.setCurrentEnterpriseName(enterpriseName);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteStudent(Long studentId) {
        EntityValidationUtil.validateIdNotNull(studentId, "学生ID");
        
        Student student = this.getById(studentId);
        EntityValidationUtil.validateEntityExists(student, "学生");
        
        // 数据权限检查：班主任只能删除管理的班级的学生（支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            if (!currentUserClassIds.contains(student.getClassId())) {
                throw new BusinessException("无权限删除该学生");
            }
        }
        
        // 软删除
        student.setDeleteFlag(DeleteFlag.DELETED.getCode());
        return this.updateById(student);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentImportResult importStudentsFromFile(MultipartFile file, Long classId) {
        // 验证文件
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择要上传的Excel文件");
        }
        
        String fileName = file.getOriginalFilename();
        if (fileName == null || (!fileName.endsWith(".xlsx") && !fileName.endsWith(".xls"))) {
            throw new BusinessException("文件格式错误，请上传Excel文件（.xlsx或.xls）");
        }
        
        // 验证班级ID必须提供
        if (classId == null) {
            throw new BusinessException("请选择学生班级");
        }
        
        // 解析Excel
        List<StudentImportDTO> importList;
        try {
            importList = ExcelUtil.parseStudentImportExcel(file);
        } catch (Exception e) {
            throw new BusinessException("文件读取失败：" + e.getMessage());
        }
        
        if (importList.isEmpty()) {
            throw new BusinessException("Excel文件中没有有效数据");
        }
        
        // 批量导入
        return batchImportStudents(importList, classId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public StudentImportResult batchImportStudents(List<StudentImportDTO> importList, Long classId) {
        StudentImportResult result = new StudentImportResult();
        result.setTotalCount(importList.size());
        result.setSuccessCount(0);
        result.setFailCount(0);
        List<StudentImportDTO> failList = new ArrayList<>();
        List<StudentImportDTO> successList = new ArrayList<>();
        
        for (StudentImportDTO dto : importList) {
            try {
                // 参数校验
                if (!validateImportDto(dto, classId, failList)) {
                    continue;
                }
                
                // 获取班级ID并验证
                Long finalClassId = getFinalClassId(dto, classId);
                Class classInfo = validateClass(finalClassId, dto, failList);
                if (classInfo == null) {
                    continue;
                }
                
                // 检查学号和用户名是否已存在
                if (!validateStudentNoAndUsername(dto, failList)) {
                    continue;
                }
                
                // 创建用户和学生记录
                createStudentFromDto(dto, finalClassId, classInfo);
                
                // 添加到成功列表
                StudentImportDTO successDto = new StudentImportDTO();
                successDto.setRowNum(dto.getRowNum());
                successDto.setStudentNo(dto.getStudentNo());
                successDto.setRealName(dto.getRealName());
                successList.add(successDto);
                
                result.setSuccessCount(result.getSuccessCount() + 1);
            } catch (Exception e) {
                dto.setErrorMessage(e.getMessage());
                failList.add(dto);
            }
        }
        
        result.setFailCount(failList.size());
        result.setFailList(failList);
        result.setSuccessList(successList);
        return result;
    }
    
    /**
     * 验证导入DTO参数
     */
    private boolean validateImportDto(StudentImportDTO dto, Long classId, List<StudentImportDTO> failList) {
        if (!StringUtils.hasText(dto.getStudentNo())) {
            dto.setErrorMessage("学号不能为空");
            failList.add(dto);
            return false;
        }
        if (!StringUtils.hasText(dto.getRealName())) {
            dto.setErrorMessage("姓名不能为空");
            failList.add(dto);
            return false;
        }
        if (dto.getEnrollmentYear() == null) {
            dto.setErrorMessage("入学年份不能为空");
            failList.add(dto);
            return false;
        }
        if (!StringUtils.hasText(dto.getPassword())) {
            dto.setErrorMessage("初始密码不能为空");
            failList.add(dto);
            return false;
        }
        return true;
    }
    
    /**
     * 获取最终的班级ID（导入时强制使用传入的classId，不再从Excel读取）
     */
    private Long getFinalClassId(StudentImportDTO dto, Long classId) {
        // 导入时强制使用传入的classId，不再从Excel读取班级ID
        return classId;
    }
    
    /**
     * 验证班级是否存在
     */
    private Class validateClass(Long finalClassId, StudentImportDTO dto, List<StudentImportDTO> failList) {
        if (finalClassId == null) {
            dto.setErrorMessage("班级ID不能为空");
            failList.add(dto);
            return null;
        }
        
        try {
            Class classInfo = classService.getClassById(finalClassId);
            
            // 数据权限检查：班主任只能导入到自己管理的班级（支持多班级）
            List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
            if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
                if (!currentUserClassIds.contains(finalClassId)) {
                    dto.setErrorMessage("无权限导入学生到该班级");
                    failList.add(dto);
                    return null;
                }
            }
            
            return classInfo;
        } catch (Exception e) {
            dto.setErrorMessage("班级不存在");
            failList.add(dto);
            return null;
        }
    }
    
    /**
     * 验证学号和用户名是否已存在
     */
    private boolean validateStudentNoAndUsername(StudentImportDTO dto, List<StudentImportDTO> failList) {
        // 检查学号是否已存在
        Student existStudent = getStudentByStudentNo(dto.getStudentNo());
        if (existStudent != null) {
            dto.setErrorMessage("学号已存在");
            failList.add(dto);
            return false;
        }
        
        // 检查用户名（学号）是否已存在
        String username = dto.getStudentNo();
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            dto.setErrorMessage("用户名（学号）已存在");
            failList.add(dto);
            return false;
        }
        
        return true;
    }
    
    /**
     * 从DTO创建学生记录
     */
    private void createStudentFromDto(StudentImportDTO dto, Long finalClassId, Class classInfo) {
        // 创建用户
        UserInfo user = createUserFromDto(dto);
        user = userService.addUser(user);
        
        // 获取组织架构信息
        Long collegeId = getCollegeId(classInfo);
        Long schoolId = getSchoolId(collegeId);
        
        // 创建学生记录
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setStudentNo(dto.getStudentNo());
        student.setClassId(finalClassId);
        student.setEnrollmentYear(dto.getEnrollmentYear());
        // 设置冗余字段
        student.setMajorId(classInfo.getMajorId());
        student.setCollegeId(collegeId);
        student.setSchoolId(schoolId);
        student.setStatus(UserStatus.ENABLED.getCode()); // 导入的学生不需要审核，直接启用
        this.addStudent(student);
    }
    
    /**
     * 从DTO创建用户对象
     */
    private UserInfo createUserFromDto(StudentImportDTO dto) {
        String username = dto.getStudentNo();
        // 密码必须填写，已在validateImportDto中验证
        String password = dto.getPassword();
        
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(password); // UserService会自动加密
        user.setRealName(dto.getRealName());
        user.setIdCard(dto.getIdCard());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setStatus(UserStatus.ENABLED.getCode()); // 导入的学生不需要审核，直接启用
        return user;
    }
    
    /**
     * 获取学院ID
     */
    private Long getCollegeId(Class classInfo) {
        Major major = majorService.getById(classInfo.getMajorId());
        return major != null ? major.getCollegeId() : null;
    }
    
    /**
     * 获取学校ID
     */
    private Long getSchoolId(Long collegeId) {
        if (collegeId == null) {
            return null;
        }
        College college = collegeService.getById(collegeId);
        return college != null ? college.getSchoolId() : null;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Student registerStudentWithShareCode(StudentImportDTO studentImportDTO, String shareCode) {
        // 验证分享码
        Class classInfo = classService.validateShareCode(shareCode);
        if (classInfo == null) {
            throw new BusinessException("分享码无效或已过期");
        }
        
        // 参数校验
        if (!StringUtils.hasText(studentImportDTO.getStudentNo())) {
            throw new BusinessException("学号不能为空");
        }
        if (!StringUtils.hasText(studentImportDTO.getRealName())) {
            throw new BusinessException("姓名不能为空");
        }
        if (studentImportDTO.getEnrollmentYear() == null) {
            throw new BusinessException("入学年份不能为空");
        }
        
        // 检查学号是否已存在
        Student existStudent = getStudentByStudentNo(studentImportDTO.getStudentNo());
        if (existStudent != null) {
            throw new BusinessException("学号已存在");
        }
        
        // 生成用户名（使用学号）
        String username = studentImportDTO.getStudentNo();
        UserInfo existUser = userService.getUserByUsername(username);
        if (existUser != null) {
            throw new BusinessException("用户名（学号）已存在");
        }
        
        // 生成初始密码（如果用户提供了密码则使用，否则使用默认密码）
        String password = StringUtils.hasText(studentImportDTO.getPassword()) 
                ? studentImportDTO.getPassword() 
                : Constants.DEFAULT_PASSWORD;
        
        // 创建用户
        UserInfo user = new UserInfo();
        user.setUsername(username);
        user.setPassword(password); // UserService会自动加密
        user.setRealName(studentImportDTO.getRealName());
        user.setIdCard(studentImportDTO.getIdCard());
        user.setPhone(studentImportDTO.getPhone());
        user.setEmail(studentImportDTO.getEmail());
        user.setStatus(UserStatus.DISABLED.getCode()); // 待审核状态
        user = userService.addUser(user);
        
        // 获取专业信息以获取collegeId
        Major major = majorService.getById(classInfo.getMajorId());
        Long collegeId = null;
        Long schoolId = null;
        if (major != null) {
            collegeId = major.getCollegeId();
            // 获取学院信息以获取schoolId
            College college = collegeService.getById(collegeId);
            if (college != null) {
                schoolId = college.getSchoolId();
            }
        }
        
        // 创建学生记录
        Student student = new Student();
        student.setUserId(user.getUserId());
        student.setStudentNo(studentImportDTO.getStudentNo());
        student.setClassId(classInfo.getClassId());
        student.setEnrollmentYear(studentImportDTO.getEnrollmentYear());
        // 设置冗余字段
        student.setMajorId(classInfo.getMajorId());
        student.setCollegeId(collegeId);
        student.setSchoolId(schoolId);
        student.setStatus(UserStatus.DISABLED.getCode()); // 待审核状态
        student = this.addStudent(student);
        
        // 增加分享码使用次数
        classService.incrementShareCodeUseCount(shareCode);
        
        return student;
    }
    
    
    @Override
    public Page<Student> getPendingApprovalStudentPage(Page<Student> page, String studentNo, String realName) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Student::getDeleteFlag);
        
        // 只查询待审核的学生（status=DISABLED，禁用状态）
        wrapper.eq(Student::getStatus, UserStatus.DISABLED.getCode());
        
        // 条件查询
        if (StringUtils.hasText(studentNo)) {
            wrapper.like(Student::getStudentNo, studentNo);
        }
        if (StringUtils.hasText(realName)) {
            // 需要通过用户表关联查询真实姓名
            // 先查询符合条件的用户ID列表
            List<UserInfo> users = userService.list(
                    new LambdaQueryWrapper<UserInfo>()
                            .like(UserInfo::getRealName, realName)
                            .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .eq(UserInfo::getStatus, UserStatus.DISABLED.getCode()) // 待审核状态
            );
            if (users != null && !users.isEmpty()) {
                List<Long> userIds = new ArrayList<>();
                for (UserInfo user : users) {
                    userIds.add(user.getUserId());
                }
                wrapper.in(Student::getUserId, userIds);
            } else {
                // 如果没有匹配的用户，返回空结果
                wrapper.eq(Student::getStudentId, -1L);
            }
        }
        
        // 数据权限过滤：班主任只能查看管理的班级的学生（支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            // 班主任：只能查看管理的班级的学生（支持多班级）
            wrapper.in(Student::getClassId, currentUserClassIds);
        } else {
            // 系统管理员、学校管理员、学院负责人可以查看所有待审核学生
            // 不添加额外的过滤条件
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(Student::getCreateTime);
        
        return this.page(page, wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean approveStudentRegistration(Long studentId, boolean approved, String auditOpinion) {
        EntityValidationUtil.validateIdNotNull(studentId, "学生ID");
        
        // 查询学生信息
        Student student = this.getById(studentId);
        EntityValidationUtil.validateEntityExists(student, "学生");
        
        // 检查学生状态是否为待审核
        if (!student.getStatus().equals(UserStatus.DISABLED.getCode())) {
            throw new BusinessException("该学生不是待审核状态");
        }
        
        // 数据权限检查：班主任只能审核管理的班级的学生（支持多班级）
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            if (!currentUserClassIds.contains(student.getClassId())) {
                throw new BusinessException("无权限审核该学生");
            }
        }
        
        // 查询用户信息
        UserInfo user = userService.getUserById(student.getUserId());
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        if (approved) {
            // 审核通过：激活用户账号
            user.setStatus(UserStatus.ENABLED.getCode());
            student.setStatus(UserStatus.ENABLED.getCode());
            
            // 权限检查：检查当前用户是否可以分配学生角色
            if (!dataPermissionUtil.canAssignRole(Constants.ROLE_STUDENT)) {
                throw new BusinessException("无权限分配学生角色");
            }
            
            // 分配学生角色
            userService.assignRoleToUser(user.getUserId(), Constants.ROLE_STUDENT);
        } else {
            // 审核拒绝：保持禁用状态（status=DISABLED），或者可以软删除
            // 这里选择保持status=DISABLED，表示审核被拒绝
            // 如果需要，可以添加一个拒绝原因字段
            user.setStatus(UserStatus.DISABLED.getCode());
            student.setStatus(UserStatus.DISABLED.getCode());
        }
        
        // 更新用户和学生状态
        userService.updateUser(user);
        this.updateById(student);
        
        return true;
    }
    
    @Override
    public List<Student> getAllStudents(StudentQueryDTO queryDTO) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, Student::getDeleteFlag);
        
        // 条件查询
        if (queryDTO != null) {
            if (StringUtils.hasText(queryDTO.getStudentNo())) {
                wrapper.like(Student::getStudentNo, queryDTO.getStudentNo());
            }
            if (queryDTO.getClassId() != null) {
                wrapper.eq(Student::getClassId, queryDTO.getClassId());
            }
            if (queryDTO.getMajorId() != null) {
                wrapper.eq(Student::getMajorId, queryDTO.getMajorId());
            }
            if (queryDTO.getCollegeId() != null) {
                wrapper.eq(Student::getCollegeId, queryDTO.getCollegeId());
            }
            if (queryDTO.getSchoolId() != null) {
                wrapper.eq(Student::getSchoolId, queryDTO.getSchoolId());
            }
            if (queryDTO.getStatus() != null) {
                wrapper.eq(Student::getStatus, queryDTO.getStatus());
            }
            if (queryDTO.getEnrollmentYear() != null) {
                wrapper.eq(Student::getEnrollmentYear, queryDTO.getEnrollmentYear());
            }
        }
        
        // 数据权限过滤：根据用户角色自动添加查询条件
        List<Long> currentUserClassIds = dataPermissionUtil.getCurrentUserClassIds();
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        Long currentUserId = dataPermissionUtil.getCurrentUserId();
        
        if (currentUserClassIds != null && !currentUserClassIds.isEmpty()) {
            // 班主任：只能查看管理的班级的学生（支持多班级）
            wrapper.in(Student::getClassId, currentUserClassIds);
        } else if (currentUserCollegeId != null) {
            // 学院负责人：只能查看本院的学生
            wrapper.eq(Student::getCollegeId, currentUserCollegeId);
        } else if (currentUserSchoolId != null) {
            // 学校管理员：只能查看本校的学生
            wrapper.eq(Student::getSchoolId, currentUserSchoolId);
        } else if (currentUserId != null && !dataPermissionUtil.isSystemAdmin()) {
            // 学生：只能查看自己的信息
            if (dataPermissionUtil.hasRole(Constants.ROLE_STUDENT)) {
                wrapper.eq(Student::getUserId, currentUserId);
            }
        }
        // 系统管理员不添加限制
        
        // 按创建时间倒序
        wrapper.orderByDesc(Student::getCreateTime);
        
        List<Student> students = this.list(wrapper);
        
        // 填充关联信息
        if (students != null && !students.isEmpty()) {
            for (Student student : students) {
                // 填充用户信息
                if (student.getUserId() != null) {
                    UserInfo user = userService.getById(student.getUserId());
                    if (user != null) {
                        student.setRealName(user.getRealName());
                        student.setPhone(user.getPhone());
                        student.setEmail(user.getEmail());
                    }
                }
                
                // 填充班级信息
                if (student.getClassId() != null) {
                    Class classInfo = classService.getById(student.getClassId());
                    if (classInfo != null) {
                        student.setClassName(classInfo.getClassName());
                    }
                }
                
                // 填充专业信息
                if (student.getMajorId() != null) {
                    Major major = majorService.getById(student.getMajorId());
                    if (major != null) {
                        student.setMajorName(major.getMajorName());
                    }
                }
                
                // 填充学院信息
                if (student.getCollegeId() != null) {
                    College college = collegeService.getById(student.getCollegeId());
                    if (college != null) {
                        student.setCollegeName(college.getCollegeName());
                    }
                }
                
                // 填充企业信息
                if (student.getCurrentEnterpriseId() != null) {
                    Enterprise enterprise = enterpriseMapper.selectById(student.getCurrentEnterpriseId());
                    if (enterprise != null) {
                        student.setCurrentEnterpriseName(enterprise.getEnterpriseName());
                    }
                }
                
                // 转换状态文字
                if (student.getStatus() != null) {
                    student.setStatusText(student.getStatus() == 1 ? "已审核" : "待审核");
                } else {
                    student.setStatusText("");
                }
                
                // 转换创建时间
                if (student.getCreateTime() != null) {
                    student.setCreateTimeText(student.getCreateTime().format(
                        java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } else {
                    student.setCreateTimeText("");
                }
            }
        }
        
        return students;
    }
    
    @Override
    public List<Integer> getDistinctEnrollmentYears() {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        wrapper.select(Student::getEnrollmentYear)
               .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .isNotNull(Student::getEnrollmentYear)
               .groupBy(Student::getEnrollmentYear)
               .orderByDesc(Student::getEnrollmentYear);
        
        List<Student> students = this.list(wrapper);
        return students.stream()
                       .map(Student::getEnrollmentYear)
                       .distinct()
                       .sorted((a, b) -> b.compareTo(a)) // 降序排列
                       .collect(java.util.stream.Collectors.toList());
    }
}

