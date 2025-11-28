package com.server.internshipserver.service.impl.statistics;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.utils.DataPermissionUtil;
import com.server.internshipserver.common.utils.QueryWrapperUtil;
import com.server.internshipserver.common.utils.UserUtil;
import com.server.internshipserver.domain.evaluation.ComprehensiveScore;
import com.server.internshipserver.domain.internship.InternshipApply;
import com.server.internshipserver.domain.internship.InternshipPost;
import com.server.internshipserver.domain.statistics.dto.*;
import com.server.internshipserver.domain.internship.InternshipLog;
import com.server.internshipserver.domain.internship.InternshipWeeklyReport;
import com.server.internshipserver.domain.system.Class;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.ComprehensiveScoreMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.statistics.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 数据统计Service实现类
 */
@Service
public class StatisticsServiceImpl implements StatisticsService {
    
    @Autowired
    private InternshipApplyMapper internshipApplyMapper;
    
    @Autowired
    private ComprehensiveScoreMapper comprehensiveScoreMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Autowired
    private EnterpriseMapper enterpriseMapper;
    
    @Autowired
    private InternshipPostMapper internshipPostMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private ClassMapper classMapper;
    
    @Autowired
    private MajorMapper majorMapper;
    
    @Autowired
    private InternshipLogMapper internshipLogMapper;
    
    @Autowired
    private InternshipWeeklyReportMapper internshipWeeklyReportMapper;
    
    @Autowired
    private DataPermissionUtil dataPermissionUtil;
    
    // 配色方案
    private static final String COLOR_PRIMARY = "#409EFF";
    private static final String COLOR_SUCCESS = "#67C23A";
    private static final String COLOR_WARNING = "#F7BA2A";
    private static final String COLOR_INFO = "#909399";
    private static final String COLOR_PENDING = "#C0C4CC";
    
    @Override
    public InternshipProgressStatistics getInternshipProgressStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        
        // 查询所有实习申请
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        InternshipProgressStatistics statistics = new InternshipProgressStatistics();
        
        // 统计总数
        statistics.setTotalCount((long) applies.size());
        
        // 统计各状态人数
        long completedCount = applies.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 8) // 已评价
                .count();
        long inProgressCount = applies.stream()
                .filter(a -> a.getStatus() != null && a.getStatus() == 6) // 实习中
                .count();
        long pendingCount = applies.stream()
                .filter(a -> {
                    Integer status = a.getStatus();
                    return status != null && (status == 3 || status == 4 || status == 5); // 已录用、已拒绝录用、已取消
                })
                .count();
        
        statistics.setCompletedCount(completedCount);
        statistics.setInProgressCount(inProgressCount);
        statistics.setPendingCount(pendingCount);
        
        // 计算完成率
        if (statistics.getTotalCount() > 0) {
            BigDecimal completionRate = new BigDecimal(completedCount)
                    .divide(new BigDecimal(statistics.getTotalCount()), 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            statistics.setCompletionRate(completionRate);
        } else {
            statistics.setCompletionRate(BigDecimal.ZERO);
        }
        
        // 构建饼图数据
        InternshipProgressStatistics.ProgressPieChartData pieChartData = new InternshipProgressStatistics.ProgressPieChartData();
        pieChartData.setPending(new InternshipProgressStatistics.PieItem("待开始", pendingCount, COLOR_PENDING));
        pieChartData.setInProgress(new InternshipProgressStatistics.PieItem("进行中", inProgressCount, COLOR_PRIMARY));
        pieChartData.setCompleted(new InternshipProgressStatistics.PieItem("已完成", completedCount, COLOR_SUCCESS));
        statistics.setPieChartData(pieChartData);
        
        return statistics;
    }
    
    @Override
    public EvaluationScoreStatistics getEvaluationScoreStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<ComprehensiveScore> scoreWrapper = new LambdaQueryWrapper<>();
        QueryWrapperUtil.notDeleted(scoreWrapper, ComprehensiveScore::getDeleteFlag);
        
        // 通过applyId关联查询，应用权限过滤
        if (queryDTO != null) {
            LambdaQueryWrapper<InternshipApply> applyWrapper = buildApplyQueryWrapper(queryDTO);
            List<InternshipApply> applies = internshipApplyMapper.selectList(applyWrapper);
            if (applies.isEmpty()) {
                return createEmptyEvaluationScoreStatistics();
            }
            List<Long> applyIds = applies.stream().map(InternshipApply::getApplyId).collect(Collectors.toList());
            scoreWrapper.in(ComprehensiveScore::getApplyId, applyIds);
        }
        
        List<ComprehensiveScore> scores = comprehensiveScoreMapper.selectList(scoreWrapper);
        
        if (scores.isEmpty()) {
            return createEmptyEvaluationScoreStatistics();
        }
        
        EvaluationScoreStatistics statistics = new EvaluationScoreStatistics();
        
        // 计算平均分、最高分、最低分
        BigDecimal total = scores.stream()
                .map(ComprehensiveScore::getComprehensiveScore)
                .filter(score -> score != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        long count = scores.stream()
                .filter(s -> s.getComprehensiveScore() != null)
                .count();
        
        if (count > 0) {
            statistics.setAverageScore(total.divide(new BigDecimal(count), 2, RoundingMode.HALF_UP));
            
            BigDecimal maxScore = scores.stream()
                    .map(ComprehensiveScore::getComprehensiveScore)
                    .filter(score -> score != null)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.setMaxScore(maxScore);
            
            BigDecimal minScore = scores.stream()
                    .map(ComprehensiveScore::getComprehensiveScore)
                    .filter(score -> score != null)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            statistics.setMinScore(minScore);
        }
        
        // 统计各等级人数
        long excellentCount = scores.stream()
                .filter(s -> {
                    BigDecimal score = s.getComprehensiveScore();
                    return score != null && score.compareTo(new BigDecimal("90")) >= 0;
                })
                .count();
        long goodCount = scores.stream()
                .filter(s -> {
                    BigDecimal score = s.getComprehensiveScore();
                    return score != null && score.compareTo(new BigDecimal("80")) >= 0 && score.compareTo(new BigDecimal("90")) < 0;
                })
                .count();
        long mediumCount = scores.stream()
                .filter(s -> {
                    BigDecimal score = s.getComprehensiveScore();
                    return score != null && score.compareTo(new BigDecimal("70")) >= 0 && score.compareTo(new BigDecimal("80")) < 0;
                })
                .count();
        long passCount = scores.stream()
                .filter(s -> {
                    BigDecimal score = s.getComprehensiveScore();
                    return score != null && score.compareTo(new BigDecimal("60")) >= 0 && score.compareTo(new BigDecimal("70")) < 0;
                })
                .count();
        long failCount = scores.stream()
                .filter(s -> {
                    BigDecimal score = s.getComprehensiveScore();
                    return score != null && score.compareTo(new BigDecimal("60")) < 0;
                })
                .count();
        
        statistics.setExcellentCount(excellentCount);
        statistics.setGoodCount(goodCount);
        statistics.setMediumCount(mediumCount);
        statistics.setPassCount(passCount);
        statistics.setFailCount(failCount);
        
        // 构建柱状图数据
        List<EvaluationScoreStatistics.BarChartItem> barChartData = new ArrayList<>();
        barChartData.add(new EvaluationScoreStatistics.BarChartItem("优秀", excellentCount, COLOR_SUCCESS));
        barChartData.add(new EvaluationScoreStatistics.BarChartItem("良好", goodCount, COLOR_PRIMARY));
        barChartData.add(new EvaluationScoreStatistics.BarChartItem("中等", mediumCount, COLOR_WARNING));
        barChartData.add(new EvaluationScoreStatistics.BarChartItem("及格", passCount, COLOR_INFO));
        barChartData.add(new EvaluationScoreStatistics.BarChartItem("不及格", failCount, "#FF7875"));
        statistics.setBarChartData(barChartData);
        
        return statistics;
    }
    
    @Override
    public InternshipDurationStatistics getInternshipDurationStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        wrapper.isNotNull(InternshipApply::getInternshipStartDate)
               .isNotNull(InternshipApply::getInternshipEndDate);
        
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        InternshipDurationStatistics statistics = new InternshipDurationStatistics();
        
        if (applies.isEmpty()) {
            statistics.setAverageDuration(BigDecimal.ZERO);
            statistics.setTotalDuration(0L);
            statistics.setMaxDuration(0L);
            statistics.setMinDuration(0L);
            statistics.setLineChartData(new ArrayList<>());
            return statistics;
        }
        
        // 计算时长
        List<Long> durations = applies.stream()
                .map(a -> ChronoUnit.DAYS.between(a.getInternshipStartDate(), a.getInternshipEndDate()))
                .filter(d -> d > 0)
                .collect(Collectors.toList());
        
        if (durations.isEmpty()) {
            statistics.setAverageDuration(BigDecimal.ZERO);
            statistics.setTotalDuration(0L);
            statistics.setMaxDuration(0L);
            statistics.setMinDuration(0L);
            statistics.setLineChartData(new ArrayList<>());
            return statistics;
        }
        
        long totalDuration = durations.stream().mapToLong(Long::longValue).sum();
        long avgDuration = totalDuration / durations.size();
        long maxDuration = durations.stream().mapToLong(Long::longValue).max().orElse(0L);
        long minDuration = durations.stream().mapToLong(Long::longValue).min().orElse(0L);
        
        statistics.setAverageDuration(new BigDecimal(avgDuration).setScale(2, RoundingMode.HALF_UP));
        statistics.setTotalDuration(totalDuration);
        statistics.setMaxDuration(maxDuration);
        statistics.setMinDuration(minDuration);
        
        // TODO: 构建折线图数据（按月份统计）
        statistics.setLineChartData(new ArrayList<>());
        
        return statistics;
    }
    
    @Override
    public PostTypeDistributionStatistics getPostTypeDistributionStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        wrapper.isNotNull(InternshipApply::getPostId);
        
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        PostTypeDistributionStatistics statistics = new PostTypeDistributionStatistics();
        List<PostTypeDistributionStatistics.PieChartItem> pieChartData = new ArrayList<>();
        
        if (applies.isEmpty()) {
            statistics.setPieChartData(pieChartData);
            return statistics;
        }
        
        // 按岗位类型分组统计
        Map<Long, Long> postTypeCount = applies.stream()
                .collect(Collectors.groupingBy(InternshipApply::getPostId, Collectors.counting()));
        
        // 获取岗位信息并构建饼图数据
        String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING};
        int colorIndex = 0;
        
        for (Map.Entry<Long, Long> entry : postTypeCount.entrySet()) {
            InternshipPost post = internshipPostMapper.selectById(entry.getKey());
            if (post != null && post.getDeleteFlag() != null && post.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                String postTypeName = post.getPostName() != null ? post.getPostName() : "未知岗位";
                pieChartData.add(new PostTypeDistributionStatistics.PieChartItem(
                        postTypeName,
                        entry.getValue(),
                        colors[colorIndex % colors.length]
                ));
                colorIndex++;
            }
        }
        
        statistics.setPieChartData(pieChartData);
        return statistics;
    }
    
    @Override
    public MajorStatisticsDTO getMajorStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        if (applies.isEmpty()) {
            MajorStatisticsDTO statistics = new MajorStatisticsDTO();
            statistics.setBarChartData(new ArrayList<>());
            return statistics;
        }
        
        // 获取所有学生ID
        List<Long> studentIds = applies.stream()
                .map(InternshipApply::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (studentIds.isEmpty()) {
            MajorStatisticsDTO statistics = new MajorStatisticsDTO();
            statistics.setBarChartData(new ArrayList<>());
            return statistics;
        }
        
        // 查询学生信息，按专业ID分组统计
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(Student::getStudentId, studentIds)
                     .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                     .isNotNull(Student::getMajorId);
        
        List<Student> students = studentMapper.selectList(studentWrapper);
        
        // 按专业ID分组统计人数
        Map<Long, Long> majorCountMap = students.stream()
                .collect(Collectors.groupingBy(Student::getMajorId, Collectors.counting()));
        
        // 构建柱状图数据
        List<MajorStatisticsDTO.BarChartItem> barChartData = new ArrayList<>();
        String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING, "#FF7875"};
        int colorIndex = 0;
        
        for (Map.Entry<Long, Long> entry : majorCountMap.entrySet()) {
            Major major = majorMapper.selectById(entry.getKey());
            if (major != null && major.getDeleteFlag() != null && major.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                String majorName = major.getMajorName() != null ? major.getMajorName() : "未知专业";
                barChartData.add(new MajorStatisticsDTO.BarChartItem(
                        majorName,
                        entry.getValue(),
                        colors[colorIndex % colors.length]
                ));
                colorIndex++;
            }
        }
        
        MajorStatisticsDTO statistics = new MajorStatisticsDTO();
        statistics.setBarChartData(barChartData);
        return statistics;
    }
    
    @Override
    public ClassStatisticsDTO getClassStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        if (applies.isEmpty()) {
            ClassStatisticsDTO statistics = new ClassStatisticsDTO();
            statistics.setBarChartData(new ArrayList<>());
            return statistics;
        }
        
        // 获取所有学生ID
        List<Long> studentIds = applies.stream()
                .map(InternshipApply::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (studentIds.isEmpty()) {
            ClassStatisticsDTO statistics = new ClassStatisticsDTO();
            statistics.setBarChartData(new ArrayList<>());
            return statistics;
        }
        
        // 查询学生信息，按班级ID分组统计
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(Student::getStudentId, studentIds)
                     .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                     .isNotNull(Student::getClassId);
        
        List<Student> students = studentMapper.selectList(studentWrapper);
        
        // 按班级ID分组统计人数
        Map<Long, Long> classCountMap = students.stream()
                .collect(Collectors.groupingBy(Student::getClassId, Collectors.counting()));
        
        // 构建柱状图数据
        List<ClassStatisticsDTO.BarChartItem> barChartData = new ArrayList<>();
        String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING, "#FF7875"};
        int colorIndex = 0;
        
        for (Map.Entry<Long, Long> entry : classCountMap.entrySet()) {
            Class classInfo = classMapper.selectById(entry.getKey());
            if (classInfo != null && classInfo.getDeleteFlag() != null && classInfo.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                String className = classInfo.getClassName() != null ? classInfo.getClassName() : "未知班级";
                barChartData.add(new ClassStatisticsDTO.BarChartItem(
                        className,
                        entry.getValue(),
                        colors[colorIndex % colors.length]
                ));
                colorIndex++;
            }
        }
        
        ClassStatisticsDTO statistics = new ClassStatisticsDTO();
        statistics.setBarChartData(barChartData);
        return statistics;
    }
    
    @Override
    public EnterpriseStatisticsDTO getEnterpriseStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        wrapper.isNotNull(InternshipApply::getEnterpriseId);
        
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        if (applies.isEmpty()) {
            EnterpriseStatisticsDTO statistics = new EnterpriseStatisticsDTO();
            statistics.setBarChartData(new ArrayList<>());
            return statistics;
        }
        
        // 按企业ID分组统计人数
        Map<Long, Long> enterpriseCountMap = applies.stream()
                .collect(Collectors.groupingBy(InternshipApply::getEnterpriseId, Collectors.counting()));
        
        // 构建柱状图数据
        List<EnterpriseStatisticsDTO.BarChartItem> barChartData = new ArrayList<>();
        String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING, "#FF7875"};
        int colorIndex = 0;
        
        for (Map.Entry<Long, Long> entry : enterpriseCountMap.entrySet()) {
            Enterprise enterprise = enterpriseMapper.selectById(entry.getKey());
            if (enterprise != null && enterprise.getDeleteFlag() != null && enterprise.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                String enterpriseName = enterprise.getEnterpriseName() != null ? enterprise.getEnterpriseName() : "未知企业";
                barChartData.add(new EnterpriseStatisticsDTO.BarChartItem(
                        enterpriseName,
                        entry.getValue(),
                        colors[colorIndex % colors.length]
                ));
                colorIndex++;
            }
        }
        
        EnterpriseStatisticsDTO statistics = new EnterpriseStatisticsDTO();
        statistics.setBarChartData(barChartData);
        return statistics;
    }
    
    @Override
    public StudentScoreRankingDTO getStudentScoreRanking(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<ComprehensiveScore> scoreWrapper = new LambdaQueryWrapper<>();
        QueryWrapperUtil.notDeleted(scoreWrapper, ComprehensiveScore::getDeleteFlag);
        
        // 通过applyId关联查询，应用权限过滤
        if (queryDTO != null && queryDTO.getClassId() != null) {
            // 根据班级ID查询学生
            LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(Student::getClassId, queryDTO.getClassId())
                         .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                         .select(Student::getStudentId);
            List<Student> students = studentMapper.selectList(studentWrapper);
            
            if (students.isEmpty()) {
                StudentScoreRankingDTO statistics = new StudentScoreRankingDTO();
                statistics.setBarChartData(new ArrayList<>());
                return statistics;
            }
            
            List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
            scoreWrapper.in(ComprehensiveScore::getStudentId, studentIds);
        } else {
            // 应用数据权限过滤
            LambdaQueryWrapper<InternshipApply> applyWrapper = buildApplyQueryWrapper(queryDTO);
            List<InternshipApply> applies = internshipApplyMapper.selectList(applyWrapper);
            if (applies.isEmpty()) {
                StudentScoreRankingDTO statistics = new StudentScoreRankingDTO();
                statistics.setBarChartData(new ArrayList<>());
                return statistics;
            }
            List<Long> applyIds = applies.stream().map(InternshipApply::getApplyId).collect(Collectors.toList());
            scoreWrapper.in(ComprehensiveScore::getApplyId, applyIds);
        }
        
        // 查询综合成绩，按分数降序排列
        scoreWrapper.orderByDesc(ComprehensiveScore::getComprehensiveScore);
        List<ComprehensiveScore> scores = comprehensiveScoreMapper.selectList(scoreWrapper);
        
        // 限制返回前20名
        if (scores.size() > 20) {
            scores = scores.subList(0, 20);
        }
        
        // 构建柱状图数据
        List<StudentScoreRankingDTO.BarChartItem> barChartData = new ArrayList<>();
        for (ComprehensiveScore score : scores) {
            if (score.getComprehensiveScore() == null) {
                continue;
            }
            
            // 查询学生信息
            Student student = studentMapper.selectById(score.getStudentId());
            if (student == null) {
                continue;
            }
            
            // 通过userId查询用户信息获取姓名
            String studentName = "未知学生";
            if (student.getUserId() != null) {
                UserInfo userInfo = userMapper.selectById(student.getUserId());
                if (userInfo != null && userInfo.getRealName() != null) {
                    studentName = userInfo.getRealName();
                }
            }
            
            barChartData.add(new StudentScoreRankingDTO.BarChartItem(
                    studentName,
                    score.getComprehensiveScore(),
                    COLOR_PRIMARY
            ));
        }
        
        StudentScoreRankingDTO statistics = new StudentScoreRankingDTO();
        statistics.setBarChartData(barChartData);
        return statistics;
    }
    
    @Override
    public PendingReviewStatistics getPendingReviewStatistics(StatisticsQueryDTO queryDTO) {
        PendingReviewStatistics statistics = new PendingReviewStatistics();
        
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> applyWrapper = buildApplyQueryWrapper(queryDTO);
        List<InternshipApply> applies = internshipApplyMapper.selectList(applyWrapper);
        
        if (applies.isEmpty()) {
            statistics.setPendingLogCount(0L);
            statistics.setPendingReportCount(0L);
            return statistics;
        }
        
        // 获取学生ID列表
        List<Long> studentIds = applies.stream()
                .map(InternshipApply::getStudentId)
                .filter(id -> id != null)
                .distinct()
                .collect(Collectors.toList());
        
        if (studentIds.isEmpty()) {
            statistics.setPendingLogCount(0L);
            statistics.setPendingReportCount(0L);
            return statistics;
        }
        
        // 查询待批阅日志数量（reviewStatus=0）
        LambdaQueryWrapper<InternshipLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.in(InternshipLog::getStudentId, studentIds)
                  .eq(InternshipLog::getReviewStatus, 0) // 0-未批阅
                  .eq(InternshipLog::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        long pendingLogCount = internshipLogMapper.selectCount(logWrapper);
        
        // 查询待批阅周报数量（reviewStatus=0）
        LambdaQueryWrapper<InternshipWeeklyReport> reportWrapper = new LambdaQueryWrapper<>();
        reportWrapper.in(InternshipWeeklyReport::getStudentId, studentIds)
                     .eq(InternshipWeeklyReport::getReviewStatus, 0) // 0-未批阅
                     .eq(InternshipWeeklyReport::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        
        long pendingReportCount = internshipWeeklyReportMapper.selectCount(reportWrapper);
        
        statistics.setPendingLogCount(pendingLogCount);
        statistics.setPendingReportCount(pendingReportCount);
        return statistics;
    }
    
    /**
     * 构建实习申请查询条件（应用权限过滤和筛选条件）
     */
    private LambdaQueryWrapper<InternshipApply> buildApplyQueryWrapper(StatisticsQueryDTO queryDTO) {
        LambdaQueryWrapper<InternshipApply> wrapper = new LambdaQueryWrapper<>();
        
        // 只查询未删除的数据
        QueryWrapperUtil.notDeleted(wrapper, InternshipApply::getDeleteFlag);
        
        // 权限过滤
        applyDataPermissionFilter(wrapper);
        
        // 应用筛选条件
        if (queryDTO != null) {
            // 时间范围筛选
            if (queryDTO.getStartDate() != null) {
                wrapper.ge(InternshipApply::getInternshipStartDate, queryDTO.getStartDate());
            }
            if (queryDTO.getEndDate() != null) {
                wrapper.le(InternshipApply::getInternshipEndDate, queryDTO.getEndDate());
            }
            
            // 组织维度筛选
            if (queryDTO.getSchoolId() != null) {
                // 通过student关联查询
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getSchoolId, queryDTO.getSchoolId())
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L); // 无匹配数据
                }
            }
            
            if (queryDTO.getCollegeId() != null) {
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getCollegeId, queryDTO.getCollegeId())
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L);
                }
            }
            
            if (queryDTO.getMajorId() != null) {
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getMajorId, queryDTO.getMajorId())
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L);
                }
            }
            
            if (queryDTO.getClassId() != null) {
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getClassId, queryDTO.getClassId())
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L);
                }
            }
            
            if (queryDTO.getEnterpriseId() != null) {
                wrapper.eq(InternshipApply::getEnterpriseId, queryDTO.getEnterpriseId());
            }
            
            if (queryDTO.getStudentId() != null) {
                wrapper.eq(InternshipApply::getStudentId, queryDTO.getStudentId());
            }
        }
        
        return wrapper;
    }
    
    /**
     * 应用数据权限过滤
     */
    private void applyDataPermissionFilter(LambdaQueryWrapper<InternshipApply> wrapper) {
        if (dataPermissionUtil.isSystemAdmin()) {
            return; // 系统管理员查看所有数据
        }
        
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        if (user == null) {
            wrapper.eq(InternshipApply::getApplyId, -1L);
            return;
        }
        
        // 获取用户角色
        List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
        if (roleCodes == null || roleCodes.isEmpty()) {
            wrapper.eq(InternshipApply::getApplyId, -1L);
            return;
        }
        
        // 企业管理员：只能查看本企业的数据
        if (roleCodes.contains("ROLE_ENTERPRISE_ADMIN")) {
            Long enterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
            if (enterpriseId != null) {
                wrapper.eq(InternshipApply::getEnterpriseId, enterpriseId);
            } else {
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
            return;
        }
        
        // 学生：只能查看自己的数据
        if (roleCodes.contains("ROLE_STUDENT")) {
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
                            .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
            );
            if (student != null) {
                wrapper.eq(InternshipApply::getStudentId, student.getStudentId());
            } else {
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
            return;
        }
        
        // 班主任：只能查看本班级学生的数据
        if (roleCodes.contains("ROLE_CLASS_TEACHER")) {
            List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
            if (classIds != null && !classIds.isEmpty()) {
                // 通过班级ID查询学生，再通过学生ID过滤申请
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.in(Student::getClassId, classIds)
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L);
                }
            } else {
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
            return;
        }
        
        // 学院负责人：只能查看本院学生的数据
        if (roleCodes.contains("ROLE_COLLEGE_LEADER")) {
            Long collegeId = dataPermissionUtil.getCurrentUserCollegeId();
            if (collegeId != null) {
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getCollegeId, collegeId)
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L);
                }
            } else {
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
            return;
        }
        
        // 学校管理员：只能查看本校学生的数据
        if (roleCodes.contains("ROLE_SCHOOL_ADMIN")) {
            Long schoolId = dataPermissionUtil.getCurrentUserSchoolId();
            if (schoolId != null) {
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getSchoolId, schoolId)
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .select(Student::getStudentId);
                List<Student> students = studentMapper.selectList(studentWrapper);
                if (!students.isEmpty()) {
                    List<Long> studentIds = students.stream().map(Student::getStudentId).collect(Collectors.toList());
                    wrapper.in(InternshipApply::getStudentId, studentIds);
                } else {
                    wrapper.eq(InternshipApply::getApplyId, -1L);
                }
            } else {
                wrapper.eq(InternshipApply::getApplyId, -1L);
            }
            return;
        }
        
        // 其他角色：返回空结果
        wrapper.eq(InternshipApply::getApplyId, -1L);
    }
    
    /**
     * 创建空的评价分数统计
     */
    private EvaluationScoreStatistics createEmptyEvaluationScoreStatistics() {
        EvaluationScoreStatistics statistics = new EvaluationScoreStatistics();
        statistics.setAverageScore(BigDecimal.ZERO);
        statistics.setMaxScore(BigDecimal.ZERO);
        statistics.setMinScore(BigDecimal.ZERO);
        statistics.setExcellentCount(0L);
        statistics.setGoodCount(0L);
        statistics.setMediumCount(0L);
        statistics.setPassCount(0L);
        statistics.setFailCount(0L);
        statistics.setBarChartData(new ArrayList<>());
        return statistics;
    }
}

