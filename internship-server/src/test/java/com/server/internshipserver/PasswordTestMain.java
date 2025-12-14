package com.server.internshipserver;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 密码加密和校验测试主类
 * 可以直接运行main方法来测试密码匹配
 */
public class PasswordTestMain {
    
    public static void main(String[] args) {
        // 创建BCrypt密码编码器（与SecurityConfig中配置的相同）
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        
        System.out.println("==========================================");
        System.out.println("密码加密和校验测试");
        System.out.println("==========================================");
        
        // 测试1：加密和校验原始密码
        String rawPassword = "123456";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        System.out.println("\n【测试1】加密和校验原始密码");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("加密后的密码: " + encodedPassword);
        System.out.println("密码匹配结果: " + passwordEncoder.matches(rawPassword, encodedPassword));
        
        // 测试2：使用用户提供的加密密码进行校验
        String userProvidedEncodedPassword = "$2a$10$/Y5E9OS96LvWBPbAPZGfSuMtIdMNOfFf5Rt2HFXMXqzQm0cBaCVlW";
        String userRawPassword = "123456";
        boolean matches = passwordEncoder.matches(userRawPassword, userProvidedEncodedPassword);
        System.out.println("\n【测试2】使用用户提供的加密密码进行校验");
        System.out.println("用户原始密码: " + userRawPassword);
        System.out.println("数据库中的加密密码: " + userProvidedEncodedPassword);
        System.out.println("密码匹配结果: " + matches);
        
        if (matches) {
            System.out.println("✓ 密码匹配成功！加密和校验使用同一个算法。");
        } else {
            System.out.println("✗ 密码匹配失败！");
            System.out.println("  可能的原因：");
            System.out.println("  1. 密码加密时使用的不是BCryptPasswordEncoder");
            System.out.println("  2. 密码被重复加密了");
            System.out.println("  3. 数据库中存储的密码不正确");
            System.out.println("  4. 原始密码不是 '123456'");
        }
        
        // 测试3：验证BCrypt加密特性（每次加密结果都不同，但都能匹配）
        System.out.println("\n【测试3】验证BCrypt加密特性");
        String encoded1 = passwordEncoder.encode(rawPassword);
        String encoded2 = passwordEncoder.encode(rawPassword);
        System.out.println("第一次加密: " + encoded1);
        System.out.println("第二次加密: " + encoded2);
        System.out.println("两次加密结果相同: " + encoded1.equals(encoded2));
        System.out.println("但都能匹配原始密码: " + 
            (passwordEncoder.matches(rawPassword, encoded1) && 
             passwordEncoder.matches(rawPassword, encoded2)));
        
        // 测试4：尝试不同的密码组合
        System.out.println("\n【测试4】测试不同的密码组合");
        String[] testPasswords = {"123456", "1234567", "12345", "teacher04", "Teacher04", "TEACHER04"};
        for (String testPwd : testPasswords) {
            boolean testMatches = passwordEncoder.matches(testPwd, userProvidedEncodedPassword);
            System.out.println("密码 '" + testPwd + "' 匹配结果: " + testMatches);
        }
        
        // 测试5：模拟注册时的密码加密过程
        System.out.println("\n【测试5】模拟注册时的密码加密过程");
        String registerPassword = "123456";
        String encrypted1 = passwordEncoder.encode(registerPassword);
        System.out.println("注册时第一次加密: " + encrypted1);
        
        // 检查是否已经被加密（BCrypt加密后的字符串以$2a$开头）
        if (encrypted1.startsWith("$2a$") || encrypted1.startsWith("$2b$") || encrypted1.startsWith("$2y$")) {
            System.out.println("密码已被正确加密（以$2a$开头）");
            // 如果再次加密（错误的情况）
            String doubleEncrypted = passwordEncoder.encode(encrypted1);
            System.out.println("如果错误地再次加密: " + doubleEncrypted);
            System.out.println("双重加密后能否匹配原始密码: " + passwordEncoder.matches(registerPassword, doubleEncrypted));
            System.out.println("双重加密后能否匹配第一次加密结果: " + passwordEncoder.matches(encrypted1, doubleEncrypted));
        }
        
        System.out.println("\n==========================================");
        System.out.println("测试完成");
        System.out.println("==========================================");
    }
}

