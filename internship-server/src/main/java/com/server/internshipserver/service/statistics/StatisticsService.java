package com.server.internshipserver.service.statistics;

import com.server.internshipserver.domain.statistics.dto.ClassStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.EvaluationScoreStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.EnterpriseStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.InternshipDurationStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.InternshipProgressStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.MajorStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.PostTypeDistributionStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.StatisticsQueryDTO;
import com.server.internshipserver.domain.statistics.dto.StudentScoreRankingDTO;
import com.server.internshipserver.domain.statistics.dto.PendingReviewStatisticsDTO;
import com.server.internshipserver.domain.statistics.dto.SystemAdminDashboardDTO;
import com.server.internshipserver.domain.statistics.dto.SchoolAdminDashboardDTO;
import com.server.internshipserver.domain.statistics.dto.CollegeLeaderDashboardDTO;
import com.server.internshipserver.domain.statistics.dto.ClassTeacherDashboardDTO;

/**
 * 数据统计Service接口
 */
public interface StatisticsService {
    
    /**
     * 获取实习进度统计
     * @param queryDTO 查询条件
     * @return 实习进度统计数据
     */
    InternshipProgressStatisticsDTO getInternshipProgressStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取评价分数统计
     * @param queryDTO 查询条件
     * @return 评价分数统计数据
     */
    EvaluationScoreStatisticsDTO getEvaluationScoreStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取实习时长统计
     * @param queryDTO 查询条件
     * @return 实习时长统计数据
     */
    InternshipDurationStatisticsDTO getInternshipDurationStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取岗位类型分布统计
     * @param queryDTO 查询条件
     * @return 岗位类型分布统计数据
     */
    PostTypeDistributionStatisticsDTO getPostTypeDistributionStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取专业维度统计
     * @param queryDTO 查询条件
     * @return 专业维度统计数据
     */
    MajorStatisticsDTO getMajorStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取班级维度统计
     * @param queryDTO 查询条件
     * @return 班级维度统计数据
     */
    ClassStatisticsDTO getClassStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取企业维度统计
     * @param queryDTO 查询条件
     * @return 企业维度统计数据
     */
    EnterpriseStatisticsDTO getEnterpriseStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取学生评价分数排行（班主任使用）
     * @param queryDTO 查询条件
     * @return 学生分数排行数据
     */
    StudentScoreRankingDTO getStudentScoreRanking(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取待批阅统计（班主任使用）
     * @param queryDTO 查询条件
     * @return 待批阅统计数据
     */
    PendingReviewStatisticsDTO getPendingReviewStatistics(StatisticsQueryDTO queryDTO);
    
    /**
     * 获取系统管理员仪表盘统计数据
     * @return 系统管理员仪表盘统计数据
     */
    SystemAdminDashboardDTO getSystemAdminDashboard();
    
    /**
     * 获取学校管理员仪表盘统计数据
     * @return 学校管理员仪表盘统计数据
     */
    SchoolAdminDashboardDTO getSchoolAdminDashboard();
    
    /**
     * 获取学院负责人仪表盘统计数据
     * @return 学院负责人仪表盘统计数据
     */
    CollegeLeaderDashboardDTO getCollegeLeaderDashboard();
    
    /**
     * 获取班主任仪表盘统计数据
     * @return 班主任仪表盘统计数据
     */
    ClassTeacherDashboardDTO getClassTeacherDashboard();
}

