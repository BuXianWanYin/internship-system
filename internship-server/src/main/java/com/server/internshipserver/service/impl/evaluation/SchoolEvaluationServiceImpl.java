package com.server.internshipserver.service.impl.evaluation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.EvaluationStatus;
import com.server.internshipserver.common.enums.InternshipApplyStatus;
import com.server.internshipserver.common.enums.ReviewStatus;
import com.server.internshipserver.common.enums.SelfInternshipApplyStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;
import com.server.internshipserver.domain.evaluation.SchoolEvaluation;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.SchoolEvaluationMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.service.evaluation.ComprehensiveScoreService;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.evaluation.SchoolEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 学校评价管理Service实现类
 */
@Service
public class SchoolEvaluationServiceImpl extends ServiceImpl<SchoolEvaluationMapper, SchoolEvaluation> implements SchoolEvaluationService {
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private InternshipLogMapper internshipLogMapper;
    
    @Autowired
    private InternshipWeeklyReportMapper internshipWeeklyReportMapper;
    
    @Autowired
    private ComprehensiveScoreService comprehensiveScoreService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SchoolEvaluation saveOrUpdateEvaluation(SchoolEvaluation evaluation) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(evaluation.getApplyId(), "申请ID");
        
        // 验证申请是否存在且实习已结束
        InternshipApply apply = internshipApplyMapper.selectById(evaluation.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 验证实习状态为"实习结束"
        // 合作企业：status=7，自主实习：status=13
        if (!isInternshipCompleted(apply)) {
            throw new BusinessException("只能评价实习已结束的学生");
        }
        
        // 获取当前登录用户（教师）
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        
        // 自动计算日志周报质量建议分数（如果未填写，则使用自动计算值）
        BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
        if (evaluation.getLogWeeklyReportScore() == null && autoScore != null) {
            // 如果未填写且自动计算有值，则使用自动计算值
            evaluation.setLogWeeklyReportScore(autoScore);
        }
        
        // 验证评分范围
        validateScore(evaluation.getLogWeeklyReportScore(), "日志周报质量");
        validateScore(evaluation.getProcessPerformanceScore(), "过程表现");
        validateScore(evaluation.getAchievementScore(), "成果展示");
        validateScore(evaluation.getSummaryReflectionScore(), "总结反思");
        
        // 计算总分（4项指标的平均分）
        BigDecimal totalScore = calculateTotalScore(
                evaluation.getLogWeeklyReportScore(),
                evaluation.getProcessPerformanceScore(),
                evaluation.getAchievementScore(),
                evaluation.getSummaryReflectionScore()
        );
        evaluation.setTotalScore(totalScore);
        
        // 获取学生信息
        setStudentIdFromApply(evaluation, apply);
        
        // 保存或更新评价
        saveOrUpdate(evaluation, user);
        
        return evaluation;
    }
    
    /**
     * 从申请中设置学生ID
     */
    private void setStudentIdFromApply(SchoolEvaluation evaluation, InternshipApply apply) {
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student != null) {
            evaluation.setStudentId(student.getStudentId());
        }
        }
        
    /**
     * 保存或更新评价
     */
    private void saveOrUpdate(SchoolEvaluation evaluation, UserInfo user) {
        // 检查是否已存在评价（草稿或已提交）
        LambdaQueryWrapper<SchoolEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolEvaluation::getApplyId, evaluation.getApplyId())
               .eq(SchoolEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        SchoolEvaluation existEvaluation = this.getOne(wrapper);
        
        if (existEvaluation != null) {
            updateExistingEvaluation(evaluation, existEvaluation, user);
        } else {
            createNewEvaluation(evaluation, user);
        }
    }
    
    /**
     * 更新现有评价
     */
    private void updateExistingEvaluation(SchoolEvaluation evaluation, SchoolEvaluation existEvaluation, UserInfo user) {
            evaluation.setEvaluationId(existEvaluation.getEvaluationId());
            evaluation.setEvaluatorId(user.getUserId());
            evaluation.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            // 如果是草稿，不更新提交时间
            if (evaluation.getEvaluationStatus() == null) {
                evaluation.setEvaluationStatus(EvaluationStatus.DRAFT.getCode());
            }
            this.updateById(evaluation);
    }
    
    /**
     * 创建新评价
     */
    private void createNewEvaluation(SchoolEvaluation evaluation, UserInfo user) {
            evaluation.setEvaluatorId(user.getUserId());
            evaluation.setEvaluationStatus(EvaluationStatus.DRAFT.getCode());
            EntityDefaultValueUtil.setDefaultValues(evaluation);
            this.save(evaluation);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitEvaluation(Long evaluationId) {
        EntityValidationUtil.validateIdNotNull(evaluationId, "评价ID");
        
        SchoolEvaluation evaluation = this.getById(evaluationId);
        EntityValidationUtil.validateEntityExists(evaluation, "评价");
        
        // 验证所有评分都已填写
        if (evaluation.getLogWeeklyReportScore() == null ||
            evaluation.getProcessPerformanceScore() == null ||
            evaluation.getAchievementScore() == null ||
            evaluation.getSummaryReflectionScore() == null) {
            throw new BusinessException("请填写所有评价指标");
        }
        
        // 更新评价状态为已提交
        evaluation.setEvaluationStatus(EvaluationStatus.SUBMITTED.getCode());
        evaluation.setSubmitTime(LocalDateTime.now());
        
        return this.updateById(evaluation);
    }
    
    @Override
    public SchoolEvaluation getEvaluationById(Long evaluationId) {
        EntityValidationUtil.validateIdNotNull(evaluationId, "评价ID");
        
        SchoolEvaluation evaluation = this.getById(evaluationId);
        EntityValidationUtil.validateEntityExists(evaluation, "评价");
        
        enrichEvaluationData(evaluation);
        
        return evaluation;
    }
    
    @Override
    public SchoolEvaluation getEvaluationByApplyId(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        LambdaQueryWrapper<SchoolEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolEvaluation::getApplyId, applyId)
               .eq(SchoolEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(SchoolEvaluation::getCreateTime)
               .last("LIMIT 1");
        
        SchoolEvaluation evaluation = this.getOne(wrapper);
        if (evaluation != null) {
            enrichEvaluationData(evaluation);
        }
        
        return evaluation;
    }
    
    /**
     * 丰富评价数据（填充自动计算的分数、综合成绩和关联字段）
     */
    private void enrichEvaluationData(SchoolEvaluation evaluation) {
        if (evaluation.getApplyId() != null) {
            // 自动计算日志周报质量建议分数（用于前端显示参考）
                BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
                evaluation.setLogWeeklyReportScoreAuto(autoScore);
                
                // 填充综合成绩（如果已计算）
            fillComprehensiveScore(evaluation);
                }
        
        // 填充关联字段
            fillEvaluationRelatedFields(evaluation);
    }
    
    @Override
    public Page<SchoolEvaluation> getEvaluationPage(Page<SchoolEvaluation> page, String studentName, Integer evaluationStatus) {
        LambdaQueryWrapper<SchoolEvaluation> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, SchoolEvaluation::getDeleteFlag);
        
        // 数据权限过滤（教师只能查看权限范围内的学生）
        applyDataPermissionFilter(wrapper);
        
        // 条件查询
        if (evaluationStatus != null) {
            wrapper.eq(SchoolEvaluation::getEvaluationStatus, evaluationStatus);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(SchoolEvaluation::getCreateTime);
        
        Page<SchoolEvaluation> result = this.page(page, wrapper);
        
        // 填充关联字段并过滤学生姓名
        if (EntityValidationUtil.hasRecords(result)) {
            result.getRecords().removeIf(evaluation -> {
                fillEvaluationRelatedFields(evaluation);
                fillComprehensiveScore(evaluation);
                return shouldFilterByStudentName(evaluation, studentName);
            });
        }
        
        return result;
    }
    
    /**
     * 填充综合成绩
     */
    private void fillComprehensiveScore(SchoolEvaluation evaluation) {
        if (evaluation.getApplyId() == null) {
            return;
        }
        
        ComprehensiveScore comprehensiveScore = comprehensiveScoreService.getScoreByApplyId(evaluation.getApplyId());
        if (comprehensiveScore == null) {
            return;
        }
        
        evaluation.setComprehensiveScore(comprehensiveScore.getComprehensiveScore());
        evaluation.setGradeLevel(comprehensiveScore.getGradeLevel());
    }
    
    /**
     * 判断是否应该根据学生姓名过滤
     * @return true表示应该过滤掉，false表示保留
     */
    private boolean shouldFilterByStudentName(SchoolEvaluation evaluation, String studentName) {
        if (!StringUtils.hasText(studentName)) {
            return false;
        }
        
        return evaluation.getStudentName() == null || !evaluation.getStudentName().contains(studentName);
    }
    
    /**
     * 计算日志周报质量建议分数（日志平均分×50% + 周报平均分×50%）
     * @param applyId 申请ID
     * @return 建议分数，如果没有日志或周报则返回null
     */
    public BigDecimal calculateLogWeeklyReportScore(Long applyId) {
        BigDecimal logAvgScore = calculateLogAverageScore(applyId);
        BigDecimal reportAvgScore = calculateReportAverageScore(applyId);
        
        return calculateCombinedScore(logAvgScore, reportAvgScore);
    }
    
    /**
     * 计算日志平均分
     */
    private BigDecimal calculateLogAverageScore(Long applyId) {
        LambdaQueryWrapper<InternshipLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(InternshipLog::getApplyId, applyId)
                  .eq(InternshipLog::getReviewStatus, ReviewStatus.APPROVED.getCode())
                  .isNotNull(InternshipLog::getReviewScore)
                  .eq(InternshipLog::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<InternshipLog> logs = internshipLogMapper.selectList(logWrapper);
        
        return calculateAverageScoreFromLogs(logs);
    }
    
    /**
     * 计算周报平均分
     */
    private BigDecimal calculateReportAverageScore(Long applyId) {
        LambdaQueryWrapper<InternshipWeeklyReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(InternshipWeeklyReport::getApplyId, applyId)
                     .eq(InternshipWeeklyReport::getReviewStatus, ReviewStatus.APPROVED.getCode())
                     .isNotNull(InternshipWeeklyReport::getReviewScore)
                     .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<InternshipWeeklyReport> reports = internshipWeeklyReportMapper.selectList(reportWrapper);
        
        return calculateAverageScoreFromReports(reports);
    }
    
    /**
     * 从日志列表计算平均分
     */
    private BigDecimal calculateAverageScoreFromLogs(List<InternshipLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return null;
        }
        
        BigDecimal sum = logs.stream()
                .map(InternshipLog::getReviewScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return null;
                }
        
        return sum.divide(new BigDecimal(logs.size()), 2, RoundingMode.HALF_UP);
        }
        
    /**
     * 从周报列表计算平均分
     */
    private BigDecimal calculateAverageScoreFromReports(List<InternshipWeeklyReport> reports) {
        if (reports == null || reports.isEmpty()) {
            return null;
        }
        
        BigDecimal sum = reports.stream()
                .map(InternshipWeeklyReport::getReviewScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (sum.compareTo(BigDecimal.ZERO) == 0) {
            return null;
                }
        
        return sum.divide(new BigDecimal(reports.size()), 2, RoundingMode.HALF_UP);
        }
        
    /**
     * 计算组合分数（日志平均分×50% + 周报平均分×50%）
     */
    private BigDecimal calculateCombinedScore(BigDecimal logAvgScore, BigDecimal reportAvgScore) {
        if (logAvgScore != null && reportAvgScore != null) {
            BigDecimal logPart = logAvgScore.multiply(new BigDecimal("0.5"));
            BigDecimal reportPart = reportAvgScore.multiply(new BigDecimal("0.5"));
            return logPart.add(reportPart).setScale(2, RoundingMode.HALF_UP);
        }
        
        if (logAvgScore != null) {
            return logAvgScore;
        }
        
        if (reportAvgScore != null) {
            return reportAvgScore;
        }
        
        return null;
    }
    
    /**
     * 验证评分范围
     */
    private void validateScore(BigDecimal score, String fieldName) {
        if (score == null) {
            throw new BusinessException(fieldName + "评分不能为空");
        }
        if (score.compareTo(new BigDecimal(Constants.SCORE_MIN)) < 0 || 
            score.compareTo(new BigDecimal(Constants.SCORE_MAX)) > 0) {
            throw new BusinessException(fieldName + "评分必须在" + Constants.SCORE_MIN + "-" + Constants.SCORE_MAX + "之间");
        }
    }
    
    /**
     * 计算总分（4项指标的平均分）
     */
    private BigDecimal calculateTotalScore(BigDecimal score1, BigDecimal score2, BigDecimal score3, BigDecimal score4) {
        BigDecimal sum = score1.add(score2).add(score3).add(score4);
        return sum.divide(new BigDecimal("4"), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<SchoolEvaluation> wrapper) {
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            setEmptyResult(wrapper);
            return;
        }
        
        // 根据角色过滤：教师只能查看权限范围内的学生评价
        List<Long> applyIds = getFilteredApplyIds();
        if (applyIds != null && !applyIds.isEmpty()) {
            wrapper.in(SchoolEvaluation::getApplyId, applyIds);
        } else {
            setEmptyResult(wrapper);
        }
    }
    
    /**
     * 根据用户权限获取可查看的申请ID列表
     */
    private List<Long> getFilteredApplyIds() {
        // 班主任：只能查看管理的班级的学生评价
        List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
        if (classIds != null && !classIds.isEmpty()) {
            return getApplyIdsByClassIds(classIds);
        }
        
        // 学院负责人：查询本学院的所有学生
        Long currentUserCollegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (currentUserCollegeId != null) {
            return getApplyIdsByCollegeId(currentUserCollegeId);
        }
        
        // 学校管理员：查询本校的所有学生
        Long currentUserSchoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (currentUserSchoolId != null) {
            return getApplyIdsBySchoolId(currentUserSchoolId);
        }
        
        // 其他角色或无权限，返回null
        return null;
    }
    
    /**
     * 根据班级ID列表获取申请ID列表
     */
    private List<Long> getApplyIdsByClassIds(List<Long> classIds) {
            List<Student> students = studentMapper.selectList(
                    new LambdaQueryWrapper<Student>()
                            .in(Student::getClassId, classIds)
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                            .select(Student::getStudentId)
            );
        
        if (students == null || students.isEmpty()) {
            return null;
        }
        
                List<Long> studentIds = students.stream()
                        .map(Student::getStudentId)
                        .collect(Collectors.toList());
                
        return getApplyIdsByStudentIds(studentIds);
    }
    
    /**
     * 根据学院ID获取申请ID列表
     */
    private List<Long> getApplyIdsByCollegeId(Long collegeId) {
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                        .eq(Student::getCollegeId, collegeId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getStudentId)
                );
        
        if (students == null || students.isEmpty()) {
            return null;
        }
        
                    List<Long> studentIds = students.stream()
                            .map(Student::getStudentId)
                            .collect(Collectors.toList());
                    
        return getApplyIdsByStudentIds(studentIds);
    }
    
    /**
     * 根据学校ID获取申请ID列表
     */
    private List<Long> getApplyIdsBySchoolId(Long schoolId) {
                List<Student> students = studentMapper.selectList(
                        new LambdaQueryWrapper<Student>()
                        .eq(Student::getSchoolId, schoolId)
                                .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                .select(Student::getStudentId)
                );
        
        if (students == null || students.isEmpty()) {
            return null;
        }
        
                    List<Long> studentIds = students.stream()
                            .map(Student::getStudentId)
                            .collect(Collectors.toList());
                    
        return getApplyIdsByStudentIds(studentIds);
    }
    
    /**
     * 根据学生ID列表获取实习结束状态的申请ID列表
     * 合作企业：status=7，自主实习：status=13
     */
    private List<Long> getApplyIdsByStudentIds(List<Long> studentIds) {
                    List<InternshipApply> applies = internshipApplyMapper.selectList(
                            new LambdaQueryWrapper<InternshipApply>()
                                    .in(InternshipApply::getStudentId, studentIds)
                        // 合作企业：status=7，自主实习：status=13
                        .and(w -> w.eq(InternshipApply::getStatus, InternshipApplyStatus.COMPLETED.getCode())
                                  .or()
                                  .eq(InternshipApply::getStatus, SelfInternshipApplyStatus.COMPLETED.getCode()))
                                    .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                                    .select(InternshipApply::getApplyId)
                    );
                    
        if (applies == null || applies.isEmpty()) {
            return null;
        }
        
        return applies.stream()
                                .map(InternshipApply::getApplyId)
                                .collect(Collectors.toList());
    }
    
    /**
     * 设置空结果查询条件
     */
    private void setEmptyResult(LambdaQueryWrapper<SchoolEvaluation> wrapper) {
                wrapper.eq(SchoolEvaluation::getEvaluationId, -1L);
    }
    
    /**
     * 填充评价关联字段
     */
    private void fillEvaluationRelatedFields(SchoolEvaluation evaluation) {
        fillStudentInfo(evaluation);
        fillEnterpriseInfo(evaluation);
        fillEvaluatorInfo(evaluation);
    }
    
    /**
     * 填充学生信息
     */
    private void fillStudentInfo(SchoolEvaluation evaluation) {
        if (evaluation.getStudentId() == null) {
            return;
        }
        
            Student student = studentMapper.selectById(evaluation.getStudentId());
        if (student == null) {
            return;
        }
        
                UserInfo studentUser = userMapper.selectById(student.getUserId());
        if (studentUser == null) {
            return;
        }
        
                    evaluation.setStudentName(studentUser.getRealName());
                    evaluation.setStudentNo(student.getStudentNo());
                }
    
    /**
     * 填充企业信息
     */
    private void fillEnterpriseInfo(SchoolEvaluation evaluation) {
        if (evaluation.getApplyId() == null) {
            return;
        }
        
            InternshipApply apply = internshipApplyMapper.selectById(evaluation.getApplyId());
        if (apply == null) {
            return;
        }
        
        // 合作企业申请，从企业表获取企业名称
                if (apply.getEnterpriseId() != null) {
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        evaluation.setEnterpriseName(enterprise.getEnterpriseName());
                    }
            return;
        }
        
                    // 自主实习，使用自主实习企业名称（学生申请时填写）
        if (apply.getApplyType() != null && apply.getApplyType().equals(ApplyType.SELF.getCode())) {
                    evaluation.setEnterpriseName(apply.getSelfEnterpriseName());
                }
            }
    
    /**
     * 填充评价人信息
     */
    private void fillEvaluatorInfo(SchoolEvaluation evaluation) {
        if (evaluation.getEvaluatorId() == null) {
            return;
        }
        
            UserInfo evaluator = userMapper.selectById(evaluation.getEvaluatorId());
        if (evaluator == null) {
            return;
        }
        
                evaluation.setEvaluatorName(evaluator.getRealName());
            }
    
    /**
     * 判断实习是否已结束
     * 合作企业：status=7，自主实习：status=13
     */
    private boolean isInternshipCompleted(InternshipApply apply) {
        if (apply == null || apply.getStatus() == null) {
            return false;
        }
        
        Integer status = apply.getStatus();
        Integer applyType = apply.getApplyType();
        
        // 合作企业：status=7
        if (applyType != null && applyType.equals(ApplyType.COOPERATION.getCode())) {
            return status.equals(InternshipApplyStatus.COMPLETED.getCode());
        }
        
        // 自主实习：status=13
        if (applyType != null && applyType.equals(ApplyType.SELF.getCode())) {
            return status.equals(SelfInternshipApplyStatus.COMPLETED.getCode());
        }
        
        return false;
    }
}

