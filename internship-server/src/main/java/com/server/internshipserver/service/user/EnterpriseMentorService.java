package com.server.internshipserver.service.user;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.server.internshipserver.domain.user.EnterpriseMentor;

/**
 * 企业导师管理Service接口
 */
public interface EnterpriseMentorService extends IService<EnterpriseMentor> {
    
    /**
     * 根据用户ID查询企业导师信息
     * @param userId 用户ID
     * @return 企业导师信息
     */
    EnterpriseMentor getEnterpriseMentorByUserId(Long userId);
    
    /**
     * 添加企业导师（自动创建用户）
     * @param mentorName 导师姓名
     * @param enterpriseId 所属企业ID
     * @param position 职位
     * @param department 部门
     * @param phone 联系电话
     * @param email 邮箱
     * @param password 初始密码
     * @param status 状态
     * @return 添加的企业导师信息
     */
    EnterpriseMentor addEnterpriseMentorWithUser(String mentorName, Long enterpriseId, 
                                                  String position, String department,
                                                  String phone, String email, 
                                                  String password, Integer status);
    
    /**
     * 更新企业导师信息（同时更新用户信息）
     * @param mentorId 企业导师ID
     * @param userId 用户ID
     * @param mentorName 导师姓名
     * @param enterpriseId 所属企业ID
     * @param position 职位
     * @param department 部门
     * @param phone 联系电话
     * @param email 邮箱
     * @param status 状态
     * @return 更新后的企业导师信息
     */
    EnterpriseMentor updateEnterpriseMentorWithUser(Long mentorId, Long userId, 
                                                      String mentorName, Long enterpriseId,
                                                      String position, String department,
                                                      String phone, String email, Integer status);
    
    /**
     * 根据ID查询企业导师详情
     * @param mentorId 企业导师ID
     * @return 企业导师信息
     */
    EnterpriseMentor getEnterpriseMentorById(Long mentorId);
    
    /**
     * 分页查询企业导师列表
     * @param page 分页参数
     * @param mentorName 导师姓名（可选）
     * @param enterpriseId 企业ID（可选）
     * @param status 状态：1-启用，0-禁用（可选）
     * @return 企业导师列表
     */
    Page<EnterpriseMentor> getEnterpriseMentorPage(Page<EnterpriseMentor> page, 
                                                    String mentorName, Long enterpriseId, Integer status);
    
    /**
     * 停用企业导师（软删除）
     * @param mentorId 企业导师ID
     * @return 是否成功
     */
    boolean deleteEnterpriseMentor(Long mentorId);
}

