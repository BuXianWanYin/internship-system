package com.server.internshipserver.service.impl.evaluation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.EvaluationStatus;
import com.server.internshipserver.common.enums.ReviewStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;
import com.server.internshipserver.domain.evaluation.SchoolEvaluation;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.SchoolEvaluationMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.service.evaluation.ComprehensiveScoreService;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
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
        
        // 验证实习状态为"实习结束"（status=7）
        if (apply.getStatus() == null || apply.getStatus() != 7) {
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
        Student student = studentMapper.selectById(apply.getStudentId());
        if (student != null) {
            evaluation.setStudentId(student.getStudentId());
        }
        
        // 检查是否已存在评价（草稿或已提交）
        LambdaQueryWrapper<SchoolEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolEvaluation::getApplyId, evaluation.getApplyId())
               .eq(SchoolEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        SchoolEvaluation existEvaluation = this.getOne(wrapper);
        
        if (existEvaluation != null) {
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
        
        // 自动计算日志周报质量建议分数（用于前端显示参考）
        if (evaluation.getApplyId() != null) {
            BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
            evaluation.setLogWeeklyReportScoreAuto(autoScore);
            
            // 填充综合成绩（如果已计算）
            ComprehensiveScore comprehensiveScore = comprehensiveScoreService.getScoreByApplyId(evaluation.getApplyId());
            if (comprehensiveScore != null) {
                evaluation.setComprehensiveScore(comprehensiveScore.getComprehensiveScore());
                evaluation.setGradeLevel(comprehensiveScore.getGradeLevel());
            }
        }
        
        // 填充关联字段
        fillEvaluationRelatedFields(evaluation);
        
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
            // 自动计算日志周报质量建议分数（用于前端显示参考）
            if (evaluation.getApplyId() != null) {
                BigDecimal autoScore = calculateLogWeeklyReportScore(evaluation.getApplyId());
                evaluation.setLogWeeklyReportScoreAuto(autoScore);
                
                // 填充综合成绩（如果已计算）
                ComprehensiveScore comprehensiveScore = comprehensiveScoreService.getScoreByApplyId(evaluation.getApplyId());
                if (comprehensiveScore != null) {
                    evaluation.setComprehensiveScore(comprehensiveScore.getComprehensiveScore());
                    evaluation.setGradeLevel(comprehensiveScore.getGradeLevel());
                }
            }
            fillEvaluationRelatedFields(evaluation);
        }
        
        return evaluation;
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
            for (SchoolEvaluation evaluation : result.getRecords()) {
                fillEvaluationRelatedFields(evaluation);
                
                // 填充综合成绩（如果已计算）
                if (evaluation.getApplyId() != null) {
                    ComprehensiveScore comprehensiveScore = comprehensiveScoreService.getScoreByApplyId(evaluation.getApplyId());
                    if (comprehensiveScore != null) {
                        evaluation.setComprehensiveScore(comprehensiveScore.getComprehensiveScore());
                        evaluation.setGradeLevel(comprehensiveScore.getGradeLevel());
                    }
                }
                
                // 如果提供了学生姓名，进行过滤
                if (StringUtils.hasText(studentName) && 
                    (evaluation.getStudentName() == null || 
                     !evaluation.getStudentName().contains(studentName))) {
                    result.getRecords().remove(evaluation);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 计算日志周报质量建议分数（日志平均分×50% + 周报平均分×50%）
     * @param applyId 申请ID
     * @return 建议分数，如果没有日志或周报则返回null
     */
    public BigDecimal calculateLogWeeklyReportScore(Long applyId) {
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
            wrapper.eq(SchoolEvaluation::getEvaluationId, -1L);
            return;
        }
        
        // 根据角色过滤：教师只能查看权限范围内的学生评价
        // 这里需要通过申请ID关联查询学生的组织信息
        // 简化处理：直接通过申请关联查询
        // TODO: 完善数据权限过滤逻辑
    }
    
    /**
     * 填充评价关联字段
     */
    private void fillEvaluationRelatedFields(SchoolEvaluation evaluation) {
        // 填充学生信息
        if (evaluation.getStudentId() != null) {
            Student student = studentMapper.selectById(evaluation.getStudentId());
            if (student != null) {
                UserInfo studentUser = userMapper.selectById(student.getUserId());
                if (studentUser != null) {
                    evaluation.setStudentName(studentUser.getRealName());
                    evaluation.setStudentNo(student.getStudentNo());
                }
            }
        }
        
        // 填充企业信息
        if (evaluation.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(evaluation.getApplyId());
            if (apply != null && apply.getEnterpriseId() != null) {
                // 通过apply获取企业名称
                // TODO: 填充企业名称
            }
        }
        
        // 填充评价人信息
        if (evaluation.getEvaluatorId() != null) {
            UserInfo evaluator = userMapper.selectById(evaluation.getEvaluatorId());
            if (evaluator != null) {
                evaluation.setEvaluatorName(evaluator.getRealName());
            }
        }
    }
}

