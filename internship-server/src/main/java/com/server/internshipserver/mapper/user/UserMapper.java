package com.server.internshipserver.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.server.internshipserver.domain.user.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户基础信息Mapper接口
 * 提供用户基础信息的数据库操作方法，包括用户角色和权限的查询
 */
@Mapper
public interface UserMapper extends BaseMapper<UserInfo> {
    
    /**
     * 根据用户ID查询用户角色代码列表
     * 
     * @param userId 用户ID
     * @return 角色代码列表
     */
    @Select("SELECT r.role_code FROM role_info r " +
            "INNER JOIN user_role ur ON r.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND ur.delete_flag = 0 AND r.delete_flag = 0")
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID查询用户权限代码列表
     * 通过用户角色关联表和角色权限关联表查询用户的所有权限
     * 
     * @param userId 用户ID
     * @return 权限代码列表
     */
    @Select("SELECT DISTINCT p.permission_code FROM permission_info p " +
            "INNER JOIN role_permission rp ON p.permission_id = rp.permission_id " +
            "INNER JOIN user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND ur.delete_flag = 0 AND rp.delete_flag = 0 AND p.delete_flag = 0")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}

