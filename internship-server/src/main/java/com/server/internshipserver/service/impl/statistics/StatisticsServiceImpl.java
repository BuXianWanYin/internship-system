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
import com.server.internshipserver.domain.system.College;
import com.server.internshipserver.domain.system.Major;
import com.server.internshipserver.domain.system.School;
import com.server.internshipserver.domain.user.Enterprise;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.evaluation.ComprehensiveScoreMapper;
import com.server.internshipserver.mapper.internship.InternshipApplyMapper;
import com.server.internshipserver.mapper.internship.InternshipLogMapper;
import com.server.internshipserver.mapper.internship.InternshipPostMapper;
import com.server.internshipserver.mapper.internship.InternshipWeeklyReportMapper;
import com.server.internshipserver.mapper.system.ClassMapper;
import com.server.internshipserver.mapper.system.CollegeMapper;
import com.server.internshipserver.mapper.system.MajorMapper;
import com.server.internshipserver.mapper.system.SchoolMapper;
import com.server.internshipserver.mapper.user.EnterpriseMapper;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.statistics.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据统计Service实现类
 * 实现各类数据统计、报表生成等业务功能
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
    
    @Autowired
    private SchoolMapper schoolMapper;
    
    @Autowired
    private CollegeMapper collegeMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    // 配色方案
    private static final String COLOR_PRIMARY = "#409EFF";
    private static final String COLOR_SUCCESS = "#67C23A";
    private static final String COLOR_WARNING = "#F7BA2A";
    private static final String COLOR_INFO = "#909399";
    private static final String COLOR_PENDING = "#C0C4CC";
    
    @Override
    public InternshipProgressStatisticsDTO getInternshipProgressStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        
        // 查询所有实习申请
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        InternshipProgressStatisticsDTO statistics = new InternshipProgressStatisticsDTO();
        
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
        InternshipProgressStatisticsDTO.ProgressPieChartData pieChartData = new InternshipProgressStatisticsDTO.ProgressPieChartData();
        pieChartData.setPending(new InternshipProgressStatisticsDTO.PieItem("待开始", pendingCount, COLOR_PENDING));
        pieChartData.setInProgress(new InternshipProgressStatisticsDTO.PieItem("进行中", inProgressCount, COLOR_PRIMARY));
        pieChartData.setCompleted(new InternshipProgressStatisticsDTO.PieItem("已完成", completedCount, COLOR_SUCCESS));
        statistics.setPieChartData(pieChartData);
        
        return statistics;
    }
    
    @Override
    public EvaluationScoreStatisticsDTO getEvaluationScoreStatistics(StatisticsQueryDTO queryDTO) {
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
        
        EvaluationScoreStatisticsDTO statistics = new EvaluationScoreStatisticsDTO();
        
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
        List<EvaluationScoreStatisticsDTO.BarChartItem> barChartData = new ArrayList<>();
        barChartData.add(new EvaluationScoreStatisticsDTO.BarChartItem("优秀", excellentCount, COLOR_SUCCESS));
        barChartData.add(new EvaluationScoreStatisticsDTO.BarChartItem("良好", goodCount, COLOR_PRIMARY));
        barChartData.add(new EvaluationScoreStatisticsDTO.BarChartItem("中等", mediumCount, COLOR_WARNING));
        barChartData.add(new EvaluationScoreStatisticsDTO.BarChartItem("及格", passCount, COLOR_INFO));
        barChartData.add(new EvaluationScoreStatisticsDTO.BarChartItem("不及格", failCount, "#FF7875"));
        statistics.setBarChartData(barChartData);
        
        return statistics;
    }
    
    @Override
    public InternshipDurationStatisticsDTO getInternshipDurationStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        wrapper.isNotNull(InternshipApply::getInternshipStartDate)
               .isNotNull(InternshipApply::getInternshipEndDate);
        
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        InternshipDurationStatisticsDTO statistics = new InternshipDurationStatisticsDTO();
        
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
        
        // 构建折线图数据（按月份统计）
        Map<String, List<Long>> monthDurations = applies.stream()
                .filter(a -> a.getInternshipStartDate() != null && a.getInternshipEndDate() != null)
                .collect(Collectors.groupingBy(
                    a -> a.getInternshipStartDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.mapping(
                        a -> ChronoUnit.DAYS.between(a.getInternshipStartDate(), a.getInternshipEndDate()),
                        Collectors.toList()
                    )
                ));
        
        List<InternshipDurationStatisticsDTO.LineChartItem> lineChartData = new ArrayList<>();
        for (Map.Entry<String, List<Long>> entry : monthDurations.entrySet()) {
            List<Long> monthDurs = entry.getValue();
            if (!monthDurs.isEmpty()) {
                long avgDays = monthDurs.stream().mapToLong(Long::longValue).sum() / monthDurs.size();
                InternshipDurationStatisticsDTO.LineChartItem item = new InternshipDurationStatisticsDTO.LineChartItem();
                item.setMonth(entry.getKey());
                item.setAverageDays(new BigDecimal(avgDays).setScale(2, RoundingMode.HALF_UP));
                lineChartData.add(item);
            }
        }
        // 按月份排序
        lineChartData.sort((a, b) -> a.getMonth().compareTo(b.getMonth()));
        statistics.setLineChartData(lineChartData);
        
        return statistics;
    }
    
    @Override
    public PostTypeDistributionStatisticsDTO getPostTypeDistributionStatistics(StatisticsQueryDTO queryDTO) {
        PostTypeDistributionStatisticsDTO statistics = new PostTypeDistributionStatisticsDTO();
        List<PostTypeDistributionStatisticsDTO.PieChartItem> pieChartData = new ArrayList<>();
        
        // 判断是否是企业管理员查看自己的数据
        UserInfo user = UserUtil.getCurrentUserOrNull(userMapper);
        boolean isEnterpriseAdmin = false;
        Long currentEnterpriseId = null;
        
        if (user != null) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
            if (roleCodes != null && roleCodes.contains("ROLE_ENTERPRISE_ADMIN")) {
                Long userEnterpriseId = dataPermissionUtil.getCurrentUserEnterpriseId();
                if (userEnterpriseId != null) {
                    // 如果queryDTO中有enterpriseId，检查是否匹配
                    if (queryDTO != null && queryDTO.getEnterpriseId() != null) {
                        if (userEnterpriseId.equals(queryDTO.getEnterpriseId())) {
                            isEnterpriseAdmin = true;
                            currentEnterpriseId = userEnterpriseId;
                        }
                    } else {
                        // 如果没有指定enterpriseId，通过权限过滤判断
                        // 检查applyDataPermissionFilter是否会过滤到当前企业
                        // 如果是企业管理员且没有指定enterpriseId，默认查看自己的企业数据
                        isEnterpriseAdmin = true;
                        currentEnterpriseId = userEnterpriseId;
                    }
                }
            }
        }
        
        // 如果是企业管理员查看自己的数据，统计所有发布的岗位
        if (isEnterpriseAdmin && currentEnterpriseId != null) {
            LambdaQueryWrapper<InternshipPost> postWrapper = QueryWrapperUtil.buildNotDeletedWrapper(InternshipPost::getDeleteFlag);
            postWrapper.eq(InternshipPost::getEnterpriseId, currentEnterpriseId);
            List<InternshipPost> posts = internshipPostMapper.selectList(postWrapper);
            
            // 按岗位名称分组统计
            Map<String, Long> postNameCount = new HashMap<>();
            for (InternshipPost post : posts) {
                String postName = post.getPostName();
                if (postName != null && !postName.trim().isEmpty()) {
                    postNameCount.put(postName, postNameCount.getOrDefault(postName, 0L) + 1);
                }
            }
            
            // 构建饼图数据
            String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING};
            int colorIndex = 0;
            for (Map.Entry<String, Long> entry : postNameCount.entrySet()) {
                pieChartData.add(new PostTypeDistributionStatisticsDTO.PieChartItem(
                        entry.getKey(),
                        entry.getValue(),
                        colors[colorIndex % colors.length]
                ));
                colorIndex++;
            }
        } else {
            // 其他情况：基于实习申请统计（包含自主实习）
            LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
            List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        if (applies.isEmpty()) {
            statistics.setPieChartData(pieChartData);
            return statistics;
        }
        
            // 按岗位名称分组统计（包含合作企业和自主实习）
            Map<String, Long> postNameCount = new HashMap<>();
            
            for (InternshipApply apply : applies) {
                String postName = null;
        
                // 判断是合作企业还是自主实习
                if (apply.getApplyType() != null && apply.getApplyType() == 2) {
                    // 自主实习：使用selfPostName
                    postName = apply.getSelfPostName();
                    if (postName == null || postName.trim().isEmpty()) {
                        postName = "自主实习-未知岗位";
                    } else {
                        postName = "自主实习-" + postName;
                    }
                } else {
                    // 合作企业：通过postId查询岗位名称
                    if (apply.getPostId() != null) {
                        InternshipPost post = internshipPostMapper.selectById(apply.getPostId());
            if (post != null && post.getDeleteFlag() != null && post.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                            postName = post.getPostName() != null ? post.getPostName() : "未知岗位";
                        } else {
                            postName = "未知岗位";
                        }
                    } else {
                        postName = "未知岗位";
                    }
                }
                
                postNameCount.put(postName, postNameCount.getOrDefault(postName, 0L) + 1);
            }
            
            // 构建饼图数据
            String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING};
            int colorIndex = 0;
            for (Map.Entry<String, Long> entry : postNameCount.entrySet()) {
                pieChartData.add(new PostTypeDistributionStatisticsDTO.PieChartItem(
                        entry.getKey(),
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
    public CollegeStatisticsDTO getCollegeStatistics(StatisticsQueryDTO queryDTO) {
        // 构建查询条件
        LambdaQueryWrapper<InternshipApply> wrapper = buildApplyQueryWrapper(queryDTO);
        List<InternshipApply> applies = internshipApplyMapper.selectList(wrapper);
        
        if (applies.isEmpty()) {
            CollegeStatisticsDTO statistics = new CollegeStatisticsDTO();
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
            CollegeStatisticsDTO statistics = new CollegeStatisticsDTO();
            statistics.setBarChartData(new ArrayList<>());
            return statistics;
        }
        
        // 查询学生信息，按学院ID分组统计
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(Student::getStudentId, studentIds)
                     .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                     .isNotNull(Student::getCollegeId);
        
        List<Student> students = studentMapper.selectList(studentWrapper);
        
        // 按学院ID分组统计人数
        Map<Long, Long> collegeCountMap = students.stream()
                .collect(Collectors.groupingBy(Student::getCollegeId, Collectors.counting()));
        
        // 构建柱状图数据
        List<CollegeStatisticsDTO.BarChartItem> barChartData = new ArrayList<>();
        String[] colors = {COLOR_PRIMARY, "#79BBFF", "#A0CFFF", COLOR_SUCCESS, COLOR_WARNING, "#FF7875"};
        int colorIndex = 0;
        
        for (Map.Entry<Long, Long> entry : collegeCountMap.entrySet()) {
            College college = collegeMapper.selectById(entry.getKey());
            if (college != null && college.getDeleteFlag() != null && college.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                String collegeName = college.getCollegeName() != null ? college.getCollegeName() : "未知学院";
                barChartData.add(new CollegeStatisticsDTO.BarChartItem(
                        collegeName,
                        entry.getValue(),
                        colors[colorIndex % colors.length]
                ));
                colorIndex++;
            }
        }
        
        CollegeStatisticsDTO statistics = new CollegeStatisticsDTO();
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
    public PendingReviewStatisticsDTO getPendingReviewStatistics(StatisticsQueryDTO queryDTO) {
        PendingReviewStatisticsDTO statistics = new PendingReviewStatisticsDTO();
        
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
        
        // 企业导师：只能查看本企业的数据（类似企业管理员）
        if (roleCodes.contains("ROLE_ENTERPRISE_MENTOR")) {
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
    private EvaluationScoreStatisticsDTO createEmptyEvaluationScoreStatistics() {
        EvaluationScoreStatisticsDTO statistics = new EvaluationScoreStatisticsDTO();
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
    
    @Override
    public SystemAdminDashboardDTO getSystemAdminDashboard() {
        SystemAdminDashboardDTO dto = new SystemAdminDashboardDTO();
        
        // 1. 统计学校总数
        LambdaQueryWrapper<School> schoolWrapper = QueryWrapperUtil.buildNotDeletedWrapper(School::getDeleteFlag);
        long schoolCount = schoolMapper.selectCount(schoolWrapper);
        dto.setSchoolCount(schoolCount);
        
        // 2. 统计用户总数
        LambdaQueryWrapper<UserInfo> userWrapper = QueryWrapperUtil.buildNotDeletedWrapper(UserInfo::getDeleteFlag);
        long userCount = userMapper.selectCount(userWrapper);
        dto.setUserCount(userCount);
        
        // 3. 统计实习岗位总数
        LambdaQueryWrapper<InternshipPost> postWrapper = QueryWrapperUtil.buildNotDeletedWrapper(InternshipPost::getDeleteFlag);
        long postCount = internshipPostMapper.selectCount(postWrapper);
        dto.setPostCount(postCount);
        
        // 4. 统计实习学生总数（status=6进行中）
        LambdaQueryWrapper<InternshipApply> applyWrapper = QueryWrapperUtil.buildNotDeletedWrapper(InternshipApply::getDeleteFlag);
        applyWrapper.eq(InternshipApply::getStatus, 6); // 进行中
        long internshipStudentCount = internshipApplyMapper.selectCount(applyWrapper);
        dto.setInternshipStudentCount(internshipStudentCount);
        
        // 5. 统计合作企业总数
        LambdaQueryWrapper<Enterprise> enterpriseWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Enterprise::getDeleteFlag);
        long enterpriseCount = enterpriseMapper.selectCount(enterpriseWrapper);
        dto.setEnterpriseCount(enterpriseCount);
        
        // 6. 用户角色分布统计
        List<UserInfo> allUsers = userMapper.selectList(userWrapper);
        Map<String, Long> roleCountMap = new java.util.HashMap<>();
        
        // 定义角色代码到角色名称的映射
        Map<String, String> roleNameMap = new java.util.HashMap<>();
        roleNameMap.put("ROLE_SYSTEM_ADMIN", "系统管理员");
        roleNameMap.put("ROLE_SCHOOL_ADMIN", "学校管理员");
        roleNameMap.put("ROLE_COLLEGE_LEADER", "学院负责人");
        roleNameMap.put("ROLE_CLASS_TEACHER", "班主任");
        roleNameMap.put("ROLE_STUDENT", "学生");
        roleNameMap.put("ROLE_ENTERPRISE_ADMIN", "企业管理员");
        roleNameMap.put("ROLE_ENTERPRISE_MENTOR", "企业导师");
        roleNameMap.put("ROLE_TEACHER", "教师");
        
        // 统计每个角色的用户数
        for (UserInfo user : allUsers) {
            List<String> roleCodes = userMapper.selectRoleCodesByUserId(user.getUserId());
            if (roleCodes != null && !roleCodes.isEmpty()) {
                for (String roleCode : roleCodes) {
                    roleCountMap.put(roleCode, roleCountMap.getOrDefault(roleCode, 0L) + 1);
                }
            }
        }
        
        // 转换为饼图数据
        List<SystemAdminDashboardDTO.PieItem> roleDistribution = new ArrayList<>();
        String[] colors = {COLOR_PRIMARY, COLOR_SUCCESS, COLOR_WARNING, "#E6A23C", "#909399", "#F56C6C", "#67C23A", COLOR_INFO};
        int colorIndex = 0;
        for (Map.Entry<String, Long> entry : roleCountMap.entrySet()) {
            String roleName = roleNameMap.getOrDefault(entry.getKey(), entry.getKey());
            roleDistribution.add(new SystemAdminDashboardDTO.PieItem(
                roleName,
                entry.getValue(),
                colors[colorIndex % colors.length]
            ));
            colorIndex++;
        }
        dto.setUserRoleDistribution(roleDistribution);
        
        // 7. 各学校实习人数对比
        List<School> schools = schoolMapper.selectList(schoolWrapper);
        List<SystemAdminDashboardDTO.BarChartItem> schoolComparison = new ArrayList<>();
        for (School school : schools) {
            // 查询该校实习学生数
            LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
            studentWrapper.eq(Student::getSchoolId, school.getSchoolId())
                         .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                         .eq(Student::getInternshipStatus, 1); // 实习中
            long count = studentMapper.selectCount(studentWrapper);
            
            if (count > 0) {
                schoolComparison.add(new SystemAdminDashboardDTO.BarChartItem(
                    school.getSchoolName(),
                    count,
                    COLOR_PRIMARY
                ));
            }
        }
        dto.setSchoolInternshipComparison(schoolComparison);
        
        // 8. 实习岗位类型分布（按岗位名称分组，显示前10个最常见的岗位，包含自主实习）
        // 统计合作企业的岗位
        List<InternshipPost> posts = internshipPostMapper.selectList(postWrapper);
        Map<String, Long> postTypeCountMap = new java.util.HashMap<>();
        for (InternshipPost post : posts) {
            String postName = post.getPostName();
            if (postName != null && !postName.trim().isEmpty()) {
                postTypeCountMap.put(postName, postTypeCountMap.getOrDefault(postName, 0L) + 1);
            }
        }
        
        // 统计自主实习的岗位（通过实习申请统计）
        LambdaQueryWrapper<InternshipApply> selfApplyWrapper = QueryWrapperUtil.buildNotDeletedWrapper(InternshipApply::getDeleteFlag);
        selfApplyWrapper.eq(InternshipApply::getApplyType, 2) // 自主实习
                       .isNotNull(InternshipApply::getSelfPostName);
        List<InternshipApply> selfApplies = internshipApplyMapper.selectList(selfApplyWrapper);
        for (InternshipApply apply : selfApplies) {
            String selfPostName = apply.getSelfPostName();
            if (selfPostName != null && !selfPostName.trim().isEmpty()) {
                String postName = "自主实习-" + selfPostName;
                postTypeCountMap.put(postName, postTypeCountMap.getOrDefault(postName, 0L) + 1);
            }
        }
        
        List<SystemAdminDashboardDTO.PieItem> postTypeDistribution = new ArrayList<>();
        // 按数量降序排序，只取前10个
        java.util.concurrent.atomic.AtomicInteger colorIndexAtomic = new java.util.concurrent.atomic.AtomicInteger(0);
        postTypeCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue()))
                .limit(10)
                .forEach(entry -> {
                    int index = colorIndexAtomic.getAndIncrement();
                    postTypeDistribution.add(new SystemAdminDashboardDTO.PieItem(
                        entry.getKey(),
                        entry.getValue(),
                        colors[index % colors.length]
                    ));
                });
        dto.setPostTypeDistribution(postTypeDistribution);
        
        return dto;
    }
    
    @Override
    public SchoolAdminDashboardDTO getSchoolAdminDashboard() {
        SchoolAdminDashboardDTO dto = new SchoolAdminDashboardDTO();
        
        try {
        Long schoolId = dataPermissionUtil.getCurrentUserSchoolId();
        if (schoolId == null) {
            // 如果没有学校ID，返回空数据
            dto.setCollegeCount(0L);
            dto.setMajorCount(0L);
            dto.setStudentCount(0L);
            dto.setMaleCount(0L);
            dto.setFemaleCount(0L);
            dto.setTeacherCount(0L);
            dto.setMajorInternshipComparison(new ArrayList<>());
            dto.setGenderDistribution(new ArrayList<>());
            return dto;
        }
        
        // 1. 统计学院总数
        LambdaQueryWrapper<College> collegeWrapper = QueryWrapperUtil.buildNotDeletedWrapper(College::getDeleteFlag);
        collegeWrapper.eq(College::getSchoolId, schoolId);
        long collegeCount = collegeMapper.selectCount(collegeWrapper);
        dto.setCollegeCount(collegeCount);
        
        // 2. 统计专业总数
        LambdaQueryWrapper<Major> majorWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Major::getDeleteFlag);
        // 通过学院关联查询专业
        List<College> colleges = collegeMapper.selectList(collegeWrapper);
        if (!colleges.isEmpty()) {
            List<Long> collegeIds = colleges.stream().map(College::getCollegeId).collect(Collectors.toList());
            majorWrapper.in(Major::getCollegeId, collegeIds);
            long majorCount = majorMapper.selectCount(majorWrapper);
            dto.setMajorCount(majorCount);
            
            // 3. 不同专业实习人数对比
            List<Major> majors = majorMapper.selectList(majorWrapper);
            List<SchoolAdminDashboardDTO.BarChartItem> majorComparison = new ArrayList<>();
            for (Major major : majors) {
                LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
                studentWrapper.eq(Student::getMajorId, major.getMajorId())
                             .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                             .eq(Student::getInternshipStatus, 1); // 实习中
                long count = studentMapper.selectCount(studentWrapper);
                
                if (count > 0) {
                    majorComparison.add(new SchoolAdminDashboardDTO.BarChartItem(
                        major.getMajorName(),
                        count,
                        COLOR_PRIMARY
                    ));
                }
            }
            dto.setMajorInternshipComparison(majorComparison);
        } else {
            dto.setMajorCount(0L);
            dto.setMajorInternshipComparison(new ArrayList<>());
        }
        
        // 4. 统计学生总数和性别分布
        LambdaQueryWrapper<Student> studentWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Student::getDeleteFlag);
        studentWrapper.eq(Student::getSchoolId, schoolId);
        List<Student> students = studentMapper.selectList(studentWrapper);
        long studentCount = students.size();
        dto.setStudentCount(studentCount);
        
        // 统计性别分布（需要通过userId关联UserInfo查询性别）
        long maleCount = 0;
        long femaleCount = 0;
        List<Long> userIds = students.stream().map(Student::getUserId).filter(java.util.Objects::nonNull).collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            LambdaQueryWrapper<UserInfo> userWrapper = new LambdaQueryWrapper<>();
            userWrapper.in(UserInfo::getUserId, userIds)
                      .eq(UserInfo::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                      .select(UserInfo::getUserId, UserInfo::getGender);
            List<UserInfo> users = userMapper.selectList(userWrapper);
            
            Map<Long, String> userIdToGenderMap = users.stream()
                    .filter(user -> user.getGender() != null)
                .collect(Collectors.toMap(UserInfo::getUserId, UserInfo::getGender, (v1, v2) -> v1));
            
            for (Student student : students) {
                    if (student.getUserId() != null) {
                String gender = userIdToGenderMap.get(student.getUserId());
                if ("男".equals(gender)) {
                    maleCount++;
                } else if ("女".equals(gender)) {
                    femaleCount++;
                        }
                }
            }
        }
        
        dto.setMaleCount(maleCount);
        dto.setFemaleCount(femaleCount);
        
        // 性别分布饼图
        List<SchoolAdminDashboardDTO.PieItem> genderDistribution = new ArrayList<>();
        if (maleCount > 0) {
            genderDistribution.add(new SchoolAdminDashboardDTO.PieItem("男生", maleCount, COLOR_PRIMARY));
        }
        if (femaleCount > 0) {
            genderDistribution.add(new SchoolAdminDashboardDTO.PieItem("女生", femaleCount, "#F56C6C"));
        }
        dto.setGenderDistribution(genderDistribution);
        
        // 5. 统计教师总数
        LambdaQueryWrapper<Teacher> teacherWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Teacher::getDeleteFlag);
        teacherWrapper.eq(Teacher::getSchoolId, schoolId);
        long teacherCount = teacherMapper.selectCount(teacherWrapper);
        dto.setTeacherCount(teacherCount);
        
        return dto;
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("获取学校管理员仪表盘数据失败: " + e.getMessage());
            e.printStackTrace();
            
            // 返回空数据，避免前端报错
            dto.setCollegeCount(0L);
            dto.setMajorCount(0L);
            dto.setStudentCount(0L);
            dto.setMaleCount(0L);
            dto.setFemaleCount(0L);
            dto.setTeacherCount(0L);
            dto.setMajorInternshipComparison(new ArrayList<>());
            dto.setGenderDistribution(new ArrayList<>());
            return dto;
        }
    }
    
    @Override
    public CollegeLeaderDashboardDTO getCollegeLeaderDashboard() {
        CollegeLeaderDashboardDTO dto = new CollegeLeaderDashboardDTO();
        
        Long collegeId = dataPermissionUtil.getCurrentUserCollegeId();
        if (collegeId == null) {
            // 如果没有学院ID，返回空数据
            dto.setMajorCount(0L);
            dto.setStudentCount(0L);
            dto.setTeacherCount(0L);
            dto.setClassCount(0L);
            return dto;
        }
        
        // 1. 统计专业总数
        LambdaQueryWrapper<Major> majorWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Major::getDeleteFlag);
        majorWrapper.eq(Major::getCollegeId, collegeId);
        long majorCount = majorMapper.selectCount(majorWrapper);
        dto.setMajorCount(majorCount);
        
        // 2. 统计学生总数
        LambdaQueryWrapper<Student> studentWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Student::getDeleteFlag);
        studentWrapper.eq(Student::getCollegeId, collegeId);
        long studentCount = studentMapper.selectCount(studentWrapper);
        dto.setStudentCount(studentCount);
        
        // 3. 统计教师总数
        LambdaQueryWrapper<Teacher> teacherWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Teacher::getDeleteFlag);
        teacherWrapper.eq(Teacher::getCollegeId, collegeId);
        long teacherCount = teacherMapper.selectCount(teacherWrapper);
        dto.setTeacherCount(teacherCount);
        
        // 4. 统计班级总数
        List<Major> majors = majorMapper.selectList(majorWrapper);
        if (!majors.isEmpty()) {
            List<Long> majorIds = majors.stream().map(Major::getMajorId).collect(Collectors.toList());
            LambdaQueryWrapper<Class> classWrapper = QueryWrapperUtil.buildNotDeletedWrapper(Class::getDeleteFlag);
            classWrapper.in(Class::getMajorId, majorIds);
            long classCount = classMapper.selectCount(classWrapper);
            dto.setClassCount(classCount);
        } else {
            dto.setClassCount(0L);
        }
        
        return dto;
    }
    
    @Override
    public ClassTeacherDashboardDTO getClassTeacherDashboard() {
        ClassTeacherDashboardDTO dashboard = new ClassTeacherDashboardDTO();
        
        try {
        // 获取当前班主任管理的班级ID列表
        List<Long> classIds = dataPermissionUtil.getCurrentUserClassIds();
        
        if (classIds == null || classIds.isEmpty()) {
            // 如果没有管理的班级，返回空数据
            dashboard.setClassCount(0L);
            dashboard.setStudentCount(0L);
            dashboard.setPostCount(0L);
            dashboard.setAverageScore(0.0);
            dashboard.setPosts(new ArrayList<>());
            dashboard.setScoreDistribution(new ArrayList<>());
            return dashboard;
        }
        
        // 1. 统计管理班级数
        dashboard.setClassCount((long) classIds.size());
        
        // 2. 查询这些班级的学生
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.in(Student::getClassId, classIds)
                     .eq(Student::getDeleteFlag, DeleteFlag.NORMAL.getCode());
        List<Student> students = studentMapper.selectList(studentWrapper);
        dashboard.setStudentCount((long) students.size());
        
        if (students.isEmpty()) {
            dashboard.setPostCount(0L);
            dashboard.setAverageScore(0.0);
            dashboard.setPosts(new ArrayList<>());
            dashboard.setScoreDistribution(new ArrayList<>());
            return dashboard;
        }
        
        List<Long> studentIds = students.stream()
                .map(Student::getStudentId)
                .collect(Collectors.toList());
        
            // 3. 查询这些学生的实习申请，统计岗位数（去重，包含自主实习）
        LambdaQueryWrapper<InternshipApply> applyWrapper = new LambdaQueryWrapper<>();
        applyWrapper.in(InternshipApply::getStudentId, studentIds)
                   .eq(InternshipApply::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                       .select(InternshipApply::getPostId, InternshipApply::getApplyType, InternshipApply::getSelfPostName);
        List<InternshipApply> applies = internshipApplyMapper.selectList(applyWrapper);
        
            // 统计不重复的岗位数（包括合作企业的postId和自主实习的selfPostName）
            Set<String> uniquePosts = new HashSet<>();
            for (InternshipApply apply : applies) {
                if (apply.getApplyType() != null && apply.getApplyType() == 2) {
                    // 自主实习：使用selfPostName作为唯一标识
                    String selfPostName = apply.getSelfPostName();
                    if (selfPostName != null && !selfPostName.trim().isEmpty()) {
                        uniquePosts.add("SELF_" + selfPostName);
                    }
                } else {
                    // 合作企业：使用postId作为唯一标识
                    if (apply.getPostId() != null) {
                        uniquePosts.add("POST_" + apply.getPostId());
                    }
                }
            }
            dashboard.setPostCount((long) uniquePosts.size());
        
        // 4. 查询这些学生的综合成绩，计算平均分
        LambdaQueryWrapper<ComprehensiveScore> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.in(ComprehensiveScore::getStudentId, studentIds)
                   .eq(ComprehensiveScore::getDeleteFlag, DeleteFlag.NORMAL.getCode())
                   .isNotNull(ComprehensiveScore::getComprehensiveScore);
        List<ComprehensiveScore> scores = comprehensiveScoreMapper.selectList(scoreWrapper);
        
        if (scores.isEmpty()) {
            dashboard.setAverageScore(0.0);
        } else {
            BigDecimal total = scores.stream()
                    .map(ComprehensiveScore::getComprehensiveScore)
                    .filter(score -> score != null)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal average = total.divide(new BigDecimal(scores.size()), 2, RoundingMode.HALF_UP);
            dashboard.setAverageScore(average.doubleValue());
        }
        
        // 5. 查询实习岗位列表（前10个，按申请人数排序）
            // 只统计合作企业的岗位（postId不为null），过滤掉自主实习（postId为null）
        Map<Long, Long> postApplyCountMap = applies.stream()
                    .filter(apply -> apply.getPostId() != null) // 过滤掉postId为null的记录（自主实习）
                .collect(Collectors.groupingBy(InternshipApply::getPostId, Collectors.counting()));
        
        List<ClassTeacherDashboardDTO.PostInfo> posts = new ArrayList<>();
        postApplyCountMap.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // 按申请人数降序
                .limit(10) // 只取前10个
                .forEach(entry -> {
                        Long postId = entry.getKey();
                        if (postId == null) {
                            return; // 跳过null值
                        }
                        InternshipPost post = internshipPostMapper.selectById(postId);
                    if (post != null && post.getDeleteFlag() != null && 
                        post.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                        ClassTeacherDashboardDTO.PostInfo postInfo = new ClassTeacherDashboardDTO.PostInfo();
                        postInfo.setPostId(post.getPostId());
                        postInfo.setPostName(post.getPostName());
                        
                        // 查询企业信息
                        if (post.getEnterpriseId() != null) {
                            Enterprise enterprise = enterpriseMapper.selectById(post.getEnterpriseId());
                            if (enterprise != null && enterprise.getDeleteFlag() != null &&
                                enterprise.getDeleteFlag().equals(DeleteFlag.NORMAL.getCode())) {
                                postInfo.setEnterpriseName(enterprise.getEnterpriseName());
                            }
                        }
                        
                        postInfo.setApplyCount(entry.getValue());
                        posts.add(postInfo);
                    }
                });
        dashboard.setPosts(posts);
        
        // 6. 统计评价分数分布（饼图数据）
        List<ClassTeacherDashboardDTO.PieItem> scoreDistribution = new ArrayList<>();
        if (!scores.isEmpty()) {
            long excellentCount = scores.stream()
                    .filter(s -> {
                        BigDecimal score = s.getComprehensiveScore();
                        return score != null && score.compareTo(new BigDecimal("90")) >= 0;
                    })
                    .count();
            long goodCount = scores.stream()
                    .filter(s -> {
                        BigDecimal score = s.getComprehensiveScore();
                        return score != null && score.compareTo(new BigDecimal("80")) >= 0 && 
                               score.compareTo(new BigDecimal("90")) < 0;
                    })
                    .count();
            long mediumCount = scores.stream()
                    .filter(s -> {
                        BigDecimal score = s.getComprehensiveScore();
                        return score != null && score.compareTo(new BigDecimal("70")) >= 0 && 
                               score.compareTo(new BigDecimal("80")) < 0;
                    })
                    .count();
            long passCount = scores.stream()
                    .filter(s -> {
                        BigDecimal score = s.getComprehensiveScore();
                        return score != null && score.compareTo(new BigDecimal("60")) >= 0 && 
                               score.compareTo(new BigDecimal("70")) < 0;
                    })
                    .count();
            long failCount = scores.stream()
                    .filter(s -> {
                        BigDecimal score = s.getComprehensiveScore();
                        return score != null && score.compareTo(new BigDecimal("60")) < 0;
                    })
                    .count();
            
            scoreDistribution.add(new ClassTeacherDashboardDTO.PieItem("优秀(90-100)", excellentCount, COLOR_SUCCESS));
            scoreDistribution.add(new ClassTeacherDashboardDTO.PieItem("良好(80-89)", goodCount, COLOR_PRIMARY));
            scoreDistribution.add(new ClassTeacherDashboardDTO.PieItem("中等(70-79)", mediumCount, COLOR_WARNING));
            scoreDistribution.add(new ClassTeacherDashboardDTO.PieItem("及格(60-69)", passCount, COLOR_INFO));
            scoreDistribution.add(new ClassTeacherDashboardDTO.PieItem("不及格(<60)", failCount, "#FF7875"));
        }
        dashboard.setScoreDistribution(scoreDistribution);
        
        } catch (Exception e) {
            // 捕获所有异常，记录日志并返回空数据，避免500错误
            System.err.println("获取班主任仪表盘数据失败: " + e.getMessage());
            e.printStackTrace();
            // 返回空数据而不是抛出异常
            dashboard.setClassCount(0L);
            dashboard.setStudentCount(0L);
            dashboard.setPostCount(0L);
            dashboard.setAverageScore(0.0);
            dashboard.setPosts(new ArrayList<>());
            dashboard.setScoreDistribution(new ArrayList<>());
        }
        
        return dashboard;
    }
}

