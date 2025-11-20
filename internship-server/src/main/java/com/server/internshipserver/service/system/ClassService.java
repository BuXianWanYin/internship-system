package com.server.internshipserver.service.system;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.system.Class;

/**
 * 班级管理Service接口
 */
public interface ClassService extends IService<Class> {
    
    /**
     * 添加班级
     * @param classInfo 班级信息
     * @return 添加的班级信息
     */
    Class addClass(Class classInfo);
    
    /**
     * 更新班级信息
     * @param classInfo 班级信息
     * @return 更新后的班级信息
     */
    Class updateClass(Class classInfo);
    
    /**
     * 根据ID查询班级详情
     * @param classId 班级ID
     * @return 班级信息
     */
    Class getClassById(Long classId);
    
    /**
     * 分页查询班级列表
     * @param page 分页参数
     * @param className 班级名称（可选）
     * @param majorId 专业ID（可选，用于数据权限过滤）
     * @param collegeId 学院ID（可选，用于筛选）
     * @param schoolId 学校ID（可选，用于筛选）
     * @return 班级列表
     */
    Page<Class> getClassPage(Page<Class> page, String className, Long majorId, Long collegeId, Long schoolId);
    
    /**
     * 停用班级（软删除）
     * @param classId 班级ID
     * @return 是否成功
     */
    boolean deleteClass(Long classId);
    
    /**
     * 生成班级分享码
     * @param classId 班级ID
     * @return 分享码
     */
    String generateShareCode(Long classId);
    
    /**
     * 重新生成班级分享码
     * @param classId 班级ID
     * @return 新的分享码
     */
    String regenerateShareCode(Long classId);
    
    /**
     * 验证分享码
     * @param shareCode 分享码
     * @return 班级信息，如果验证失败返回null
     */
    Class validateShareCode(String shareCode);
    
    /**
     * 获取分享码信息
     * @param classId 班级ID
     * @return 分享码信息（包含分享码、生成时间、过期时间、使用次数）
     */
    java.util.Map<String, Object> getShareCodeInfo(Long classId);
    
    /**
     * 增加分享码使用次数
     * @param shareCode 分享码
     */
    void incrementShareCodeUseCount(String shareCode);
    
    /**
     * 任命班主任
     * @param classId 班级ID
     * @param teacherId 教师ID（对应Teacher表的teacherId）
     * @return 是否成功
     */
    boolean appointClassTeacher(Long classId, Long teacherId);
    
    /**
     * 取消班主任任命
     * @param classId 班级ID
     * @return 是否成功
     */
    boolean removeClassTeacher(Long classId);
}

