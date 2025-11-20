package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.dto.StudentImportDTO;
import com.server.internshipserver.domain.user.dto.StudentImportResult;

import java.util.List;

/**
 * 学生管理Service接口
 */
public interface StudentService extends IService<Student> {
    
    /**
     * 根据用户ID查询学生信息
     * @param userId 用户ID
     * @return 学生信息
     */
    Student getStudentByUserId(Long userId);
    
    /**
     * 根据学号查询学生信息
     * @param studentNo 学号
     * @return 学生信息
     */
    Student getStudentByStudentNo(String studentNo);
    
    /**
     * 添加学生
     * @param student 学生信息
     * @return 添加的学生信息
     */
    Student addStudent(Student student);
    
    /**
     * 更新学生信息
     * @param student 学生信息
     * @return 更新后的学生信息
     */
    Student updateStudent(Student student);
    
    /**
     * 根据ID查询学生详情
     * @param studentId 学生ID
     * @return 学生信息
     */
    Student getStudentById(Long studentId);
    
    /**
     * 分页查询学生列表
     * @param page 分页参数
     * @param studentNo 学号（可选）
     * @param classId 班级ID（可选）
     * @param majorId 专业ID（可选）
     * @param collegeId 学院ID（可选）
     * @param schoolId 学校ID（可选）
     * @return 学生列表
     */
    Page<Student> getStudentPage(Page<Student> page, String studentNo, Long classId, 
                                  Long majorId, Long collegeId, Long schoolId);
    
    /**
     * 停用学生（软删除）
     * @param studentId 学生ID
     * @return 是否成功
     */
    boolean deleteStudent(Long studentId);
    
    /**
     * Excel批量导入学生（包含文件验证和解析）
     * @param file Excel文件
     * @param classId 班级ID（可选，如果Excel中未指定班级ID则使用此值）
     * @return 导入结果
     */
    StudentImportResult importStudentsFromFile(
            org.springframework.web.multipart.MultipartFile file, 
            Long classId);
    
    /**
     * Excel批量导入学生（仅处理已解析的数据）
     * @param importList 导入数据列表
     * @param classId 班级ID（如果Excel中未指定，使用此值）
     * @return 导入结果
     */
    StudentImportResult batchImportStudents(
            List<StudentImportDTO> importList, 
            Long classId);
    
    /**
     * 学生自主注册（使用分享码）
     * @param studentImportDTO 学生注册信息
     * @param shareCode 班级分享码
     * @return 注册的学生信息
     */
    Student registerStudentWithShareCode(
            StudentImportDTO studentImportDTO, 
            String shareCode);
    
    /**
     * 分页查询待审核学生列表（班主任审核）
     * @param page 分页参数
     * @param studentNo 学号（可选）
     * @param realName 姓名（可选）
     * @return 待审核学生列表
     */
    Page<Student> getPendingApprovalStudentPage(Page<Student> page, String studentNo, String realName);
    
    /**
     * 审核学生注册申请
     * @param studentId 学生ID
     * @param approved 是否通过：true-通过，false-拒绝
     * @param auditOpinion 审核意见（可选）
     * @return 是否成功
     */
    boolean approveStudentRegistration(Long studentId, boolean approved, String auditOpinion);
}

