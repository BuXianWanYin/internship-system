package com.server.internshipserver.controller.user;

import com.server.internshipserver.common.constant.Constants;
import com.server.internshipserver.common.result.Result;
import com.server.internshipserver.common.result.ResultCode;
import com.server.internshipserver.common.utils.JwtUtil;
import com.server.internshipserver.common.utils.RedisUtil;
import com.server.internshipserver.domain.user.User;
import com.server.internshipserver.service.user.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证授权控制器
 */
@Api(tags = "认证授权管理")
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    @Autowired
    private AuthenticationManager authenticationManager;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private RedisUtil redisUtil;
    
    @Autowired
    private UserService userService;

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody User loginUser) {
        // 参数校验
        if (!StringUtils.hasText(loginUser.getUsername())) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "用户名不能为空");
        }
        if (!StringUtils.hasText(loginUser.getPassword())) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "密码不能为空");
        }
        
        try {
            // 进行身份认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginUser.getUsername(), loginUser.getPassword()));
            
            // 认证成功，生成Token
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtUtil.generateToken(userDetails);
            
            // 将Token存储到Redis
            String redisKey = Constants.TOKEN_KEY_PREFIX + loginUser.getUsername();
            redisUtil.set(redisKey, token, 2, TimeUnit.HOURS);
            
            // 构建返回数据
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("username", loginUser.getUsername());
            
            // 添加用户详细信息
            User user = userService.getUserByUsername(loginUser.getUsername());
            if (user != null) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("userId", user.getUserId());
                userInfo.put("realName", user.getRealName());
                userInfo.put("phone", user.getPhone());
                userInfo.put("email", user.getEmail());
                userInfo.put("avatar", user.getAvatar());
                data.put("userInfo", userInfo);
            }
            
            return Result.success("登录成功", data);
        } catch (Exception e) {
            return Result.error(ResultCode.LOGIN_ERROR);
        }
    }

    @ApiOperation("用户登出")
    @PostMapping("/logout")
    public Result<?> logout(HttpServletRequest request) {
        try {
            // 从请求头获取Token
            String token = getTokenFromRequest(request);
            if (token != null && !token.isEmpty()) {
                // 从Token中获取用户名
                String username = jwtUtil.getUsernameFromToken(token);
                if (username != null) {
                    // 从Redis中删除Token
                    String redisKey = Constants.TOKEN_KEY_PREFIX + username;
                    redisUtil.delete(redisKey);
                }
            }
            return Result.success("登出成功");
        } catch (Exception e) {
            // 即使出错也返回成功，避免影响用户体验
            return Result.success("登出成功");
        }
    }
    
    /**
     * 从请求头获取Token
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    @ApiOperation("刷新Token")
    @PostMapping("/refresh")
    public Result<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token == null || token.isEmpty()) {
            return Result.error(ResultCode.PARAM_ERROR.getCode(), "Token不能为空");
        }
        try {
            String username = jwtUtil.getUsernameFromToken(token);
            String newToken = jwtUtil.refreshToken(token);
            
            // 更新Redis中的Token
            String redisKey = Constants.TOKEN_KEY_PREFIX + username;
            redisUtil.set(redisKey, newToken, 7, TimeUnit.DAYS);
            
            Map<String, Object> data = new HashMap<>();
            data.put("token", newToken);
            
            return Result.success("Token刷新成功", data);
        } catch (Exception e) {
            return Result.error(ResultCode.TOKEN_INVALID);
        }
    }
}

