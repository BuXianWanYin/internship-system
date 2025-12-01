package com.server.internshipserver.service.impl.evaluation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.EvaluationStatus;
import com.server.internshipserver.common.enums.InternshipApplyStatus;
import com.server.internshipserver.common.enums.SelfInternshipApplyStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.evaluation.SelfEvaluation;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.SelfEvaluationMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.evaluation.ComprehensiveScoreService;
import com.server.internshipserver.service.evaluation.SelfEvaluationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 学生自评管理Service实现类
 * 实现学生自评的创建、提交、查询等业务功能
 */
@Service
public class SelfEvaluationServiceImpl extends ServiceImpl<SelfEvaluationMapper, SelfEvaluation> implements SelfEvaluationService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private ComprehensiveScoreService comprehensiveScoreService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public SelfEvaluation saveOrUpdateEvaluation(SelfEvaluation evaluation) {
        // 参数校验
        EntityValidationUtil.validateIdNotNull(evaluation.getApplyId(), "申请ID");
        
        // 验证申请是否存在且实习已结束
        InternshipApply apply = internshipApplyMapper.selectById(evaluation.getApplyId());
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 验证实习状态：允许已录用（status=3）或已结束（status=7合作企业，status=13自主实习）
        Integer status = apply.getStatus();
        if (status == null) {
            throw new BusinessException("实习申请状态异常，无法填写自我评价");
        }
        
        Integer applyType = apply.getApplyType();
        boolean isCompleted = false;
        
        // 判断是否为实习结束状态
        if (applyType != null && applyType.equals(ApplyType.COOPERATION.getCode())) {
            // 合作企业：status=3（已录用）或 status=7（实习结束）
            isCompleted = status.equals(InternshipApplyStatus.COMPLETED.getCode());
            if (!status.equals(InternshipApplyStatus.ACCEPTED.getCode()) && !isCompleted) {
                throw new BusinessException("只能评价已录用或已结束的实习申请");
            }
        } else if (applyType != null && applyType.equals(ApplyType.SELF.getCode())) {
            // 自主实习：status=11（实习中）或 status=13（实习结束）
            isCompleted = status.equals(SelfInternshipApplyStatus.COMPLETED.getCode());
            if (!status.equals(SelfInternshipApplyStatus.IN_PROGRESS.getCode()) && !isCompleted) {
                throw new BusinessException("只能评价实习中或已结束的实习申请");
            }
        } else {
            throw new BusinessException("实习申请类型异常，无法填写自我评价");
        }
        
        // 验证实习是否已结束（实习结束日期已过或今天，或者状态为已结束）
        if (!isCompleted) {
            if (apply.getInternshipEndDate() == null) {
                throw new BusinessException("实习结束日期未设置，无法填写自我评价");
            }
            java.time.LocalDate today = java.time.LocalDate.now();
            if (apply.getInternshipEndDate().isAfter(today)) {
                throw new BusinessException("实习尚未结束，请在实习结束后填写自我评价");
            }
        }
        
        // 获取当前登录用户（学生）
        UserInfo user = UserUtil.getCurrentUser(userMapper);
        
        // 获取学生信息
        Student student = studentMapper.selectOne(
                new LambdaQueryWrapper<Student>()
                        .eq(Student::getUserId, user.getUserId())
                        .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
        );
        if (student == null) {
            throw new BusinessException("学生信息不存在");
        }
        
        // 验证申请是否属于当前学生
        if (!apply.getStudentId().equals(student.getStudentId())) {
            throw new BusinessException("无权评价该申请");
        }
        
        // 验证评分范围
        if (evaluation.getSelfScore() == null) {
            throw new BusinessException("自评分数不能为空");
        }
        if (evaluation.getSelfScore().compareTo(new BigDecimal(Constants.SCORE_MIN)) < 0 || 
            evaluation.getSelfScore().compareTo(new BigDecimal(Constants.SCORE_MAX)) > 0) {
            throw new BusinessException("自评分数必须在" + Constants.SCORE_MIN + "-" + Constants.SCORE_MAX + "之间");
        }
        
        // 设置学生ID
        evaluation.setStudentId(student.getStudentId());
        
        // 检查是否已存在自评（草稿或已提交）
        LambdaQueryWrapper<SelfEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelfEvaluation::getApplyId, evaluation.getApplyId())
               .eq(SelfEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        SelfEvaluation existEvaluation = this.getOne(wrapper);
        
        if (existEvaluation != null) {
            // 如果已提交，不允许修改
            if (existEvaluation.getEvaluationStatus() != null && 
                existEvaluation.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode())) {
                throw new BusinessException("评价已提交，不允许修改");
            }
            // 更新现有自评
            evaluation.setEvaluationId(existEvaluation.getEvaluationId());
            evaluation.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            // 如果是草稿，不更新提交时间
            if (evaluation.getEvaluationStatus() == null) {
                evaluation.setEvaluationStatus(EvaluationStatus.DRAFT.getCode());
            }
            this.updateById(evaluation);
        } else {
            // 新增自评
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
        
        SelfEvaluation evaluation = this.getById(evaluationId);
        EntityValidationUtil.validateEntityExists(evaluation, "评价");
        
        // 验证自评分数已填写
        if (evaluation.getSelfScore() == null) {
            throw new BusinessException("请填写自评分数");
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
    public SelfEvaluation getEvaluationById(Long evaluationId) {
        EntityValidationUtil.validateIdNotNull(evaluationId, "评价ID");
        
        SelfEvaluation evaluation = this.getById(evaluationId);
        EntityValidationUtil.validateEntityExists(evaluation, "评价");
        
        // 填充关联字段
        fillEvaluationRelatedFields(evaluation);
        
        return evaluation;
    }
    
    @Override
    public SelfEvaluation getEvaluationByApplyId(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        LambdaQueryWrapper<SelfEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelfEvaluation::getApplyId, applyId)
               .eq(SelfEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(SelfEvaluation::getCreateTime)
               .last("LIMIT 1");
        
        SelfEvaluation evaluation = this.getOne(wrapper);
        if (evaluation != null) {
            fillEvaluationRelatedFields(evaluation);
        }
        
        return evaluation;
    }
    
    @Override
    public Page<SelfEvaluation> getEvaluationPage(Page<SelfEvaluation> page, Long studentId, Integer evaluationStatus) {
        LambdaQueryWrapper<SelfEvaluation> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, SelfEvaluation::getDeleteFlag);
        
        // 条件查询
        if (studentId != null) {
            wrapper.eq(SelfEvaluation::getStudentId, studentId);
        }
        if (evaluationStatus != null) {
            wrapper.eq(SelfEvaluation::getEvaluationStatus, evaluationStatus);
        }
        
        // 按创建时间倒序
        wrapper.orderByDesc(SelfEvaluation::getCreateTime);
        
        Page<SelfEvaluation> result = this.page(page, wrapper);
        
        // 填充关联字段
        if (EntityValidationUtil.hasRecords(result)) {
            for (SelfEvaluation evaluation : result.getRecords()) {
                fillEvaluationRelatedFields(evaluation);
            }
        }
        
        return result;
    }
    
    /**
     * 填充评价关联字段
     */
    private void fillEvaluationRelatedFields(SelfEvaluation evaluation) {
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
                // TODO: 填充企业名称
            }
        }
    }
}

