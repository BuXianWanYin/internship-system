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
import com.server.internshipserver.domain.evaluation.EnterpriseEvaluation;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.EnterpriseEvaluationMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.evaluation.ComprehensiveScoreService;
import com.server.internshipserver.service.evaluation.EnterpriseEvaluationService;
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
 * 企业评价管理Service实现类
 */
@Service
public class EnterpriseEvaluationServiceImpl extends ServiceImpl<EnterpriseEvaluationMapper, EnterpriseEvaluation> implements EnterpriseEvaluationService {
    
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
    public EnterpriseEvaluation saveOrUpdateEvaluation(EnterpriseEvaluation evaluation) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(evaluation.getApplyId(), "申请ID");
        
        // 验证申请是否存在且实习已结束
        InternshipApply apply = internshipApplyMapper.selectById(evaluation.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 如果企业ID为空，从申请中自动获取
        if (evaluation.getEnterpriseId() == null) {
            if (apply.getEnterpriseId() == null) {
                throw new BusinessException("申请中缺少企业ID信息，无法进行评价");
            }
            evaluation.setEnterpriseId(apply.getEnterpriseId());
        }
        
        // 验证实习状态为"实习结束"
        // 合作企业：status=7，自主实习：status=13
        if (!isInternshipCompleted(apply)) {
            throw new BusinessException("只能评价实习已结束的学生");
        }
        
        // 验证企业ID匹配
        if (!apply.getEnterpriseId().equals(evaluation.getEnterpriseId())) {
            throw new BusinessException("无权评价该学生的实习");
        }
        
        // 获取当前登录用户
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        
        // 权限检查：只有企业管理员或企业导师可以评价
        Enterprise enterprise = enterpriseMapper.selectById(evaluation.getEnterpriseId());
        EntityValidationUtil.validateEntityExists(enterprise, "企业");
        
        // 使用 getCurrentUserEnterpriseId() 方法检查当前用户是否是企业管理员或企业导师
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId == null || !currentUserEnterpriseId.equals(evaluation.getEnterpriseId())) {
            throw new BusinessException("只有企业管理员或企业导师可以评价");
        }
        
        // 验证评分范围
        validateScore(evaluation.getWorkAttitudeScore(), "工作态度");
        validateScore(evaluation.getKnowledgeApplicationScore(), "专业知识应用");
        validateScore(evaluation.getProfessionalSkillScore(), "专业技能");
        validateScore(evaluation.getTeamworkScore(), "团队协作");
        validateScore(evaluation.getInnovationScore(), "创新意识");
        
        // 自动计算日志周报质量分数（如果未填写）
        if (evaluation.getLogWeeklyReportScore() == null) {
            BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
            evaluation.setLogWeeklyReportScoreAuto(autoScore);
            // 如果自动计算有值，则使用自动计算值；否则需要手动填写
            if (autoScore != null) {
                evaluation.setLogWeeklyReportScore(autoScore);
            }
        } else {
            // 如果已填写，也计算自动值作为参考
            BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
            evaluation.setLogWeeklyReportScoreAuto(autoScore);
        }
        
        // 验证日志周报质量评分范围（如果已填写）
        if (evaluation.getLogWeeklyReportScore() != null) {
            validateScore(evaluation.getLogWeeklyReportScore(), "日志周报质量");
        }
        
        // 计算总分（6项指标的平均分）
        BigDecimal totalScore = calculateTotalScore(
                evaluation.getWorkAttitudeScore(),
                evaluation.getKnowledgeApplicationScore(),
                evaluation.getProfessionalSkillScore(),
                evaluation.getTeamworkScore(),
                evaluation.getInnovationScore(),
                evaluation.getLogWeeklyReportScore()
        );
        evaluation.setTotalScore(totalScore);
        
        // 获取学生信息
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student != null) {
            evaluation.setStudentId(student.getStudentId());
        }
        
        // 检查是否已存在评价（草稿或已提交）
        LambdaQueryWrapper<EnterpriseEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEvaluation::getApplyId, evaluation.getApplyId())
               .eq(EnterpriseEvaluation::getEnterpriseId, evaluation.getEnterpriseId())
               .eq(EnterpriseEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        EnterpriseEvaluation existEvaluation = this.getOne(wrapper);
        
        if (existEvaluation != null) {
            // 如果已提交，不允许修改
            if (existEvaluation.getEvaluationStatus() != null && 
                existEvaluation.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode())) {
                throw new BusinessException("评价已提交，不允许修改");
            }
            // 更新现有评价
            evaluation.setEvaluationId(existEvaluation.getEvaluationId());
            evaluation.setEvaluatorId(user.getUserId());
            evaluation.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            // 如果是草稿，不更新提交时间
            if (evaluation.getEvaluationStatus() == null) {
                evaluation.setEvaluationStatus(EvaluationStatus.DRAFT.getCode());
            }
            this.updateById(evaluation);
        } else {
            // 新增评价
            evaluation.setEvaluatorId(user.getUserId());
            evaluation.setEvaluationStatus(EvaluationStatus.DRAFT.getCode());
            EntityDefaultValueUtil.setDefaultValues(evaluation);
            this.save(evaluation);
        }
        
        return evaluation;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitEvaluation(Long evaluationId) {
        EntityValidationUtil.validateIdNotNull(evaluationId, "评价ID");
        
        EnterpriseEvaluation evaluation = this.getById(evaluationId);
        EntityValidationUtil.validateEntityExists(evaluation, "评价");
        
        // 验证所有评分都已填写
        if (evaluation.getWorkAttitudeScore() == null ||
            evaluation.getKnowledgeApplicationScore() == null ||
            evaluation.getProfessionalSkillScore() == null ||
            evaluation.getTeamworkScore() == null ||
            evaluation.getInnovationScore() == null ||
            evaluation.getLogWeeklyReportScore() == null) {
            throw new BusinessException("请填写所有评价指标");
        }
        
        // 更新评价状态为已提交
        evaluation.setEvaluationStatus(EvaluationStatus.SUBMITTED.getCode());
        evaluation.setSubmitTime(LocalDateTime.now());
        
        boolean updated = this.updateById(evaluation);
        
        // 提交成功后，检查是否所有评价都完成，如果完成则自动触发综合成绩计算
        if (updated) {
            try {
                if (comprehensiveScoreService.checkAllEvaluationsCompleted(evaluation.getApplyId())) {
                    comprehensiveScoreService.calculateComprehensiveScore(evaluation.getApplyId());
                }
            } catch (Exception e) {
                // 自动计算失败不影响提交，只记录日志
                // 可以后续手动触发计算
            }
        }
        
        return updated;
    }
    
    @Override
    public EnterpriseEvaluation getEvaluationById(Long evaluationId) {
        EntityValidationUtil.validateIdNotNull(evaluationId, "评价ID");
        
        EnterpriseEvaluation evaluation = this.getById(evaluationId);
        EntityValidationUtil.validateEntityExists(evaluation, "评价");
        
        // 自动计算日志周报质量分数（如果还没有自动计算值）
        if (evaluation.getLogWeeklyReportScoreAuto() == null && evaluation.getApplyId() != null) {
            BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
            evaluation.setLogWeeklyReportScoreAuto(autoScore);
        }
        
        // 填充关联字段
        fillEvaluationRelatedFields(evaluation);
        
        return evaluation;
    }
    
    @Override
    public EnterpriseEvaluation getEvaluationByApplyId(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        LambdaQueryWrapper<EnterpriseEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEvaluation::getApplyId, applyId)
               .eq(EnterpriseEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(EnterpriseEvaluation::getCreateTime)
               .last("LIMIT 1");
        
        EnterpriseEvaluation evaluation = this.getOne(wrapper);
        if (evaluation != null) {
            // 自动计算日志周报质量分数（如果还没有自动计算值）
            if (evaluation.getLogWeeklyReportScoreAuto() == null && evaluation.getApplyId() != null) {
                BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
                evaluation.setLogWeeklyReportScoreAuto(autoScore);
            }
            fillEvaluationRelatedFields(evaluation);
        }
        
        return evaluation;
    }
    
    @Override
    public Page<EnterpriseEvaluation> getEvaluationPage(Page<EnterpriseEvaluation> page, Long enterpriseId, 
                                                         String studentName, Integer evaluationStatus) {
        LambdaQueryWrapper<EnterpriseEvaluation> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, EnterpriseEvaluation::getDeleteFlag);
        
        // 数据权限过滤（企业管理员和企业导师只能查看本企业的评价）
        applyDataPermissionFilter(wrapper);
        
        // 过滤实习状态：只显示实习已结束（status=7）的学生评价
        filterByInternshipStatus(wrapper, enterpriseId);
        
        // 条件查询
        if (enterpriseId != null) {
            wrapper.eq(EnterpriseEvaluation::getEnterpriseId, enterpriseId);
        }
        if (evaluationStatus != null) {
            wrapper.eq(EnterpriseEvaluation::getEvaluationStatus, evaluationStatus);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(EnterpriseEvaluation::getCreateTime);
        
        Page<EnterpriseEvaluation> result = this.page(page, wrapper);
        
        // 填充关联字段并过滤学生姓名
        if (EntityValidationUtil.hasRecords(result)) {
            result.getRecords().removeIf(evaluation -> {
                fillEvaluationRelatedFields(evaluation);
                return shouldFilterByStudentName(evaluation, studentName);
            });
        }
        
        return result;
    }
    
    /**
     * 过滤实习状态：只显示实习已结束的学生评价
     * 合作企业：status=7，自主实习：status=13
     */
    private void filterByInternshipStatus(LambdaQueryWrapper<EnterpriseEvaluation> wrapper, Long enterpriseId) {
        // 查询实习已结束的申请ID列表（包括已评价）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        // 合作企业：status=7（实习结束）或 status=8（已评价），自主实习：status=13（实习结束）
        applyWrapper.and(w -> w.eq(InternshipApply::getStatus, InternshipApplyStatus.COMPLETED.getCode())
                              .or()
                              .eq(InternshipApply::getStatus, InternshipApplyStatus.EVALUATED.getCode())
                              .or()
                              .eq(InternshipApply::getStatus, SelfInternshipApplyStatus.COMPLETED.getCode()))
                    .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                    .select(InternshipApply::getApplyId);
        
        // 如果指定了企业ID，只查询该企业的申请
        if (enterpriseId != null) {
            applyWrapper.eq(InternshipApply::getEnterpriseId, enterpriseId);
        } else {
            // 如果没有指定企业ID，但已经通过数据权限过滤了企业，需要进一步过滤
            Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (currentUserEnterpriseId != null && !dataPermissionUtil.isSystemAdmin()) {
                applyWrapper.eq(InternshipApply::getEnterpriseId, currentUserEnterpriseId);
            }
        }
        
        List<InternshipApply> applies = internshipApplyMapper.selectList(applyWrapper);
        
        if (applies == null || applies.isEmpty()) {
            // 如果没有符合条件的申请，返回空结果
            wrapper.eq(EnterpriseEvaluation::getEvaluationId, -1L);
            return;
        }
        
        List<Long> applyIds = applies.stream()
                .map(InternshipApply::getApplyId)
                .collect(Collectors.toList());
        
        // 只查询这些申请ID对应的评价
        wrapper.in(EnterpriseEvaluation::getApplyId, applyIds);
    }
    
    /**
     * 数据权限过滤（企业管理员和企业导师只能查看本企业的评价）
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<EnterpriseEvaluation> wrapper) {
        if (dataPermissionUtil.isSystemAdmin()) {
            return;
        }
        
        // 使用 getCurrentUserEnterpriseId() 方法，它已经支持企业管理员和企业导师
        Long currentUserEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
        if (currentUserEnterpriseId != null) {
            wrapper.eq(EnterpriseEvaluation::getEnterpriseId, currentUserEnterpriseId);
        } else {
            // 如果不是企业管理员或企业导师，返回空结果
            wrapper.eq(EnterpriseEvaluation::getEvaluationId, -1L);
        }
    }
    
    /**
     * 判断是否应该根据学生姓名过滤
     * @return true表示应该过滤掉，false表示保留
     */
    private boolean shouldFilterByStudentName(EnterpriseEvaluation evaluation, String studentName) {
        if (!StringUtils.hasText(studentName)) {
            return false;
        }
        
        return evaluation.getStudentName() == null || !evaluation.getStudentName().contains(studentName);
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
     * 计算总分（6项指标的平均分）
     */
    private BigDecimal calculateTotalScore(BigDecimal score1, BigDecimal score2, BigDecimal score3, 
                                           BigDecimal score4, BigDecimal score5, BigDecimal score6) {
        if (score6 == null) {
            throw new BusinessException("日志周报质量评分不能为空");
        }
        BigDecimal sum = score1.add(score2).add(score3).add(score4).add(score5).add(score6);
        return sum.divide(new BigDecimal("6"), 2, RoundingMode.HALF_UP);
    }
    
    /**
     * 计算日志周报质量建议分数（日志平均分×50% + 周报平均分×50%）
     * @param applyId 申请ID
     * @return 建议分数，如果没有日志或周报则返回null
     */
    private BigDecimal calculateLogWeeklyReportScore(Long applyId) {
        // 查询已批阅的日志
        LambdaQueryWrapper<InternshipLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(InternshipLog::getApplyId, applyId)
                  .eq(InternshipLog::getReviewStatus, ReviewStatus.APPROVED.getCode())
                  .isNotNull(InternshipLog::getReviewScore)
                  .eq(InternshipLog::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<InternshipLog> logs = internshipLogMapper.selectList(logWrapper);
        
        // 查询已批阅的周报
        LambdaQueryWrapper<InternshipWeeklyReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.eq(InternshipWeeklyReport::getApplyId, applyId)
                     .eq(InternshipWeeklyReport::getReviewStatus, ReviewStatus.APPROVED.getCode())
                     .isNotNull(InternshipWeeklyReport::getReviewScore)
                     .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<InternshipWeeklyReport> reports = internshipWeeklyReportMapper.selectList(reportWrapper);
        
        BigDecimal logAvgScore = null;
        BigDecimal reportAvgScore = null;
        
        // 计算日志平均分
        if (logs != null && !logs.isEmpty()) {
            BigDecimal logSum = BigDecimal.ZERO;
            for (InternshipLog log : logs) {
                if (log.getReviewScore() != null) {
                    logSum = logSum.add(log.getReviewScore());
                }
            }
            logAvgScore = logSum.divide(new BigDecimal(logs.size()), 2, RoundingMode.HALF_UP);
        }
        
        // 计算周报平均分
        if (reports != null && !reports.isEmpty()) {
            BigDecimal reportSum = BigDecimal.ZERO;
            for (InternshipWeeklyReport report : reports) {
                if (report.getReviewScore() != null) {
                    reportSum = reportSum.add(report.getReviewScore());
                }
            }
            reportAvgScore = reportSum.divide(new BigDecimal(reports.size()), 2, RoundingMode.HALF_UP);
        }
        
        // 计算建议分数（日志平均分×50% + 周报平均分×50%）
        if (logAvgScore != null && reportAvgScore != null) {
            BigDecimal logPart = logAvgScore.multiply(new BigDecimal("0.5"));
            BigDecimal reportPart = reportAvgScore.multiply(new BigDecimal("0.5"));
            return logPart.add(reportPart).setScale(2, RoundingMode.HALF_UP);
        } else if (logAvgScore != null) {
            return logAvgScore;
        } else if (reportAvgScore != null) {
            return reportAvgScore;
        }
        
        return null;
    }
    
    /**
     * 填充评价关联字段
     */
    private void fillEvaluationRelatedFields(EnterpriseEvaluation evaluation) {
        fillStudentInfo(evaluation);
        fillEnterpriseInfo(evaluation);
        fillEvaluatorInfo(evaluation);
        fillInternshipDateInfo(evaluation);
    }
    
    /**
     * 填充学生信息
     */
    private void fillStudentInfo(EnterpriseEvaluation evaluation) {
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
    private void fillEnterpriseInfo(EnterpriseEvaluation evaluation) {
        if (evaluation.getEnterpriseId() == null) {
            return;
        }
        
        Enterprise enterprise = enterpriseMapper.selectById(evaluation.getEnterpriseId());
        if (enterprise == null) {
            return;
        }
        
        evaluation.setEnterpriseName(enterprise.getEnterpriseName());
    }
    
    /**
     * 填充评价人信息
     */
    private void fillEvaluatorInfo(EnterpriseEvaluation evaluation) {
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
     * 填充实习时间信息
     */
    private void fillInternshipDateInfo(EnterpriseEvaluation evaluation) {
        if (evaluation.getApplyId() == null) {
            return;
        }
        
        InternshipApply apply = internshipApplyMapper.selectById(evaluation.getApplyId());
        if (apply == null) {
            return;
        }
        
        evaluation.setInternshipStartDate(apply.getInternshipStartDate());
        evaluation.setInternshipEndDate(apply.getInternshipEndDate());
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


