package com.server.internshipserver.service.impl.evaluation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.server.internshipserver.common.constant.ConfigKeys;
import com.server.internshipserver.common.enums.ApplyType;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.EvaluationStatus;
import com.server.internshipserver.common.enums.InternshipApplyStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.EntityDefaultValueUtil;
import com.server.internshipserver.common.utils.EntityValidationUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.SystemConfigUtil;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;
import com.server.internshipserver.domain.evaluation.EnterpriseEvaluation;
import com.server.internshipserver.domain.evaluation.SchoolEvaluation;
import com.server.internshipserver.domain.evaluation.SelfEvaluation;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.ComprehensiveScoreMapper;
import com.server.internshipserver.mapper.evaluation.EnterpriseEvaluationMapper;
import com.server.internshipserver.mapper.evaluation.SchoolEvaluationMapper;
import com.server.internshipserver.mapper.evaluation.SelfEvaluationMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.service.evaluation.ComprehensiveScoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 综合成绩管理Service实现类
 */
@Service
public class ComprehensiveScoreServiceImpl extends ServiceImpl<ComprehensiveScoreMapper, ComprehensiveScore> implements ComprehensiveScoreService {
    
    @Autowired
    private EnterpriseEvaluationMapper enterpriseEvaluationMapper;
    
    @Autowired
    private SchoolEvaluationMapper schoolEvaluationMapper;
    
    @Autowired
    private SelfEvaluationMapper selfEvaluationMapper;
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ComprehensiveScore calculateComprehensiveScore(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        // 验证申请是否存在
        InternshipApply apply = internshipApplyMapper.selectById(applyId);
        EntityValidationUtil.validateEntityExists(apply, "申请");
        
        // 判断申请类型
        boolean isCooperation = apply.getApplyType() != null && 
                               apply.getApplyType().equals(ApplyType.COOPERATION.getCode());
        
        // 检查评价是否都完成（根据实习类型判断）
        if (!checkAllEvaluationsCompleted(applyId)) {
            if (isCooperation) {
                throw new BusinessException("企业评价、学校评价和自评都完成后才能计算综合成绩");
            } else {
                throw new BusinessException("学校评价和自评都完成后才能计算综合成绩");
            }
        }
        
        // 获取学校评价和自评（所有类型都需要）
        SchoolEvaluation schoolEval = getSchoolEvaluation(applyId);
        SelfEvaluation selfEval = getSelfEvaluation(applyId);
        
        if (schoolEval == null || selfEval == null) {
            throw new BusinessException("评价信息不完整，无法计算综合成绩");
        }
        
        // 验证评价状态为已提交
        if (!schoolEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode()) ||
            !selfEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode())) {
            throw new BusinessException("学校评价和自评都必须已提交才能计算综合成绩");
        }
        
        BigDecimal enterpriseScore = null;
        BigDecimal schoolScore = schoolEval.getTotalScore();
        BigDecimal selfScore = selfEval.getSelfScore();
        BigDecimal comprehensiveScore;
        
        if (isCooperation) {
            // 合作企业实习：企业评价×权重 + 学校评价×权重 + 自评×权重
            EnterpriseEvaluation enterpriseEval = getEnterpriseEvaluation(applyId);
            if (enterpriseEval == null) {
                throw new BusinessException("企业评价信息不完整，无法计算综合成绩");
            }
            if (!enterpriseEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode())) {
                throw new BusinessException("企业评价必须已提交才能计算综合成绩");
            }
            
            enterpriseScore = enterpriseEval.getTotalScore();
            
            // 从系统配置读取权重，默认值：企业40%、学校40%、自评20%
            BigDecimal enterpriseWeight = new BigDecimal(SystemConfigUtil.getConfigValue(ConfigKeys.ENTERPRISE_EVALUATION_WEIGHT, "0.4"));
            BigDecimal schoolWeight = new BigDecimal(SystemConfigUtil.getConfigValue(ConfigKeys.SCHOOL_EVALUATION_WEIGHT, "0.4"));
            BigDecimal selfWeight = new BigDecimal(SystemConfigUtil.getConfigValue(ConfigKeys.STUDENT_SELF_EVALUATION_WEIGHT, "0.2"));
            
            // 验证权重总和是否为1.0（允许0.01的误差）
            BigDecimal totalWeight = enterpriseWeight.add(schoolWeight).add(selfWeight);
            if (totalWeight.compareTo(new BigDecimal("1.0")) < 0 || totalWeight.compareTo(new BigDecimal("1.01")) > 0) {
                throw new BusinessException("评价权重配置错误，三个权重之和必须等于1.0");
            }
            
            // 计算综合成绩：企业评价×权重 + 学校评价×权重 + 自评×权重
            BigDecimal enterprisePart = enterpriseScore.multiply(enterpriseWeight);
            BigDecimal schoolPart = schoolScore.multiply(schoolWeight);
            BigDecimal selfPart = selfScore.multiply(selfWeight);
            comprehensiveScore = enterprisePart.add(schoolPart).add(selfPart).setScale(2, RoundingMode.HALF_UP);
        } else {
            // 自主实习：学校评价×60% + 自评×40%
            BigDecimal schoolWeight = new BigDecimal("0.6");
            BigDecimal selfWeight = new BigDecimal("0.4");
            
            BigDecimal schoolPart = schoolScore.multiply(schoolWeight);
            BigDecimal selfPart = selfScore.multiply(selfWeight);
            comprehensiveScore = schoolPart.add(selfPart).setScale(2, RoundingMode.HALF_UP);
        }
        
        // 计算等级
        String gradeLevel = calculateGradeLevel(comprehensiveScore);
        
        // 检查是否已存在综合成绩
        LambdaQueryWrapper<ComprehensiveScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComprehensiveScore::getApplyId, applyId)
               .eq(ComprehensiveScore::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        ComprehensiveScore existScore = this.getOne(wrapper);
        
        ComprehensiveScore score = new ComprehensiveScore();
        if (existScore != null) {
            score.setScoreId(existScore.getScoreId());
        }
        
        score.setApplyId(applyId);
        score.setStudentId(apply.getStudentId());
        score.setEnterpriseScore(enterpriseScore);
        score.setSchoolScore(schoolScore);
        score.setSelfScore(selfScore);
        score.setComprehensiveScore(comprehensiveScore);
        score.setGradeLevel(gradeLevel);
        score.setCalculateTime(LocalDateTime.now());
        
        if (existScore != null) {
            score.setDeleteFlag(DeleteFlag.NORMAL.getCode());
            this.updateById(score);
        } else {
            EntityDefaultValueUtil.setDefaultValues(score);
            this.save(score);
        }
        
        // 更新实习申请状态为"已评价"（status=8）
        apply.setStatus(InternshipApplyStatus.EVALUATED.getCode());
        internshipApplyMapper.updateById(apply);
        
        // 填充关联字段
        fillScoreRelatedFields(score);
        
        return score;
    }
    
    @Override
    public boolean checkAllEvaluationsCompleted(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        // 获取申请信息，判断申请类型
        InternshipApply apply = internshipApplyMapper.selectById(applyId);
        if (apply == null) {
            return false;
        }
        
        // 检查学校评价和自评（所有类型都需要）
        SchoolEvaluation schoolEval = getSchoolEvaluation(applyId);
        SelfEvaluation selfEval = getSelfEvaluation(applyId);
        
        if (schoolEval == null || selfEval == null) {
            return false;
        }
        
        // 判断申请类型
        boolean isCooperation = apply.getApplyType() != null && 
                               apply.getApplyType().equals(ApplyType.COOPERATION.getCode());
        
        if (isCooperation) {
            // 合作企业实习：需要企业评价、学校评价和自评
            EnterpriseEvaluation enterpriseEval = getEnterpriseEvaluation(applyId);
            return enterpriseEval != null && 
                   enterpriseEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode()) &&
                   schoolEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode()) &&
                   selfEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode());
        } else {
            // 自主实习：只需要学校评价和自评
            return schoolEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode()) &&
                   selfEval.getEvaluationStatus().equals(EvaluationStatus.SUBMITTED.getCode());
        }
    }
    
    @Override
    public ComprehensiveScore getScoreById(Long scoreId) {
        EntityValidationUtil.validateIdNotNull(scoreId, "成绩ID");
        
        ComprehensiveScore score = this.getById(scoreId);
        EntityValidationUtil.validateEntityExists(score, "综合成绩");
        
        // 填充关联字段
        fillScoreRelatedFields(score);
        
        return score;
    }
    
    @Override
    public ComprehensiveScore getScoreByApplyId(Long applyId) {
        EntityValidationUtil.validateIdNotNull(applyId, "申请ID");
        
        LambdaQueryWrapper<ComprehensiveScore> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ComprehensiveScore::getApplyId, applyId)
               .eq(ComprehensiveScore::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .orderByDesc(ComprehensiveScore::getCreateTime)
               .last("LIMIT 1");
        
        ComprehensiveScore score = this.getOne(wrapper);
        if (score != null) {
            fillScoreRelatedFields(score);
        }
        
        return score;
    }
    
    @Override
    public Page<ComprehensiveScore> getScorePage(Page<ComprehensiveScore> page, String studentName) {
        LambdaQueryWrapper<ComprehensiveScore> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, ComprehensiveScore::getDeleteFlag);
        
        // 按创建时间倒序
        wrapper.orderByDesc(ComprehensiveScore::getCreateTime);
        
        Page<ComprehensiveScore> result = this.page(page, wrapper);
        
        // 填充关联字段并过滤学生姓名
        if (EntityValidationUtil.hasRecords(result)) {
            for (ComprehensiveScore score : result.getRecords()) {
                fillScoreRelatedFields(score);
                // 如果提供了学生姓名，进行过滤
                if (studentName != null && !studentName.isEmpty() && 
                    (score.getStudentName() == null || 
                     !score.getStudentName().contains(studentName))) {
                    result.getRecords().remove(score);
                }
            }
        }
        
        return result;
    }
    
    /**
     * 获取企业评价
     */
    private EnterpriseEvaluation getEnterpriseEvaluation(Long applyId) {
        LambdaQueryWrapper<EnterpriseEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EnterpriseEvaluation::getApplyId, applyId)
               .eq(EnterpriseEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .eq(EnterpriseEvaluation::getEvaluationStatus, EvaluationStatus.SUBMITTED.getCode())
               .orderByDesc(EnterpriseEvaluation::getCreateTime)
               .last("LIMIT 1");
        return enterpriseEvaluationMapper.selectOne(wrapper);
    }
    
    /**
     * 获取学校评价
     */
    private SchoolEvaluation getSchoolEvaluation(Long applyId) {
        LambdaQueryWrapper<SchoolEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SchoolEvaluation::getApplyId, applyId)
               .eq(SchoolEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .eq(SchoolEvaluation::getEvaluationStatus, EvaluationStatus.SUBMITTED.getCode())
               .orderByDesc(SchoolEvaluation::getCreateTime)
               .last("LIMIT 1");
        return schoolEvaluationMapper.selectOne(wrapper);
    }
    
    /**
     * 获取学生自评
     */
    private SelfEvaluation getSelfEvaluation(Long applyId) {
        LambdaQueryWrapper<SelfEvaluation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SelfEvaluation::getApplyId, applyId)
               .eq(SelfEvaluation::getDeleteFlag, DeleteFlag.NORMAL.getCode())
               .eq(SelfEvaluation::getEvaluationStatus, EvaluationStatus.SUBMITTED.getCode())
               .orderByDesc(SelfEvaluation::getCreateTime)
               .last("LIMIT 1");
        return selfEvaluationMapper.selectOne(wrapper);
    }
    
    /**
     * 计算等级
     * 优秀：90-100分
     * 良好：80-89分
     * 中等：70-79分
     * 及格：60-69分
     * 不及格：0-59分
     */
    private String calculateGradeLevel(BigDecimal score) {
        int scoreValue = score.intValue();
        if (scoreValue >= 90) {
            return "优秀";
        } else if (scoreValue >= 80) {
            return "良好";
        } else if (scoreValue >= 70) {
            return "中等";
        } else if (scoreValue >= 60) {
            return "及格";
        } else {
            return "不及格";
        }
    }
    
    /**
     * 填充成绩关联字段
     */
    private void fillScoreRelatedFields(ComprehensiveScore score) {
        // 填充学生信息
        if (score.getStudentId() != null) {
            Student student = studentMapper.selectById(score.getStudentId());
            if (student != null) {
                UserInfo studentUser = userMapper.selectById(student.getUserId());
                if (studentUser != null) {
                    score.setStudentName(studentUser.getRealName());
                    score.setStudentNo(student.getStudentNo());
                }
            }
        }
        
        // 填充申请信息（企业名称、实习时间等）
        if (score.getApplyId() != null) {
            InternshipApply apply = internshipApplyMapper.selectById(score.getApplyId());
            if (apply != null) {
                // 填充申请类型
                score.setApplyType(apply.getApplyType());
                
                // 填充企业名称
                if (apply.getEnterpriseId() != null) {
                    Enterprise enterprise = enterpriseMapper.selectById(apply.getEnterpriseId());
                    if (enterprise != null) {
                        score.setEnterpriseName(enterprise.getEnterpriseName());
                    }
                } else if (apply.getSelfEnterpriseName() != null) {
                    // 自主实习的企业名称
                    score.setEnterpriseName(apply.getSelfEnterpriseName());
                }
                
                // 填充实习时间
                score.setInternshipStartDate(apply.getInternshipStartDate());
                score.setInternshipEndDate(apply.getInternshipEndDate());
            }
        }
    }
}

