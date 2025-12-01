package com.server.internshipserver.controller.statistics;

import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.domain.statistics.dto.*;
import com.server.internshipserver.domain.statistics.dto.SystemAdminDashboardDTO;
import com.server.internshipserver.domain.statistics.dto.SchoolAdminDashboardDTO;
import com.server.internshipserver.domain.statistics.dto.CollegeLeaderDashboardDTO;
import com.server.internshipserver.domain.statistics.dto.ClassTeacherDashboardDTO;
import com.server.internshipserver.service.statistics.StatisticsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * 数据统计Controller
 */
@Api(tags = "数据统计管理")
@RestController
@RequestMapping("/statistics")
public class StatisticsController {
    
    @Autowired
    private StatisticsService statisticsService;
    
    @ApiOperation("获取实习进度统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR', 'ROLE_STUDENT')")
    @GetMapping("/internship-progress")
    public Result<InternshipProgressStatisticsDTO> getInternshipProgressStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId,
            @ApiParam(value = "班级ID", required = false) @RequestParam(required = false) Long classId,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setMajorId(majorId);
        queryDTO.setClassId(classId);
        queryDTO.setEnterpriseId(enterpriseId);
        
        InternshipProgressStatisticsDTO statistics = statisticsService.getInternshipProgressStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取评价分数统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/evaluation-score")
    public Result<EvaluationScoreStatisticsDTO> getEvaluationScoreStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId,
            @ApiParam(value = "班级ID", required = false) @RequestParam(required = false) Long classId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setMajorId(majorId);
        queryDTO.setClassId(classId);
        
        EvaluationScoreStatisticsDTO statistics = statisticsService.getEvaluationScoreStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取实习时长统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/internship-duration")
    public Result<InternshipDurationStatisticsDTO> getInternshipDurationStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setEnterpriseId(enterpriseId);
        
        InternshipDurationStatisticsDTO statistics = statisticsService.getInternshipDurationStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取岗位类型分布统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/post-type-distribution")
    public Result<PostTypeDistributionStatisticsDTO> getPostTypeDistributionStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setEnterpriseId(enterpriseId);
        
        PostTypeDistributionStatisticsDTO statistics = statisticsService.getPostTypeDistributionStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取专业维度统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER')")
    @GetMapping("/major")
    public Result<MajorStatisticsDTO> getMajorStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        
        MajorStatisticsDTO statistics = statisticsService.getMajorStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取班级维度统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN', 'ROLE_COLLEGE_LEADER', 'ROLE_CLASS_TEACHER')")
    @GetMapping("/class")
    public Result<ClassStatisticsDTO> getClassStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId,
            @ApiParam(value = "学院ID", required = false) @RequestParam(required = false) Long collegeId,
            @ApiParam(value = "专业ID", required = false) @RequestParam(required = false) Long majorId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        queryDTO.setCollegeId(collegeId);
        queryDTO.setMajorId(majorId);
        
        ClassStatisticsDTO statistics = statisticsService.getClassStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取企业维度统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/enterprise")
    public Result<EnterpriseStatisticsDTO> getEnterpriseStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        
        EnterpriseStatisticsDTO statistics = statisticsService.getEnterpriseStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取学院维度统计")
    @PreAuthorize("hasAnyRole('ROLE_SYSTEM_ADMIN', 'ROLE_SCHOOL_ADMIN')")
    @GetMapping("/college")
    public Result<CollegeStatisticsDTO> getCollegeStatistics(
            @ApiParam(value = "开始日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @ApiParam(value = "结束日期", required = false) @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
            @ApiParam(value = "学校ID", required = false) @RequestParam(required = false) Long schoolId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setStartDate(startDate);
        queryDTO.setEndDate(endDate);
        queryDTO.setSchoolId(schoolId);
        
        CollegeStatisticsDTO statistics = statisticsService.getCollegeStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取学生评价分数排行（班主任、企业管理员、企业导师使用）")
    @PreAuthorize("hasAnyRole('ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_ADMIN', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/student-score-ranking")
    public Result<StudentScoreRankingDTO> getStudentScoreRanking(
            @ApiParam(value = "班级ID", required = false) @RequestParam(required = false) Long classId,
            @ApiParam(value = "企业ID", required = false) @RequestParam(required = false) Long enterpriseId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setClassId(classId);
        queryDTO.setEnterpriseId(enterpriseId);
        
        StudentScoreRankingDTO statistics = statisticsService.getStudentScoreRanking(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取待批阅统计（班主任和企业导师使用）")
    @PreAuthorize("hasAnyRole('ROLE_CLASS_TEACHER', 'ROLE_ENTERPRISE_MENTOR')")
    @GetMapping("/pending-review")
    public Result<PendingReviewStatisticsDTO> getPendingReviewStatistics(
            @ApiParam(value = "班级ID", required = false) @RequestParam(required = false) Long classId) {
        StatisticsQueryDTO queryDTO = new StatisticsQueryDTO();
        queryDTO.setClassId(classId);
        
        PendingReviewStatisticsDTO statistics = statisticsService.getPendingReviewStatistics(queryDTO);
        return Result.success(statistics);
    }
    
    @ApiOperation("获取系统管理员仪表盘统计数据")
    @PreAuthorize("hasRole('ROLE_SYSTEM_ADMIN')")
    @GetMapping("/system-admin-dashboard")
    public Result<SystemAdminDashboardDTO> getSystemAdminDashboard() {
        SystemAdminDashboardDTO dashboard = statisticsService.getSystemAdminDashboard();
        return Result.success(dashboard);
    }
    
    @ApiOperation("获取学校管理员仪表盘统计数据")
    @PreAuthorize("hasRole('ROLE_SCHOOL_ADMIN')")
    @GetMapping("/school-admin-dashboard")
    public Result<SchoolAdminDashboardDTO> getSchoolAdminDashboard() {
        SchoolAdminDashboardDTO dashboard = statisticsService.getSchoolAdminDashboard();
        return Result.success(dashboard);
    }
    
    @ApiOperation("获取学院负责人仪表盘统计数据")
    @PreAuthorize("hasRole('ROLE_COLLEGE_LEADER')")
    @GetMapping("/college-leader-dashboard")
    public Result<CollegeLeaderDashboardDTO> getCollegeLeaderDashboard() {
        CollegeLeaderDashboardDTO dashboard = statisticsService.getCollegeLeaderDashboard();
        return Result.success(dashboard);
    }
    
    @ApiOperation("获取班主任仪表盘统计数据")
    @PreAuthorize("hasRole('ROLE_CLASS_TEACHER')")
    @GetMapping("/class-teacher-dashboard")
    public Result<ClassTeacherDashboardDTO> getClassTeacherDashboard() {
        ClassTeacherDashboardDTO dashboard = statisticsService.getClassTeacherDashboard();
        return Result.success(dashboard);
    }
}

