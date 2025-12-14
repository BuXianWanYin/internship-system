package com.server.internshipserver.service.impl.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.enums.AuditStatus;
import com.server.internshipserver.common.enums.DeleteFlag;
import com.server.internshipserver.common.enums.UserStatus;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.common.utils.RedisUtil;
import com.server.internshipserver.domain.user.Student;
import com.server.internshipserver.domain.user.Teacher;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.mapper.user.StudentMapper;
import com.server.internshipserver.mapper.user.TeacherMapper;
import com.server.internshipserver.mapper.user.UserMapper;
import com.server.internshipserver.service.user.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证授权Service实现类
 * 实现用户登录、登出、Token刷新等认证授权功能
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private TeacherMapper teacherMapper;
    
    @Autowired
    private StudentMapper studentMapper;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration; // JWT过期时间（毫秒）
    
    @Value("${jwt.refresh-expiration}")
    private Long jwtRefreshExpiration; // JWT刷新过期时间（毫秒）
    
    @Override
    public Map<String, Object> login(UserInfo loginUser) {
        // 参数校验
        if (!StringUtils.hasText(loginUser.getUsername())) {
            throw new BusinessException("用户名不能为空");
        }
        if (!StringUtils.hasText(loginUser.getPassword())) {
            throw new BusinessException("密码不能为空");
        }
        
        // 按照正确的顺序进行校验：
        // 1. 先校验是否有账号（用户是否存在）
        // 2. 再校验是否删除（如果已删除，提示"用户不存在"）
        // 3. 再校验是否启用
        // 4. 再校验审核状态（如果是教师/学生）
        // 5. 最后校验用户名和密码是否正确（在authenticationManager.authenticate中进行）
        
        // 步骤1和2：检查用户是否存在，以及是否已删除
        // 注意：这里不通过userService.getUserByUsername查询，因为它会过滤已删除的用户
        // 我们需要直接查询，以便判断用户是否存在以及是否已删除
        UserInfo user = userMapper.selectOne(
                new LambdaQueryWrapper<UserInfo>()
                        .eq(UserInfo::getUsername, loginUser.getUsername())
        );
        
        // 如果用户不存在，直接抛出异常
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        
        // 步骤2：检查是否已删除
        if (user.getDeleteFlag() == null || user.getDeleteFlag().equals(DeleteFlag.DELETED.getCode())) {
            throw new BusinessException("用户不存在");
        }
        
        // 步骤3：检查用户状态（必须是启用）
        if (user.getStatus() == null || user.getStatus().equals(UserStatus.DISABLED.getCode())) {
            throw new BusinessException("账号已被禁用，无法登录");
        }
        
        // 步骤4：检查审核状态（如果是教师或学生）
        Teacher teacher = teacherMapper.selectOne(
                new LambdaQueryWrapper<Teacher>()
                        .eq(Teacher::getUserId, user.getUserId())
        );
        
        // 如果是教师，检查审核状态
        if (teacher != null) {
            if (teacher.getAuditStatus() == null || !teacher.getAuditStatus().equals(AuditStatus.APPROVED.getCode())) {
                if (teacher.getAuditStatus() != null && teacher.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
                    throw new BusinessException("账号未审核通过，无法登录，请等待管理员审核");
                } else if (teacher.getAuditStatus() != null && teacher.getAuditStatus().equals(AuditStatus.REJECTED.getCode())) {
                    throw new BusinessException("账号审核未通过，无法登录");
                } else {
                    throw new BusinessException("账号审核状态异常，无法登录");
                }
            }
        } else {
            // 检查是否是学生
            Student student = studentMapper.selectOne(
                    new LambdaQueryWrapper<Student>()
                            .eq(Student::getUserId, user.getUserId())
            );
            
            // 如果是学生，检查审核状态
            if (student != null) {
                if (student.getAuditStatus() == null || !student.getAuditStatus().equals(AuditStatus.APPROVED.getCode())) {
                    if (student.getAuditStatus() != null && student.getAuditStatus().equals(AuditStatus.PENDING.getCode())) {
                        throw new BusinessException("账号未审核通过，无法登录，请等待管理员审核");
                    } else if (student.getAuditStatus() != null && student.getAuditStatus().equals(AuditStatus.REJECTED.getCode())) {
                        throw new BusinessException("账号审核未通过，无法登录");
                    } else {
                        throw new BusinessException("账号审核状态异常，无法登录");
                    }
                }
            }
        }
        
        // 进行身份认证（密码验证）
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
        } catch (org.springframework.security.core.AuthenticationException e) {
            // 捕获认证异常，将其转换为BusinessException，以便返回更友好的错误信息
            // 注意：审核状态、账号禁用等异常已经在上面检查过了，但UserDetailsServiceImpl可能也会抛出异常
            String message = e.getMessage();
            // 检查是否是审核状态、账号状态相关的异常消息
            if (message != null) {
                if (message.contains("账号未审核") || message.contains("审核") || 
                    message.contains("账号已被禁用") || message.contains("用户不存在")) {
                    // 如果是审核状态或账号状态相关的异常，直接使用原消息
                    throw new BusinessException(message);
                }
            }
            // 其他认证异常（主要是密码错误）
            throw new BusinessException("用户名或密码错误");
        }
        
        // 认证成功，生成Token
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String token = jwtUtil.generateToken(userDetails);
        
        // 将Token存储到Redis（过期时间与JWT token过期时间一致）
        String redisKey = Constants.TOKEN_KEY_PREFIX + loginUser.getUsername();
        // 将毫秒转换为秒
        long expirationSeconds = jwtExpiration / 1000;
        redisUtil.set(redisKey, token, expirationSeconds, TimeUnit.SECONDS);
        
        // 获取用户角色列表
        List<String> roles = userDetails.getAuthorities().stream()
                .map(authority -> authority.getAuthority())
                .filter(authority -> authority.startsWith("ROLE_"))
                .collect(Collectors.toList());
        
        // 构建返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("username", loginUser.getUsername());
        
        // 添加用户详细信息（user变量在前面已经定义过，直接使用）
        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("userId", user.getUserId());
            userInfo.put("realName", user.getRealName());
            userInfo.put("phone", user.getPhone());
            userInfo.put("email", user.getEmail());
            userInfo.put("avatar", user.getAvatar());
            userInfo.put("roles", roles);  // 添加角色信息
            data.put("userInfo", userInfo);
        }
        
        return data;
    }
    
    @Override
    public boolean logout(String token) {
        if (token == null || token.isEmpty()) {
            return true; // Token为空也返回成功，避免影响用户体验
        }
        
        try {
            // 从Token中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            if (username != null) {
                // 从Redis中删除Token
                String redisKey = Constants.TOKEN_KEY_PREFIX + username;
                redisUtil.delete(redisKey);
            }
            return true;
        } catch (Exception e) {
            // 即使出错也返回成功，避免影响用户体验
            return true;
        }
    }
    
    @Override
    public Map<String, Object> refreshToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException("Token不能为空");
        }
        
        String username = jwtUtil.getUsernameFromToken(token);
        String newToken = jwtUtil.refreshToken(token);
        
        // 更新Redis中的Token（过期时间与JWT refresh token过期时间一致）
        String redisKey = Constants.TOKEN_KEY_PREFIX + username;
        // 将毫秒转换为秒
        long expirationSeconds = jwtRefreshExpiration / 1000;
        redisUtil.set(redisKey, newToken, expirationSeconds, TimeUnit.SECONDS);
        
        Map<String, Object> data = new HashMap<>();
        data.put("token", newToken);
        
        return data;
    }
}

