package com.server.internshipserver;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * 检查教师状态的测试类
 * 用于查询 teacher04 用户的详细信息
 */
@SpringBootTest
public class CheckTeacherStatusMain {
    
    @Autowired(required = false)
    private JdbcTemplate jdbcTemplate;
    
    @Test
    public void checkTeacher04Status() {
        if (jdbcTemplate == null) {
            System.out.println("JdbcTemplate未注入，无法查询数据库");
            return;
        }
        
        System.out.println("==========================================");
        System.out.println("查询 teacher04 用户信息");
        System.out.println("==========================================");
        
        // 查询用户和教师信息
        String sql1 = "SELECT u.user_id, u.username, u.status as user_status, " +
                     "u.delete_flag as user_delete_flag, t.teacher_id, t.teacher_no, " +
                     "t.audit_status, t.audit_time, t.auditor_id " +
                     "FROM user_info u " +
                     "LEFT JOIN teacher_info t ON u.user_id = t.user_id " +
                     "WHERE u.username = 'teacher04'";
        
        List<Map<String, Object>> userInfo = jdbcTemplate.queryForList(sql1);
        System.out.println("\n【用户和教师信息】");
        if (userInfo.isEmpty()) {
            System.out.println("未找到 teacher04 用户");
        } else {
            for (Map<String, Object> row : userInfo) {
                System.out.println("user_id: " + row.get("user_id"));
                System.out.println("username: " + row.get("username"));
                System.out.println("user_status: " + row.get("user_status") + " (1=启用, 0=禁用)");
                System.out.println("user_delete_flag: " + row.get("user_delete_flag") + " (0=正常, 1=已删除)");
                System.out.println("teacher_id: " + row.get("teacher_id"));
                System.out.println("teacher_no: " + row.get("teacher_no"));
                System.out.println("audit_status: " + row.get("audit_status") + " (0=待审核, 1=已通过, 2=已拒绝)");
                System.out.println("audit_time: " + row.get("audit_time"));
                System.out.println("auditor_id: " + row.get("auditor_id"));
            }
        }
        
        // 查询角色信息
        String sql2 = "SELECT ur.user_id, ur.role_id, r.role_code, r.role_name, ur.delete_flag " +
                     "FROM user_role ur " +
                     "INNER JOIN role_info r ON ur.role_id = r.role_id " +
                     "WHERE ur.user_id = (SELECT user_id FROM user_info WHERE username = 'teacher04') " +
                     "AND ur.delete_flag = 0 AND r.delete_flag = 0";
        
        List<Map<String, Object>> roles = jdbcTemplate.queryForList(sql2);
        System.out.println("\n【角色信息】");
        if (roles.isEmpty()) {
            System.out.println("⚠️ 警告：用户没有分配任何角色！这可能导致登录失败。");
        } else {
            for (Map<String, Object> row : roles) {
                System.out.println("role_id: " + row.get("role_id"));
                System.out.println("role_code: " + row.get("role_code"));
                System.out.println("role_name: " + row.get("role_name"));
            }
        }
        
        System.out.println("\n==========================================");
    }
}

