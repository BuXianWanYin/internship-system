package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.SchoolAdmin;

/**
 * 学校管理员管理Service接口
 */
public interface SchoolAdminService extends IService<SchoolAdmin> {
    
    /**
     * 根据用户ID查询学校管理员信息
     * @param userId 用户ID
     * @return 学校管理员信息
     */
    SchoolAdmin getSchoolAdminByUserId(Long userId);
    
    /**
     * 根据学校ID查询学校管理员信息（取第一个启用的管理员）
     * @param schoolId 学校ID
     * @return 学校管理员信息
     */
    SchoolAdmin getSchoolAdminBySchoolId(Long schoolId);
    
    /**
     * 添加学校管理员
     * @param schoolAdmin 学校管理员信息
     * @return 添加的学校管理员信息
     */
    SchoolAdmin addSchoolAdmin(SchoolAdmin schoolAdmin);
    
    /**
     * 更新学校管理员信息
     * @param schoolAdmin 学校管理员信息
     * @return 更新后的学校管理员信息
     */
    SchoolAdmin updateSchoolAdmin(SchoolAdmin schoolAdmin);
    
    /**
     * 停用学校管理员（软删除）
     * @param adminId 管理员ID
     * @return 是否成功
     */
    boolean deleteSchoolAdmin(Long adminId);
}

