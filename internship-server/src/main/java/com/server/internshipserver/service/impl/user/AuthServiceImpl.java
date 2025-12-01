package com.server.internshipserver.service.impl.user;

import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.exception.BusinessException;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.common.utils.RedisUtil;
import com.server.internshipserver.domain.user.UserInfo;
import com.server.internshipserver.service.user.AuthService;
import com.server.internshipserver.service.user.UserService;
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
    private UserService userService;
    
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
        
        // 进行身份认证
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
        
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
        
        // 添加用户详细信息
        UserInfo user = userService.getUserByUsername(loginUser.getUsername());
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

